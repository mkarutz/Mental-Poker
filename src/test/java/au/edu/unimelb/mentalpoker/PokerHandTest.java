package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PokerHandTest {
    private static final PokerHand PAIR_LOW =
            new PokerHand(
                    PokerHand.Type.PAIR,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                            new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                            new Card(Card.Suit.HEARTS, Card.Rank.FOUR),
                            new Card(Card.Suit.HEARTS, Card.Rank.FIVE)));

    private static final PokerHand PAIR_MID =
            new PokerHand(
                    PokerHand.Type.PAIR,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                            new Card(Card.Suit.HEARTS, Card.Rank.KING),
                            new Card(Card.Suit.HEARTS, Card.Rank.QUEEN)));

    private static final PokerHand PAIR_HIGH =
            new PokerHand(
                    PokerHand.Type.PAIR,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.KING),
                            new Card(Card.Suit.CLUBS, Card.Rank.KING),
                            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                            new Card(Card.Suit.HEARTS, Card.Rank.TEN),
                            new Card(Card.Suit.HEARTS, Card.Rank.SEVEN)));

    private static final PokerHand TWO_PAIR_LOW =
            new PokerHand(
                    PokerHand.Type.TWO_PAIR,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                            new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                            new Card(Card.Suit.CLUBS, Card.Rank.THREE),
                            new Card(Card.Suit.HEARTS, Card.Rank.SEVEN)));

    private static final PokerHand TWO_PAIR_MID =
            new PokerHand(
                    PokerHand.Type.TWO_PAIR,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                            new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                            new Card(Card.Suit.CLUBS, Card.Rank.THREE),
                            new Card(Card.Suit.HEARTS, Card.Rank.KING)));

    private static final PokerHand TWO_PAIR_HIGH =
            new PokerHand(
                    PokerHand.Type.TWO_PAIR,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                            new Card(Card.Suit.CLUBS, Card.Rank.ACE),
                            new Card(Card.Suit.HEARTS, Card.Rank.KING),
                            new Card(Card.Suit.SPADES, Card.Rank.KING),
                            new Card(Card.Suit.HEARTS, Card.Rank.QUEEN)));

    private static final PokerHand TRIPLE_LOW =
            new PokerHand(
                    PokerHand.Type.TRIPLE,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                            new Card(Card.Suit.SPADES, Card.Rank.TWO),
                            new Card(Card.Suit.CLUBS, Card.Rank.THREE),
                            new Card(Card.Suit.HEARTS, Card.Rank.SEVEN)));

    private static final PokerHand TRIPLE_MID =
            new PokerHand(
                    PokerHand.Type.TRIPLE,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                            new Card(Card.Suit.SPADES, Card.Rank.TWO),
                            new Card(Card.Suit.CLUBS, Card.Rank.ACE),
                            new Card(Card.Suit.HEARTS, Card.Rank.KING)));

    private static final PokerHand TRIPLE_HIGH =
            new PokerHand(
                    PokerHand.Type.TRIPLE,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                            new Card(Card.Suit.CLUBS, Card.Rank.ACE),
                            new Card(Card.Suit.SPADES, Card.Rank.ACE),
                            new Card(Card.Suit.SPADES, Card.Rank.KING),
                            new Card(Card.Suit.HEARTS, Card.Rank.QUEEN)));

    private static final PokerHand STRAIGHT_LOW =
            new PokerHand(
                    PokerHand.Type.STRAIGHT,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                            new Card(Card.Suit.SPADES, Card.Rank.THREE),
                            new Card(Card.Suit.CLUBS, Card.Rank.FOUR),
                            new Card(Card.Suit.HEARTS, Card.Rank.FIVE)));


    private static final PokerHand STRAIGHT_HIGH =
            new PokerHand(
                    PokerHand.Type.STRAIGHT,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                            new Card(Card.Suit.CLUBS, Card.Rank.KING),
                            new Card(Card.Suit.SPADES, Card.Rank.TEN),
                            new Card(Card.Suit.SPADES, Card.Rank.JACK),
                            new Card(Card.Suit.HEARTS, Card.Rank.QUEEN)));

    private static final PokerHand FLUSH_LOW =
            new PokerHand(
                    PokerHand.Type.FLUSH,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                            new Card(Card.Suit.HEARTS, Card.Rank.FOUR),
                            new Card(Card.Suit.HEARTS, Card.Rank.SIX),
                            new Card(Card.Suit.HEARTS, Card.Rank.EIGHT),
                            new Card(Card.Suit.HEARTS, Card.Rank.TEN)));

    private static final PokerHand FLUSH_HIGH =
            new PokerHand(
                    PokerHand.Type.FLUSH,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                            new Card(Card.Suit.HEARTS, Card.Rank.TEN),
                            new Card(Card.Suit.HEARTS, Card.Rank.QUEEN),
                            new Card(Card.Suit.HEARTS, Card.Rank.NINE),
                            new Card(Card.Suit.HEARTS, Card.Rank.TWO)));

    private static final PokerHand FULL_HOUSE_LOW =
            new PokerHand(
                    PokerHand.Type.FULL_HOUSE,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                            new Card(Card.Suit.SPADES, Card.Rank.THREE),
                            new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                            new Card(Card.Suit.SPADES, Card.Rank.TWO),
                            new Card(Card.Suit.CLUBS, Card.Rank.TWO)));


    private static final PokerHand FULL_HOUSE_HIGH =
            new PokerHand(
                    PokerHand.Type.FULL_HOUSE,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                            new Card(Card.Suit.SPADES, Card.Rank.ACE),
                            new Card(Card.Suit.HEARTS, Card.Rank.KING),
                            new Card(Card.Suit.SPADES, Card.Rank.KING),
                            new Card(Card.Suit.CLUBS, Card.Rank.KING)));

    private static final PokerHand QUAD_LOW =
            new PokerHand(
                    PokerHand.Type.QUAD,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                            new Card(Card.Suit.SPADES, Card.Rank.THREE),
                            new Card(Card.Suit.CLUBS, Card.Rank.THREE),
                            new Card(Card.Suit.DIAMONDS, Card.Rank.THREE),
                            new Card(Card.Suit.CLUBS, Card.Rank.TWO)));

    private static final PokerHand QUAD_HIGH =
            new PokerHand(
                    PokerHand.Type.QUAD,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                            new Card(Card.Suit.SPADES, Card.Rank.ACE),
                            new Card(Card.Suit.CLUBS, Card.Rank.ACE),
                            new Card(Card.Suit.DIAMONDS, Card.Rank.ACE),
                            new Card(Card.Suit.CLUBS, Card.Rank.KING)));

    private static final PokerHand STRAIGHT_FLUSH_LOW =
            new PokerHand(
                    PokerHand.Type.STRAIGHT_FLUSH,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                            new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                            new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                            new Card(Card.Suit.HEARTS, Card.Rank.FOUR),
                            new Card(Card.Suit.HEARTS, Card.Rank.FIVE)));

    private static final PokerHand STRAIGHT_FLUSH_HIGH =
            new PokerHand(
                    PokerHand.Type.STRAIGHT_FLUSH,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                            new Card(Card.Suit.HEARTS, Card.Rank.KING),
                            new Card(Card.Suit.HEARTS, Card.Rank.TEN),
                            new Card(Card.Suit.HEARTS, Card.Rank.JACK),
                            new Card(Card.Suit.HEARTS, Card.Rank.QUEEN)));

    private static final List<PokerHand> SORTED_HANDS =
            ImmutableList.of(
                    PAIR_LOW,
                    PAIR_HIGH,
                    TWO_PAIR_LOW,
                    TWO_PAIR_HIGH,
                    TRIPLE_LOW,
                    TRIPLE_HIGH,
                    STRAIGHT_LOW,
                    STRAIGHT_HIGH,
                    FLUSH_LOW,
                    FLUSH_HIGH,
                    FULL_HOUSE_LOW,
                    FULL_HOUSE_HIGH,
                    QUAD_LOW,
                    QUAD_HIGH,
                    STRAIGHT_FLUSH_LOW,
                    STRAIGHT_FLUSH_HIGH);

    @Test
    public void compareTo() {
        assertGreaterThan(PAIR_HIGH, PAIR_MID);
        assertGreaterThan(PAIR_MID, PAIR_LOW);
        assertGreaterThan(TWO_PAIR_HIGH, TWO_PAIR_MID);
        assertGreaterThan(TWO_PAIR_MID, TWO_PAIR_LOW);
        assertGreaterThan(TRIPLE_HIGH, TRIPLE_MID);
        assertGreaterThan(TRIPLE_MID, TRIPLE_LOW);
        assertGreaterThan(STRAIGHT_HIGH, STRAIGHT_LOW);
        assertGreaterThan(FLUSH_HIGH, FLUSH_LOW);
        assertGreaterThan(FULL_HOUSE_HIGH, FULL_HOUSE_LOW);
        assertGreaterThan(QUAD_HIGH, QUAD_LOW);
        assertGreaterThan(STRAIGHT_FLUSH_HIGH, STRAIGHT_FLUSH_LOW);
    }

    private void assertGreaterThan(PokerHand a, PokerHand b) {
        assertTrue(a.compareTo(b) > 0);
    }

    @Test
    public void sort() {
        final List<PokerHand> actual = Lists.newArrayList(SORTED_HANDS);
        Collections.sort(actual);
        assertEquals(actual, SORTED_HANDS);
    }
}
