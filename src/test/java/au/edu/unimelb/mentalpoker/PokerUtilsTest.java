package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class PokerUtilsTest {
    private static final List<Card> PAIR_LOW =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                    new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                    new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                    new Card(Card.Suit.HEARTS, Card.Rank.FOUR),
                    new Card(Card.Suit.HEARTS, Card.Rank.FIVE));

    private static final List<Card> PAIR_HIGH =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.KING),
                    new Card(Card.Suit.CLUBS, Card.Rank.KING),
                    new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                    new Card(Card.Suit.HEARTS, Card.Rank.TEN),
                    new Card(Card.Suit.HEARTS, Card.Rank.SEVEN));

    private static final List<Card> TWO_PAIR_LOW =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                    new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                    new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                    new Card(Card.Suit.CLUBS, Card.Rank.THREE),
                    new Card(Card.Suit.HEARTS, Card.Rank.SEVEN));

    private static final List<Card> TWO_PAIR_HIGH =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                    new Card(Card.Suit.CLUBS, Card.Rank.ACE),
                    new Card(Card.Suit.HEARTS, Card.Rank.KING),
                    new Card(Card.Suit.SPADES, Card.Rank.KING),
                    new Card(Card.Suit.HEARTS, Card.Rank.QUEEN));

    private static final List<Card> TRIPLE_LOW =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                    new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                    new Card(Card.Suit.SPADES, Card.Rank.TWO),
                    new Card(Card.Suit.CLUBS, Card.Rank.THREE),
                    new Card(Card.Suit.HEARTS, Card.Rank.SEVEN));

    private static final List<Card> TRIPLE_HIGH =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                    new Card(Card.Suit.CLUBS, Card.Rank.ACE),
                    new Card(Card.Suit.SPADES, Card.Rank.ACE),
                    new Card(Card.Suit.SPADES, Card.Rank.KING),
                    new Card(Card.Suit.HEARTS, Card.Rank.QUEEN));

    private static final List<Card> STRAIGHT_LOW =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                    new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                    new Card(Card.Suit.SPADES, Card.Rank.THREE),
                    new Card(Card.Suit.CLUBS, Card.Rank.FOUR),
                    new Card(Card.Suit.HEARTS, Card.Rank.FIVE));


    private static final List<Card> STRAIGHT_HIGH =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                    new Card(Card.Suit.CLUBS, Card.Rank.KING),
                    new Card(Card.Suit.SPADES, Card.Rank.TEN),
                    new Card(Card.Suit.SPADES, Card.Rank.JACK),
                    new Card(Card.Suit.HEARTS, Card.Rank.QUEEN));

    private static final List<Card> FLUSH_LOW =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                    new Card(Card.Suit.HEARTS, Card.Rank.FOUR),
                    new Card(Card.Suit.HEARTS, Card.Rank.SIX),
                    new Card(Card.Suit.HEARTS, Card.Rank.EIGHT),
                    new Card(Card.Suit.HEARTS, Card.Rank.TEN));

    private static final List<Card> FLUSH_HIGH =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                    new Card(Card.Suit.HEARTS, Card.Rank.TEN),
                    new Card(Card.Suit.HEARTS, Card.Rank.QUEEN),
                    new Card(Card.Suit.HEARTS, Card.Rank.NINE),
                    new Card(Card.Suit.HEARTS, Card.Rank.TWO));

    private static final List<Card> FULL_HOUSE_LOW =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                    new Card(Card.Suit.SPADES, Card.Rank.THREE),
                    new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                    new Card(Card.Suit.SPADES, Card.Rank.TWO),
                    new Card(Card.Suit.CLUBS, Card.Rank.TWO));


    private static final List<Card> FULL_HOUSE_HIGH =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                    new Card(Card.Suit.SPADES, Card.Rank.ACE),
                    new Card(Card.Suit.HEARTS, Card.Rank.KING),
                    new Card(Card.Suit.SPADES, Card.Rank.KING),
                    new Card(Card.Suit.CLUBS, Card.Rank.KING));

    private static final List<Card> QUAD_LOW =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                    new Card(Card.Suit.SPADES, Card.Rank.THREE),
                    new Card(Card.Suit.CLUBS, Card.Rank.THREE),
                    new Card(Card.Suit.DIAMONDS, Card.Rank.THREE),
                    new Card(Card.Suit.CLUBS, Card.Rank.TWO));


    private static final List<Card> QUAD_HIGH =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                    new Card(Card.Suit.SPADES, Card.Rank.ACE),
                    new Card(Card.Suit.CLUBS, Card.Rank.ACE),
                    new Card(Card.Suit.DIAMONDS, Card.Rank.ACE),
                    new Card(Card.Suit.CLUBS, Card.Rank.KING));

    private static final List<Card> STRAIGHT_FLUSH_LOW =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                    new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                    new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                    new Card(Card.Suit.HEARTS, Card.Rank.FOUR),
                    new Card(Card.Suit.HEARTS, Card.Rank.FIVE));

    private static final List<Card> STRAIGHT_FLUSH_HIGH =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                    new Card(Card.Suit.HEARTS, Card.Rank.KING),
                    new Card(Card.Suit.HEARTS, Card.Rank.TEN),
                    new Card(Card.Suit.HEARTS, Card.Rank.JACK),
                    new Card(Card.Suit.HEARTS, Card.Rank.QUEEN));

    private static final List<List<Card>> HANDS =
            Lists.newArrayList(
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
    public void getBestHand() throws Exception {
        final PokerHand actual = PokerUtils.getBestHand(HANDS);
        assertEquals(new PokerHand(PokerHand.Type.STRAIGHT_FLUSH, STRAIGHT_FLUSH_HIGH), actual);
    }

    @Test
    public void getPlayerBestHand() throws Exception {
        final List<Card> PLAYER_CARDS =
                Lists.newArrayList(
                        new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                        new Card(Card.Suit.HEARTS, Card.Rank.TWO));

        final List<Card> PUBLIC_CARDS =
                Lists.newArrayList(
                        new Card(Card.Suit.HEARTS, Card.Rank.TEN),
                        new Card(Card.Suit.HEARTS, Card.Rank.JACK),
                        new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                        new Card(Card.Suit.CLUBS, Card.Rank.FOUR),
                        new Card(Card.Suit.CLUBS, Card.Rank.FIVE));

        final PokerHand EXPECTED =
                new PokerHand(
                        PokerHand.Type.FLUSH,
                        Lists.newArrayList(
                                new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                                new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                                new Card(Card.Suit.HEARTS, Card.Rank.TEN),
                                new Card(Card.Suit.HEARTS, Card.Rank.JACK),
                                new Card(Card.Suit.HEARTS, Card.Rank.THREE)));

        assertEquals(EXPECTED, PokerUtils.getPlayerBestHand(PLAYER_CARDS, PUBLIC_CARDS));
    }
}
