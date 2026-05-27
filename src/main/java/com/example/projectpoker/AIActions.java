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
    private static final String GEMINI_API_KEY = "";
    // gemini-3.1-flash-lite-preview / gemma-4-31b-it / gemini-2.5-flash
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key="
                    + GEMINI_API_KEY;

    // Gets poker actions for one or more AI players by sending their game states to Gemini.
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