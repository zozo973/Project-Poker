package com.example.projectpoker;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.enums.*;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AIActions {

    public enum AiPersonality {
        ROBOT, PIRATE, WIZARD
    }

    // Load from environment variable (Best Practice)
    private static final String GEMINI_API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY;

    public static class PokerAIResponse {
        public Action action;
        public int confidence;
        public String reaction;
        public int amount;
        public String errormsg;
    }

    public PokerAIResponse getChoice(Card[] handCards, Card[] boardCards, Stage stage, AiPersonality personality) {
        PokerAIResponse result = new PokerAIResponse();
        try {
            Gson gson = new Gson();

            // 1. Build System Instruction
            JsonObject systemInstruction = new JsonObject();
            JsonArray systemParts = new JsonArray();
            JsonObject systemTextObj = new JsonObject();
            systemTextObj.addProperty("text", getSystemPrompt(personality));
            systemParts.add(systemTextObj);
            systemInstruction.add("parts", systemParts);

            // 2. Build User Content
            JsonObject content = new JsonObject();
            JsonArray contentParts = new JsonArray();
            JsonObject contentTextObj = new JsonObject();
            contentTextObj.addProperty("text", getUserPrompt(handCards, boardCards, stage));
            contentParts.add(contentTextObj);
            content.add("parts", contentParts);
            JsonArray contentsArray = new JsonArray();
            contentsArray.add(content);

            // 3. Build Full Request Body
            JsonObject requestBody = new JsonObject();
            requestBody.add("system_instruction", systemInstruction);
            requestBody.add("contents", contentsArray);

            JsonObject config = new JsonObject();
            config.addProperty("response_mime_type", "application/json");
            requestBody.add("generationConfig", config);

            // 4. Send Request
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 5. Parse
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            String generatedText = jsonResponse.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            return gson.fromJson(generatedText, PokerAIResponse.class);

        } catch (Exception e) {
            e.printStackTrace();
            result.errormsg = "AI suggestion failed: " + e.getMessage();
            return result;
        }
    }

    private String getSystemPrompt(AiPersonality personality) {
        String personaDescription = switch (personality) {
            case ROBOT -> "You are a cold, calculated logic engine. Play according to strict GTO strategies.";
            case PIRATE -> "You are a daring Poker Playing Pirate. Be aggressive and take risks.";
            case WIZARD -> "You are a mysterious, ancient Wizard. Play cautiously, predicting the future of the cards.";
        };

        return String.format("""
                %s
                [STRICT OUTPUT RULES]
                1. Output ONLY pure JSON.
                2. Fields required:
                   - "action": One of "FOLD", "CHECK", "CALL", "BET", "RAISE", "ALL_IN".
                   - "confidence": Integer (0-100).
                   - "reaction": A single sentence written in your persona style stating your action and reasoning.
                   - "amount": Integer. If action is BET, RAISE, or ALL_IN, provide the chip amount. Otherwise 0.
                """, personaDescription);
    }

    private String getUserPrompt(Card[] handCards, Card[] boardCards, Stage stage) {
        return String.format("""
                [CURRENT GAME STATE]
                Game Stage: %s
                Your Hand: %s
                Community Cards: %s
                """, stage.toString(), formatCards(handCards), formatCards(boardCards));
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


void main() {
}
 // For Test API connection ----------------------------------------------------------------------------------------
 public static void main(String[] args) {
     System.out.println("--- Starting AIActions Harness Tests ---\n");
     AIActions coach = new AIActions();

     // Setup test data
     Card[] hand = {
             new Card(com.example.projectpoker.model.Suit.SPADES, com.example.projectpoker.model.Rank.ACE),
             new Card(com.example.projectpoker.model.Suit.HEARTS, com.example.projectpoker.model.Rank.ACE)
     };
     Card[] board = {
             new Card(com.example.projectpoker.model.Suit.CLUBS, com.example.projectpoker.model.Rank.TWO),
             new Card(com.example.projectpoker.model.Suit.DIAMONDS, com.example.projectpoker.model.Rank.FIVE)
     };

     // Test each personality
     for (AiPersonality p : AiPersonality.values()) {
         System.out.println("Testing Personality: " + p);
         PokerAIResponse response = coach.getChoice(hand, board, Stage.allIn, p);
         printResult(response);
     }
     System.out.println("--- Harness Tests Finished ---");
 }

private static void printResult(PokerAIResponse response) {
    if (response.errormsg != null) {
        System.err.println("  -> Error: " + response.errormsg);
    } else {
        System.out.println("  -> Action: " + response.action);
        System.out.println("  -> Amount: " + response.amount);
        System.out.println("  -> Confidence: " + response.confidence + "%");
        System.out.println("  -> Reaction: " + response.reaction);
    }
    System.out.println("-------------------------------------------------");
}
}
