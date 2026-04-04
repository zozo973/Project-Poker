package com.example.projectpoker;

import com.example.projectpoker.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


public class AiCoaching {

    //API Key in here
    private static final String GEMINI_API_KEY = "IWILLAPPLYTHEKEYLATER";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:" + GEMINI_API_KEY;

    public static class AiAdvice{
        public Action action;
        public int confidence;
        public String reason;
        public String errormsg;
    }

    public AiAdvice getAdvice(Card[] handCards, Card[] boardCards, Stage stage, AiAdviceMode mode) {
        AiAdvice result = new AiAdvice();

        try {
            //prompt contact
            String systemPrompt = getSystemPrompt();
            String userPrompt = getUserPrompt(handCards, boardCards, stage, mode);

            Gson gson = new Gson();
            String GsonSystemPrompt = gson.toJson(systemPrompt);
            String GsonUserPrompt = gson.toJson(userPrompt);

            //Json for input
            String requestBody = String.format("""
                {
                  "system_instruction": { "parts": [ { "text": %s } ] },
                  "contents": [ { "parts": [ { "text": %s } ] } ],
                  "generationConfig": { "response_mime_type": "application/json" }
                }
                """, GsonSystemPrompt, GsonUserPrompt);

            // HttpClient & HttpRequest
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send the data out
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // Recived Json
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);


            // {"action": "CALL", "confidence": 80, "reasoning": "XXXXXXXX"}
            String generatedText = jsonResponse.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            //
            JsonObject adviceJson = gson.fromJson(generatedText, JsonObject.class);
            result.action = Action.valueOf(adviceJson.get("action").getAsString());
            result.confidence = adviceJson.get("confidence").getAsInt();
            result.reason = adviceJson.get("reasoning").getAsString();

        } catch (Exception e) {
        e.printStackTrace();
        result.errormsg = "AI suggestion - verify with your own understanding";
    }
        return result;
    }

    public String getSystemPrompt(){
        return """
                You are a world-class Texas Hold'em poker master and AI coach. Your task is to analyze the current poker game state and provide the best action recommendation.
                [STRICT OUTPUT RULES]
                1. You must output ONLY pure JSON data. Do NOT include any Markdown formatting (e.g., no ```json).
                2. Do not include any greetings, conversational text, or additional explanations outside the JSON.
                3. The JSON must exactly contain the following three fields:
                   - "action": Must be exactly one of the following: "FOLD", "CHECK", "CALL", "BET".
                   - "confidence": An integer between 0 and 100 representing your confidence level.
                   - "reasoning": A brief, one-sentence explanation for your decision.
                """;
    }

    public String getUserPrompt(Card[] handCards, Card[] boardCards, Stage stage, AiAdviceMode mode) {
        String handStr = formatCards(handCards);
        String boardStr = formatCards(boardCards);

        String styleInstructions = switch (mode) {
            case RISKY -> "RISKY mode: Aim for the biggest benefit. Be aggressive, look for opportunities to bluff or BET heavily to maximize the pot size, even with marginal hands.";
            case SAFE -> "SAFE mode: Aim to minimize losses. Play very conservatively, FOLD if facing aggression without a strong hand, and prioritize protecting your chip stack.";
            case NORMAL -> "NORMAL mode: Play a balanced, standard Game Theory Optimal (GTO) style.";
        };

        return String.format("""
            [CURRENT GAME STATE]
            Game Stage: %s
            Advice Strategy: %s
            Your Hand: %s
            Community Cards (Board): %s
            
            Based on the information above, provide your decision JSON.
            """, stage.toString(), styleInstructions, handStr, boardStr);
    }

    //Change the card from array to text for the prompt input
    private String formatCards(Card[] cards) {
        if (cards == null || cards.length == 0 || cards[0] == null) {
            return "None";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] != null) {
                sb.append(cards[i].getRank()).append(" of ").append(cards[i].getSuit());
                if (i < cards.length - 1 && cards[i+1] != null) {
                    sb.append(", ");
                }
            }
        }
        return sb.toString();
    }
}






