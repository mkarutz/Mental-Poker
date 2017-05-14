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
