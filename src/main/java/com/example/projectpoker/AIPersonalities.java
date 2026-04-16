
package com.example.projectpoker;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
class GenerateContentWithAction {
    public static void main(String[] args) {
        Client client = new Client();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3-flash-preview",
                        "Talk about being a Poker playing pirate",
                        null);

        System.out.println(response.text());
    }
}

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GenerateTextFromTextInput {
    public static void main(String[] args) {
        // The client gets the API key from the environment variable `GEMINI_API_KEY`.
        Client client = new Client();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3-flash-preview",
                        "Explain how AI works in a few words",
                        null);

        System.out.println(response.text());
    }
}








//
/////
/////Response Check
//public void respondCheck() {
//    System.out.println("Sample check");
//}
/////Response Bet
//public void respondBet() {
//    System.out.println("Sample Bet");
//}
/////Response Raise
//public void respondRaise() {
//    System.out.println("Sample Raise");
//}
/////Response Fold
//public void respondFold() {
//    System.out.println("Sample fold");
//}
/////Response All-in
//public void respondAllIn() {
//    System.out.println("Sample All-in");
//}
///// Response Loss
//public void respondLoss() {
//    System.out.println("Sample Loss");
//}
///// Response Win
//public void respondWin() {
//    System.out.println("Sample win");
//}
/////
