package com.example.projectpoker.model.game;

import com.example.projectpoker.model.game.enums.Rank;
import com.example.projectpoker.model.game.enums.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CardDeckTest {

    private CardDeck cardDeck;

    @BeforeEach
    void setUp() {
        cardDeck = new CardDeck();
    }

    @Test
    void constructorCreatesDeckWith52Cards() {
        int initialDeckSize = drawAllCards(cardDeck).size();
        assertEquals(52, initialDeckSize, "New deck should contain 52 cards");
    }

    @Test
    void constructorCreates52UniqueCards() {
        Set<String> cardSet = new HashSet<>();
        for (Card card : drawAllCards(cardDeck)) {
            String cardKey = card.getSuit() + "_" + card.getRank();
            assertTrue(cardSet.add(cardKey), "Deck should not contain duplicate cards: " + cardKey);
        }
        assertEquals(52, cardSet.size());
    }

    @Test
    void drawRemovesCardFromDeck() {
        int cardsBefore = drawAllCards(cardDeck).size();
        CardDeck freshDeck = new CardDeck();
        freshDeck.draw();
        int cardsAfter = drawAllCards(freshDeck).size();
        assertEquals(51, cardsAfter);
        assertEquals(52, cardsBefore);
    }

    @Test
    void drawReturnsNullWhenDeckEmpty() {
        CardDeck emptyDeck = new CardDeck();
        for (int i = 0; i < 52; i++) {
            emptyDeck.draw();
        }
        assertNull(emptyDeck.draw(), "Draw should return null when deck is empty");
    }

    @Test
    void burnCardRemovesCardFromDeck() {
        CardDeck testDeck = new CardDeck();
        testDeck.burnCard();
        int remainingCards = drawAllCards(testDeck).size();
        assertEquals(51, remainingCards, "Burn card should remove one card from deck");
    }

    @Test
    void resetRestoresDeckTo52Cards() {
        CardDeck testDeck = new CardDeck();
        testDeck.draw();
        testDeck.draw();
        testDeck.burnCard();
        testDeck.reset();
        int cardsAfterReset = drawAllCards(testDeck).size();
        assertEquals(52, cardsAfterReset, "Reset should restore deck to 52 cards");
    }

    @Test
    void shuffleDoesNotChangeDeckSize() {
        CardDeck testDeck = new CardDeck();
        int deckSize = drawAllCards(testDeck).size();
        testDeck.shuffle();
        int shuffledSize = drawAllCards(testDeck).size();
        assertEquals(deckSize, shuffledSize, "Shuffle should not change deck size");
    }

    @Test
    void allSuitsAreRepresentedInDeck() {
        Set<Suit> suits = new HashSet<>();
        for (Card card : drawAllCards(cardDeck)) {
            suits.add(card.getSuit());
        }
        assertEquals(4, suits.size(), "Deck should contain all 4 suits");
    }

    @Test
    void allRanksAreRepresentedInDeck() {
        Set<Rank> ranks = new HashSet<>();
        for (Card card : drawAllCards(cardDeck)) {
            ranks.add(card.getRank());
        }
        assertEquals(13, ranks.size(), "Deck should contain all 13 ranks");
    }

    @Test
    void drawnCardsAreDistinctAfterMultipleDraws() {
        Set<String> drawnCardKeys = new HashSet<>();
        CardDeck testDeck = new CardDeck();
        for (int i = 0; i < 5; i++) {
            Card card = testDeck.draw();
            String cardKey = card.getSuit() + "_" + card.getRank();
            assertTrue(drawnCardKeys.add(cardKey), "Each drawn card should be unique");
        }
    }

    private java.util.ArrayList<Card> drawAllCards(CardDeck deck) {
        java.util.ArrayList<Card> cards = new java.util.ArrayList<>();
        Card card;
        while ((card = deck.draw()) != null) {
            cards.add(card);
        }
        return cards;
    }
}
