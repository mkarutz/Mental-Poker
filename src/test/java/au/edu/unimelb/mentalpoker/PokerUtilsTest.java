package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by brandon on 15/05/17.
 */
public class PokerUtilsTest {
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

    private static final List<Card> TWO_PAIR_MID =
            Lists.newArrayList(
                    new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                    new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                    new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                    new Card(Card.Suit.CLUBS, Card.Rank.THREE),
                    new Card(Card.Suit.HEARTS, Card.Rank.KING));

    @Test
    public void getBestHand() throws Exception {
        final List<List<Card>> hands = Lists.newArrayList(PAIR_HIGH, TWO_PAIR_LOW, TWO_PAIR_MID);
        final PokerHand expected = new PokerHand(PokerHand.Type.TWO_PAIR, TWO_PAIR_MID);
        final PokerHand actual = PokerUtils.getBestHand(hands);
        assertEquals(expected, actual);
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
                        ImmutableList.of(
                                new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                                new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                                new Card(Card.Suit.HEARTS, Card.Rank.TEN),
                                new Card(Card.Suit.HEARTS, Card.Rank.JACK),
                                new Card(Card.Suit.HEARTS, Card.Rank.THREE)));

        assertEquals(EXPECTED, PokerUtils.getPlayerBestHand(PLAYER_CARDS, PUBLIC_CARDS));
    }
}