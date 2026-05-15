package com.example.projectpoker;

// Project model imports
import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.RoundStatus;


// JSON handling
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

// HTTP/API handling
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


public class AIActions {

    // AI personality options
    public enum AiPersonality {
        ROBOT, PIRATE, WIZARD
    }

    public static class AiPlayerResponse {
        public int playerNumber;
        public Action action;
        public int amount;
        public String errormsg;
    }

    public AiPlayerResponse getChoice(
            Card[] handCards,
            Card[] boardCards,
            RoundStatus currentStatus,
            int toPlay,
            int potSize,
            int stackSize,
            int requiredToCall,
            int alreadyInvested
    ) {
        List<Card[]> handCardsPerPlayer = new ArrayList<>();
        handCardsPerPlayer.add(handCards);

        List<Integer> stackSizes = new ArrayList<>();
        stackSizes.add(stackSize);

        List<Integer> requiredToCallList = new ArrayList<>();
        requiredToCallList.add(requiredToCall);

        List<Integer> alreadyInvestedList = new ArrayList<>();
        alreadyInvestedList.add(alreadyInvested);

        List<AiPlayerResponse> responses = getAllChoices(
                handCardsPerPlayer,
                boardCards,
                currentStatus,
                toPlay,
                potSize,
                stackSizes,
                requiredToCallList,
                alreadyInvestedList
        );

        if (responses == null || responses.isEmpty()) {
            AiPlayerResponse r = new AiPlayerResponse();
            r.playerNumber = 1;
            r.errormsg = "Gemini returned no response for single AI player.";
            return r;
        }

        return responses.get(0);
    }

    // Gemini API configuration
    private static final String GEMINI_API_KEY = "API";
    // gemini-3.1-flash-lite-preview / gemma-4-31b-it / gemini-2.5-flash
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key="
                    + GEMINI_API_KEY;

    public List<AiPlayerResponse> getAllChoices(List<Card[]> handCardsPerPlayer, Card[] boardCards, RoundStatus currentStatus, int toPlay, int potSize, List<Integer> stackSizes, List<Integer> requiredToCallList, List<Integer> alreadyInvestedList) {
        List<AiPlayerResponse> results = new ArrayList<>();
        int expectedCount = handCardsPerPlayer == null ? 0 : handCardsPerPlayer.size();
        if (stackSizes == null || requiredToCallList == null || alreadyInvestedList == null || stackSizes.size() != expectedCount || requiredToCallList.size() != expectedCount || alreadyInvestedList.size() != expectedCount) {
            throw new IllegalArgumentException(
                    "AI input list sizes do not match. handCards=" + expectedCount + ", stacks=" + (stackSizes == null ? "null" : stackSizes.size()) + ", requiredToCall=" + (requiredToCallList == null ? "null" : requiredToCallList.size()) + ", alreadyInvested=" + (alreadyInvestedList == null ? "null" : alreadyInvestedList.size())
            );
        }
        try {
            Gson gson = new Gson();
            String systemPrompt = gson.toJson(getSystemPromptForAiPlayer(expectedCount));
            String userPrompt   = gson.toJson(buildPrompt(handCardsPerPlayer, boardCards, currentStatus, toPlay, potSize,stackSizes,requiredToCallList,alreadyInvestedList));

            String requestBody = String.format("""
                    {
                      "system_instruction": { "parts": [ { "text": %s } ] },
                      "contents": [ { "parts": [ { "text": %s } ] } ],
                      "generationConfig": { "response_mime_type": "application/json" }
                    }
                    """, systemPrompt, userPrompt);

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("[AIActions] HTTP status: " + response.statusCode());
            //System.out.println("[AIActions] Raw body: " + response.body());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API error " + response.statusCode() + ": " + response.body());
            }
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);

            String generatedText = extractGeneratedText(jsonResponse);

            //System.out.println("[AIActions] Gemini raw response: " + generatedText);
            System.out.println("[AIActions] Gemini response received.");
            String cleaned = generatedText.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```json", "").replaceAll("```", "").trim();
            }
            com.google.gson.stream.JsonReader reader =
                    new com.google.gson.stream.JsonReader(new java.io.StringReader(cleaned));
            reader.setLenient(false);
            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
            if (jsonArray == null) {
                throw new RuntimeException("Gemini generated text is not a valid JSON array.");
            }
            for (int i = 0; i < Math.min(jsonArray.size(), expectedCount); i++) {
                JsonObject obj = jsonArray.get(i).getAsJsonObject();
                AiPlayerResponse r = parseAiResponse(obj);
                r.playerNumber = i + 1;
                results.add(r);
            }

            while (results.size() < expectedCount) {
                AiPlayerResponse r = new AiPlayerResponse();
                r.playerNumber = results.size() + 1;
                r.errormsg = "Gemini returned fewer results than expected.";
                results.add(r);
            }

            //System.out.println("[AIActions] Gemini API success — got " + results.size() + " decisions for " + expectedCount + " AI players.");

        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("[AIActions] Gemini API failed: " + e.getMessage());
            // If API fails, return CALL for all players as fallback
            results.clear();
            for (int i = 0; i < expectedCount; i++) {
                AiPlayerResponse r = new AiPlayerResponse();
                r.playerNumber = i + 1;
                r.errormsg = "AI action failed: " + e.getMessage();
                results.add(r);
            }
        }
        return results;
    }

    // Check what Gemini return
    private String extractGeneratedText(JsonObject jsonResponse) {
        if (jsonResponse == null) {
            throw new RuntimeException("Gemini response is null.");
        }

        JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Gemini returned no candidates.");
        }

        JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
        if (firstCandidate == null || !firstCandidate.has("content")) {
            throw new RuntimeException("Gemini response missing content.");
        }

        JsonObject content = firstCandidate.getAsJsonObject("content");
        if (content == null || !content.has("parts")) {
            throw new RuntimeException("Gemini response missing parts.");
        }

        JsonArray parts = content.getAsJsonArray("parts");
        if (parts == null || parts.isEmpty()) {
            throw new RuntimeException("Gemini response parts is empty.");
        }

        JsonObject firstPart = parts.get(0).getAsJsonObject();
        if (firstPart == null || !firstPart.has("text")) {
            throw new RuntimeException("Gemini response missing text.");
        }

        return firstPart.get("text").getAsString();
    }

    //In case Gemini doesn't follow the formal
    private AiPlayerResponse parseAiResponse(JsonObject obj) {
        AiPlayerResponse r = new AiPlayerResponse();

        try {
            if (obj == null || !obj.has("action")) {
                r.errormsg = "Missing action from Gemini response.";
                return r;
            }

            String actionText = obj.get("action").getAsString().trim().toUpperCase();

            // Accept common AI variants
            if (actionText.equals("ALL_IN") || actionText.equals("ALL-IN")) {
                actionText = "ALLIN";
            }

            try {
                r.action = Action.valueOf(actionText);
            } catch (IllegalArgumentException e) {
                r.errormsg = "Invalid action from Gemini: " + actionText;
                return r;
            }

            if (obj.has("amount") && !obj.get("amount").isJsonNull()) {
                r.amount = Math.max(0, obj.get("amount").getAsInt());
            } else {
                r.amount = 0;
            }

            if (r.action != Action.RAISE) {
                r.amount = 0;
            }

        } catch (Exception e) {
            r.errormsg = "Failed to parse Gemini response: " + e.getMessage();
        }

        return r;
    }


    private String getSystemPromptForAiPlayer(int playerCount) {
        return String.format("""
                You are a Texas Hold'em poker AI controlling %d players simultaneously.
                [STRICT OUTPUT RULES]
                1. Output ONLY a pure JSON array with exactly %d elements. No markdown.
                2. Each element must contain exactly:
                   - "action": One of "FOLD", "CHECK", "CALL", "RAISE", "ALLIN".
                   - "amount": Integer. If action is RAISE, this means the total bet amount to raise to, not the extra amount added. The amount must be greater than the current bet to call and must not exceed the player's max raise-to amount. For FOLD, CHECK, CALL, and ALLIN, use 0.
                3. Play reasonably — do NOT fold unless the hand is very weak AND the bet is very high relative to stack.
                4. Example for %d players: %s
                5. Never output "BET", "ALL_IN", "ALL-IN", or any action outside the allowed list.
                """, playerCount, playerCount, playerCount, buildExample(playerCount));
        }

    private String buildExample(int count) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            sb.append("{\"action\":\"CALL\",\"amount\":0}");
            if (i < count - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String buildPrompt(List<Card[]> handCardsPerPlayer, Card[] boardCards, RoundStatus currentStatus, int toPlay, int potSize, List<Integer> stackSizes, List<Integer> requiredToCallList, List<Integer> alreadyInvestedList) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Stage: %s, Board: %s, Bet to call: %d, Pot size: %d\n",
                currentStatus.name(), formatCards(boardCards), toPlay, potSize));
        for (int i = 0; i < handCardsPerPlayer.size(); i++) {
            int stack = (stackSizes != null && i < stackSizes.size()) ? stackSizes.get(i) : 0;
            int requiredToCall = (requiredToCallList != null && i < requiredToCallList.size()) ? requiredToCallList.get(i) : 0;
            int alreadyInvested = (alreadyInvestedList != null && i < alreadyInvestedList.size()) ? alreadyInvestedList.get(i) : 0;
            int maxRaiseTo = alreadyInvested + stack;

            sb.append(String.format(
                    "Player %d Hand: %s, Stack: %d, Required to call: %d, Already invested in current pot: %d, Max raise-to amount: %d\n",
                    i + 1,
                    formatCards(handCardsPerPlayer.get(i)),
                    stack,
                    requiredToCall,
                    alreadyInvested,
                    maxRaiseTo
            ));
        }
        return sb.toString();
    }


    private String formatCards(Card[] cards) {
        if (cards == null || cards.length == 0 || cards[0] == null) return "None";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null) {
                sb.append(cards[i].getRank()).append(" of ").append(cards[i].getSuit());
                if (i < cards.length - 1 && cards[i + 1] != null) sb.append(", ");
            }
        }
        return sb.toString();
    }
}


/**
// Object used to store the AI response
public static class PokerAIResponse {
    public Action action;
    public int confidence;
    public String reaction;
    public int amount;
    public String errormsg;
}

// Main AI method: sends game state to Gemini and returns poker action advice
public PokerAIResponse getChoice(
        Card[] handCards,
        Card[] boardCards,
        String gameStage,
        AiPersonality personality
) {
    PokerAIResponse result = new PokerAIResponse();

    try {
        // Build prompts
        String systemPrompt = getSystemPrompt(personality);
        String userPrompt = getUserPrompt(handCards, boardCards, gameStage);

        Gson gson = new Gson();

        // Convert prompts safely into JSON strings
        String gsonSystemPrompt = gson.toJson(systemPrompt);
        String gsonUserPrompt = gson.toJson(userPrompt);

        // Build Gemini API request body
        String requestBody = String.format("""
                {
                  "system_instruction": { "parts": [ { "text": %s } ] },
                  "contents": [ { "parts": [ { "text": %s } ] } ],
                  "generationConfig": { "response_mime_type": "application/json" }
                }
                """, gsonSystemPrompt, gsonUserPrompt);

        // Create HTTP client
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Create POST request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Send request to Gemini
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // Convert Gemini response into JSON
        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);

        // Extract generated JSON text from Gemini response
        String generatedText = jsonResponse.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();

        // Parse AI's JSON decision
        JsonObject aiJson = gson.fromJson(generatedText, JsonObject.class);

        result.action = Action.valueOf(aiJson.get("action").getAsString());
        result.confidence = aiJson.get("confidence").getAsInt();
        result.reaction = aiJson.get("reaction").getAsString();
        result.amount = aiJson.get("amount").getAsInt();

    } catch (Exception e) {
        // Store error message if API or parsing fails
        e.printStackTrace();
        result.errormsg = "AI suggestion failed: " + e.getMessage();
    }

    return result;
}

// Creates the AI's personality and response rules
private String getSystemPrompt(AiPersonality personality) {
    String personaDescription = switch (personality) {
        case ROBOT ->
                "You are a cold, calculated logic engine. Play according to strict GTO strategies.";
        case PIRATE ->
                "You are a daring poker-playing pirate. Be aggressive and take risks.";
        case WIZARD ->
                "You are a mysterious ancient wizard. Play cautiously and predict the future of the cards.";
    };

    return String.format("""
            %s

            [STRICT OUTPUT RULES]
            1. You must output ONLY pure JSON data.
            2. Do NOT include Markdown formatting.
            3. Do NOT include greetings, explanations, or text outside the JSON.
            4. The JSON must exactly contain these four fields:
               - "action": Must be exactly one of: "FOLD", "CHECK", "CALL", "BET", "RAISE", "ALL_IN".
               - "confidence": An integer between 0 and 100.
               - "reaction": A single sentence written in your persona style explaining the decision.
               - "amount": An integer. If action is BET, RAISE, or ALL_IN, provide the chip amount. Otherwise use 0.
            """, personaDescription);
}

// Builds the game state prompt sent to the AI
private String getUserPrompt(Card[] handCards, Card[] boardCards, String gameStage) {
    return String.format("""
            [CURRENT GAME STATE]
            Game Stage: %s
            Your Hand: %s
            Community Cards: %s

            Based on the information above, provide your decision JSON.
            """, gameStage, formatCards(handCards), formatCards(boardCards));
}

// Converts card arrays into readable text for the prompt
private String formatCards(Card[] cards) {
    if (cards == null || cards.length == 0 || cards[0] == null) {
        return "None";
    }

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < cards.length; i++) {
        if (cards[i] != null) {
            sb.append(cards[i].getRank())
                    .append(" of ")
                    .append(cards[i].getSuit());

            if (i < cards.length - 1 && cards[i + 1] != null) {
                sb.append(", ");
            }
        }
    }

    return sb.toString();
}

// Test method for checking API connection and AI responses
public static void main(String[] args) {
    System.out.println("Preparing 3 different tests for AI Actions...\n");

    AIActions ai = new AIActions();

    // Test 1: null cards
    System.out.println("-------------------------------------------------");
    System.out.println("Test 1: Null cards + ROBOT personality");
    PokerAIResponse response1 = ai.getChoice(
            null,
            null,
            "PREFLOP",
            AiPersonality.ROBOT
    );
    printResult(response1);

    // Test 2: strong hand
    System.out.println("-------------------------------------------------");
    System.out.println("Test 2: Pair of Aces + PIRATE personality");

    Card[] strongHand = {
            new Card(Suit.Spades, Rank.Ace),
            new Card(Suit.Hearts, Rank.Ace)
    };

    Card[] flopBoard = {
            new Card(Suit.Clubs, Rank.Two),
            new Card(Suit.Diamonds, Rank.Five),
            new Card(Suit.Spades, Rank.Nine)
    };

    PokerAIResponse response2 = ai.getChoice(
            strongHand,
            flopBoard,
            "FLOP",
            AiPersonality.PIRATE
    );
    printResult(response2);

    // Test 3: weak hand
    System.out.println("-------------------------------------------------");
    System.out.println("Test 3: 2 of Hearts + 7 of Clubs + WIZARD personality");

    Card[] weakHand = {
            new Card(Suit.Hearts, Rank.Two),
            new Card(Suit.Clubs, Rank.Seven)
    };

    Card[] turnBoard = {
            new Card(Suit.Diamonds, Rank.Ace),
            new Card(Suit.Hearts, Rank.King),
            new Card(Suit.Spades, Rank.Queen),
            new Card(Suit.Clubs, Rank.Jack)
    };

    PokerAIResponse response3 = ai.getChoice(
            weakHand,
            turnBoard,
            "TURN",
            AiPersonality.WIZARD
    );
    printResult(response3);

    System.out.println("-----Finish-----");
}

// Prints test results clearly
private static void printResult(PokerAIResponse response) {
    if (response.errormsg != null) {
        System.out.println("Testing Fail: " + response.errormsg + "\n");
    } else {
        System.out.println("Action: " + response.action);
        System.out.println("Confidence: " + response.confidence + "%");
        System.out.println("Amount: " + response.amount);
        System.out.println("Reaction: " + response.reaction + "\n");
    }
}
}*/