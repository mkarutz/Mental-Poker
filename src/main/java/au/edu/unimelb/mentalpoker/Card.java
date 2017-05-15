package au.edu.unimelb.mentalpoker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Card {
    private final Suit suit;
    private final Rank rank;

    public enum Suit {
        DIAMONDS("D"),
        CLUBS("C"),
        HEARTS("H"),
        SPADES("S");

        private String symbol;

        Suit(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    public enum Rank {
        TWO("2"),
        THREE("3"),
        FOUR("4"),
        FIVE("5"),
        SIX("6"),
        SEVEN("7"),
        EIGHT("8"),
        NINE("9"),
        TEN("T"),
        JACK("J"),
        QUEEN("Q"),
        KING("K"),
        ACE("A");

        private String symbol;

        Rank(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Rank getRank() {
        return rank;
    }

    public static List<Card> standardDeck() {
        final List<Card> result = new ArrayList<>(52);
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                result.add(new Card(suit, rank));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return rank.toString() + suit.toString();
    }

    public int getRawRank() {
        if (rank == Rank.ACE) {
            return 1;
        }
        return rank.ordinal() + 2;
    }

    public String getSuit() {
        return suit.symbol;
    }

    public static Comparator<Card> COMPARE_BY_RANK = new Comparator<Card>() {
        @Override
        public int compare(Card c1, Card c2) {
            return c1.getRawRank() - c2.getRawRank();
        }
    };

    public static Comparator<Card> ACE_HIGH = new Comparator<Card>() {
        @Override
        public int compare(Card o1, Card o2) {
            return o1.rank.compareTo(o2.rank);
        }
    };
}
