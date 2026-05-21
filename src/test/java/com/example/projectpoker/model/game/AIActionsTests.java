
package com.example.projectpoker.model.game;

import com.example.projectpoker.AIActions;
import com.example.projectpoker.AIActions.AiPersonality;
import com.example.projectpoker.AIActions.AiPlayerResponse;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.RoundStatus;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class AIActionsTests {

    @Test
    void testConnectionToJson() {
        Gson gson = new Gson();

        String json = """
                [
                  {"action":"CALL","amount":0},
                  {"action":"RAISE","amount":50}
                ]
                """;

        AiPlayerResponse[] responses =
                gson.fromJson(json, AiPlayerResponse[].class);

        assertNotNull(responses);
        assertEquals(2, responses.length);

        assertEquals(Action.CALL, responses[0].action);
        assertEquals(0, responses[0].amount);

        assertEquals(Action.RAISE, responses[1].action);
        assertEquals(50, responses[1].amount);

        System.out.println("JSON connection test result:");
        for (AiPlayerResponse response : responses) {
            System.out.println("Action: " + response.action + ", Amount: " + response.amount);
        }
    }

    @Test
    void testRandomHandAndPersonalityPrintResult() {
        AIActions aiActions = new AIActions();

        AiPersonality personality = getRandomPersonality();

        List<Card[]> hands = new ArrayList<>();
        hands.add(new Card[]{
                createRandomCard(),
                createRandomCard()
        });

        Card[] boardCards = new Card[]{
                createRandomCard(),
                createRandomCard(),
                createRandomCard()
        };

        RoundStatus status = RoundStatus.values()[0];

        List<AiPlayerResponse> result =
                callGetAllChoices(aiActions, hands, boardCards, status);

        assertNotNull(result);
        assertEquals(1, result.size());

        System.out.println("Random AI personality: " + personality);
        System.out.println("Random hand result:");

        for (AiPlayerResponse response : result) {
            System.out.println("Action: " + response.action);
            System.out.println("Amount: " + response.amount);
            System.out.println("Error: " + response.errormsg);

            assertTrue(
                    response.action != null || response.errormsg != null,
                    "Response should contain either a valid AI action or an error message"
            );
        }
    }

    private AiPersonality getRandomPersonality() {
        AiPersonality[] personalities = AiPersonality.values();
        return personalities[new Random().nextInt(personalities.length)];
    }

    private List<AiPlayerResponse> callGetAllChoices(
            AIActions aiActions,
            List<Card[]> hands,
            Card[] boardCards,
            RoundStatus status
    ) {
        int playerCount = hands.size();

        List<Integer> stackSizes = new ArrayList<>();
        List<Integer> requiredToCallList = new ArrayList<>();
        List<Integer> alreadyInvestedList = new ArrayList<>();

        for (int i = 0; i < playerCount; i++) {
            stackSizes.add(1000);
            requiredToCallList.add(0);
            alreadyInvestedList.add(0);
        }

        int toPlay = 0;
        int potSize = 100;

        return aiActions.getAllChoices(
                hands,
                boardCards,
                status,
                toPlay,
                potSize,
                stackSizes,
                requiredToCallList,
                alreadyInvestedList
        );
    }

    private Card createRandomCard() {
        try {
            Constructor<?> constructor = Card.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);

            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] arguments = new Object[parameterTypes.length];

            Random random = new Random();

            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].isEnum()) {
                    Object[] enumValues = parameterTypes[i].getEnumConstants();
                    arguments[i] = enumValues[random.nextInt(enumValues.length)];
                } else if (parameterTypes[i] == String.class) {
                    arguments[i] = "TEST";
                } else if (parameterTypes[i] == int.class || parameterTypes[i] == Integer.class) {
                    arguments[i] = 1;
                } else {
                    arguments[i] = null;
                }
            }

            return (Card) constructor.newInstance(arguments);

        } catch (Exception e) {
            System.out.println("Could not create random Card, using null instead: " + e.getMessage());
            return null;
        }
    }

    @Test
    void testGetActionForMultiplePlayers() {
        AIActions aiActions = new AIActions();

        List<Card[]> hands = new ArrayList<>();

        hands.add(new Card[]{
                createRandomCard(),
                createRandomCard()
        });

        hands.add(new Card[]{
                createRandomCard(),
                createRandomCard()
        });

        hands.add(new Card[]{
                createRandomCard(),
                createRandomCard()
        });

        Card[] boardCards = new Card[]{
                createRandomCard(),
                createRandomCard(),
                createRandomCard()
        };

        RoundStatus status = RoundStatus.values()[0];

        List<AiPlayerResponse> result =
                callGetAllChoices(aiActions, hands, boardCards, status);

        assertNotNull(result);
        assertEquals(3, result.size(), "Should return one AI response per player");

        System.out.println("Multiple player AI results:");

        for (int i = 0; i < result.size(); i++) {
            AiPlayerResponse response = result.get(i);

            System.out.println("Player " + (i + 1));
            System.out.println("Action: " + response.action);
            System.out.println("Amount: " + response.amount);
            System.out.println("Error: " + response.errormsg);

            assertTrue(
                    response.action != null || response.errormsg != null,
                    "Each player should have either an action or an error message"
            );
        }
    }
}