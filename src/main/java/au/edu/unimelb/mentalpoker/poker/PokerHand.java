package au.edu.unimelb.mentalpoker.poker;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class PokerHand implements Comparable<PokerHand> {
    private static final int POKER_HAND_SIZE = 5;

    private final Type type;
    private final ImmutableList<Card> cards;

    public enum Type {
        HIGH_CARD,
        PAIR,
        TWO_PAIR,
        TRIPLE,
        STRAIGHT,
        FLUSH,
        FULL_HOUSE,
        QUAD,
        STRAIGHT_FLUSH;
    }

    private static class RankPair implements Comparable<RankPair> {
        final Card.Rank first;
        final Card.Rank second;

        public RankPair(Card.Rank first, Card.Rank second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public int compareTo(RankPair o) {
            final int cmp = first.compareTo(o.first);
            if (cmp == 0) {
                return second.compareTo(o.second);
            } else {
                return cmp;
            }
        }
    }

    public PokerHand(Type type, List<Card> cards) {
        checkArgument(cards.size() == POKER_HAND_SIZE);
        cards = new ArrayList<>(cards);
        cards.sort(Card.ACE_HIGH.reversed());
        this.type = type;
        this.cards = ImmutableList.copyOf(cards);
    }

    @Override
    public int compareTo(PokerHand o) {
        if (!type.equals(o.type)) {
            return type.compareTo(o.type);
        }

        switch (type) {
            case HIGH_CARD:
                return compareHighCards(cards, o.cards);
            case PAIR:
                return comparePair(cards, o.cards);
            case TWO_PAIR:
                return compareTwoPair(cards, o.cards);
            case TRIPLE:
                return compareTriple(cards, o.cards);
            case STRAIGHT:
                return compareStraight(cards, o.cards);
            case FLUSH:
                return compareFlush(cards, o.cards);
            case FULL_HOUSE:
                return compareFullHouse(cards, o.cards);
            case QUAD:
                return compareQuad(cards, o.cards);
            case STRAIGHT_FLUSH:
                return compareStraightFlush(cards, o.cards);
            default:
                return compareHighCards(cards, o.cards);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append(" (");
        for (Card card : cards) {
            sb.append(card);
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PokerHand pokerHand = (PokerHand) o;

        if (type != pokerHand.type) return false;
        return cards.equals(pokerHand.cards);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + cards.hashCode();
        return result;
    }

    private static int compareHighCards(List<Card> a, List<Card> b) {
        for (int i = 0; i < a.size(); i++) {
            final int cmp = a.get(i).getRank().compareTo(b.get(i).getRank());
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    private static int comparePair(List<Card> a, List<Card> b) {
        final int cmp = extractPairRank(a).compareTo(extractPairRank(b));
        if (cmp == 0) {
            return compareHighCards(a, b);
        } else {
            return cmp;
        }
    }

    private static int compareTwoPair(List<Card> a, List<Card> b) {
        final int cmp = extractTwoPairRanks(a).compareTo(extractTwoPairRanks(b));
        if (cmp == 0) {
            return compareHighCards(a, b);
        } else {
            return cmp;
        }
    }

    private static int compareTriple(List<Card> a, List<Card> b) {
        final int cmp = extractTripleRank(a).compareTo(extractTripleRank(b));
        if (cmp == 0) {
            return compareHighCards(a, b);
        } else {
            return cmp;
        }
    }

    private static int compareStraight(List<Card> a, List<Card> b) {
        return compareHighCards(a, b);
    }

    private static int compareFlush(List<Card> a, List<Card> b) {
        return compareHighCards(a, b);
    }

    private static int compareFullHouse(List<Card> a, List<Card> b) {
        final int cmp = extractFullHouseRanks(a).compareTo(extractFullHouseRanks(b));
        if (cmp == 0) {
            return compareHighCards(a, b);
        } else {
            return cmp;
        }
    }

    private static RankPair extractFullHouseRanks(List<Card> cards) {
        if (cards.get(0).getRank().equals(cards.get(2).getRank())) {
            return new RankPair(cards.get(0).getRank(), cards.get(3).getRank());
        }

        return new RankPair(cards.get(2).getRank(), cards.get(0).getRank());
    }

    private static int compareQuad(List<Card> a, List<Card> b) {
        final int cmp = extractQuadRank(a).compareTo(extractQuadRank(b));
        if (cmp == 0) {
            return compareHighCards(a, b);
        } else {
            return cmp;
        }
    }

    private static int compareStraightFlush(List<Card> a, List<Card> b) {
        return compareHighCards(a, b);
    }

    private static Card.Rank extractQuadRank(List<Card> cards) {
        return extractPairRank(cards);
    }

    private static RankPair extractTwoPairRanks(List<Card> cards) {
        for (int i = 0; i < cards.size() - 1; i++) {
            final Card curr = cards.get(i);
            final Card next = cards.get(i + 1);
            if (curr.getRank().equals(next.getRank())) {
                return new RankPair(curr.getRank(), extractPairRank(cards.subList(i + 2, cards.size())));
            }
        }
        throw new RuntimeException();
    }

    private static Card.Rank extractPairRank(List<Card> cards) {
        for (int i = 0; i < cards.size() - 1; i++) {
            final Card curr = cards.get(i);
            final Card next = cards.get(i + 1);
            if (curr.getRank().equals(next.getRank())) {
                return curr.getRank();
            }
        }
        throw new RuntimeException();
    }

    private static Card.Rank extractTripleRank(List<Card> cards) {
        for (int i = 0; i < cards.size() - 1; i++) {
            final Card curr = cards.get(i);
            final Card second = cards.get(i + 1);
            final Card third = cards.get(i + 1);
            if (curr.getRank().equals(second.getRank()) && curr.getRank().equals(third.getRank())) {
                return curr.getRank();
            }
        }
        throw new RuntimeException();
    }
}
