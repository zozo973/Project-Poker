package com.example.projectpoker;

// Project model imports
import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Rank;
import com.example.projectpoker.model.game.enums.Suit;

// JSON handling
import com.google.gson.Gson;
import com.google.gson.JsonObject;

// HTTP/API handling
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AIActions {

    // AI personality options
    public enum AiPersonality {
        ROBOT, PIRATE, WIZARD
    }

    // Gemini API configuration
    private static final String GEMINI_API_KEY = System.getenv("AIzaSyDQdmck1sQw-4fmV-w0H7NFsEXyMbreaJI");
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                    + GEMINI_API_KEY;

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
}