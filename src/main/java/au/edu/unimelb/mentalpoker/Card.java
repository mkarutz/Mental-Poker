package au.edu.unimelb.mentalpoker;

import java.util.ArrayList;
import java.util.List;

public class Card {
    private final Suit suit;
    private final int rank;
    private boolean isFaceUp;

    public Card(Suit suit, int rank) {
        if (rank < 1 || rank > 13) {
            throw new IllegalArgumentException("Rank out of range.");
        }

        this.suit = suit;
        this.rank = rank;
    }

    public static List<Card> standardDeck() {
        final List<Card> result = new ArrayList<>(52);
        for (Suit suit : Suit.values()) {
            for (int rank = 0; rank < 13; rank++) {
                result.add(new Card(suit, rank));
            }
        }
        return result;
    }

    /** An enum representing the suit of a card. */
    private enum Suit {
        SPADES("S"),
        DIAMONDS("D"),
        CLUBS("C"),
        HEARTS("H");

        private String symbol;

        Suit(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    public boolean isFaceUp() {
        return isFaceUp;
    }

    @Override
    public String toString() {
        if (!isFaceUp()) {
            return "?";
        }
        return suit.toString() + getRank();
    }

    private String getRank() {
        if (rank == 1) {
            return "A";
        } else if (rank == 11) {
            return "J";
        } else if (rank == 12) {
            return "Q";
        } else if (rank == 13) {
            return "K";
        } else {
            return Integer.toString(rank);
        }
    }
}
