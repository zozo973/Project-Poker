package com.example.projectpoker.model.game;


import com.example.projectpoker.model.game.enums.Rank;
import com.example.projectpoker.model.game.enums.Suit;

public class Card {

    private Suit suit;
    private Rank rank;

    // Constructor
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public int getValue() {
        return rank.ordinal() + 2;
    }

    // Possibly recode or remove method as it may not be necessary
    // as there should never be the exact same card
    @Override
    public boolean equals(Object o) {
        return (o instanceof Card && ((Card) o).rank == rank && ((Card) o).suit == suit);
    }

    public String getCardImagePath(Card card) {
        Character rank = card.getRank().name().toUpperCase().charAt(0);
        Character suit = card.getSuit().name().toUpperCase().charAt(0);

        return "/com/example/projectpoker/Images/" + suit + rank + ".png";
    }

    // These are instantiated here for testing purposes while I build in the rest of the files.
    // Remove later
    public static Card C2 = new Card(Suit.Clubs, Rank.Two);
    public static Card C3 = new Card(Suit.Clubs,Rank.Three);
    public static Card C4 = new Card(Suit.Clubs,Rank.Four);
    public static Card C5 = new Card(Suit.Clubs,Rank.Five);
    public static Card C6 = new Card(Suit.Clubs,Rank.Six);
    public static Card C7 = new Card(Suit.Clubs, Rank.Seven);
    public static Card C8 = new Card(Suit.Clubs,Rank.Eight);
    public static Card C9 = new Card(Suit.Clubs,Rank.Nine);
    public static Card CT = new Card(Suit.Clubs,Rank.Ten);
    public static Card CJ = new Card(Suit.Clubs,Rank.Jack);
    public static Card CQ = new Card(Suit.Clubs,Rank.Queen);
    public static Card CK = new Card(Suit.Clubs, Rank.King);
    public static Card CA = new Card(Suit.Clubs, Rank.Ace);

    public static Card D2 = new Card(Suit.Diamonds,Rank.Two);
    public static Card D3 = new Card(Suit.Diamonds,Rank.Three);
    public static Card D4 = new Card(Suit.Diamonds,Rank.Four);
    public static Card D5 = new Card(Suit.Diamonds,Rank.Five);
    public static Card D6 = new Card(Suit.Diamonds,Rank.Six);
    public static Card D7 = new Card(Suit.Diamonds, Rank.Seven);
    public static Card D8 = new Card(Suit.Diamonds,Rank.Eight);
    public static Card D9 = new Card(Suit.Diamonds,Rank.Nine);
    public static Card DT = new Card(Suit.Diamonds,Rank.Ten);
    public static Card DJ = new Card(Suit.Diamonds,Rank.Jack);
    public static Card DQ = new Card(Suit.Diamonds,Rank.Queen);
    public static Card DK = new Card(Suit.Diamonds, Rank.King);
    public static Card DA = new Card(Suit.Diamonds, Rank.Ace);

    public static Card H2 = new Card(Suit.Hearts,Rank.Two);
    public static Card H3 = new Card(Suit.Hearts,Rank.Three);
    public static Card H4 = new Card(Suit.Hearts,Rank.Four);
    public static Card H5 = new Card(Suit.Hearts,Rank.Five);
    public static Card H6 = new Card(Suit.Hearts,Rank.Six);
    public static Card H7 = new Card(Suit.Hearts, Rank.Seven);
    public static Card H8 = new Card(Suit.Hearts,Rank.Eight);
    public static Card H9 = new Card(Suit.Hearts,Rank.Nine);
    public static Card HT = new Card(Suit.Hearts,Rank.Ten);
    public static Card HJ = new Card(Suit.Hearts,Rank.Jack);
    public static Card HQ = new Card(Suit.Hearts,Rank.Queen);
    public static Card HK = new Card(Suit.Hearts, Rank.King);
    public static Card HA = new Card(Suit.Hearts, Rank.Ace);

    public static Card S2 = new Card(Suit.Spades,Rank.Two);
    public static Card S3 = new Card(Suit.Spades,Rank.Three);
    public static Card S4 = new Card(Suit.Spades,Rank.Four);
    public static Card S5 = new Card(Suit.Spades,Rank.Five);
    public static Card S6 = new Card(Suit.Spades,Rank.Six);
    public static Card S7 = new Card(Suit.Spades, Rank.Seven);
    public static Card S8 = new Card(Suit.Spades,Rank.Eight);
    public static Card S9 = new Card(Suit.Spades,Rank.Nine);
    public static Card ST = new Card(Suit.Spades,Rank.Ten);
    public static Card SJ = new Card(Suit.Spades,Rank.Jack);
    public static Card SQ = new Card(Suit.Spades,Rank.Queen);
    public static Card SK = new Card(Suit.Spades, Rank.King);
    public static Card SA = new Card(Suit.Spades, Rank.Ace);

}
