package au.edu.unimelb.mentalpoker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Card {
    private final Suit suit;
    private final int rank;

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
            for (int rank = 1; rank <= 13; rank++) {
                result.add(new Card(suit, rank));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return getRank() + suit.toString();
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

    public int getRawRank()
    {
        return this.rank;
    }
    public String getSuit()
    {
        return this.suit.symbol;
    }

    public static Comparator<Card> CompareByRank = new Comparator<Card>() {
        @Override
        public int compare(Card c1, Card c2) {
            return c1.getRawRank()<c2.getRawRank()?-1:c1.getRawRank()>c2.getRawRank()?+1:0;
        }
    } ;

}
