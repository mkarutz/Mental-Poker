package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;

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
                            new Card(Card.Suit.HEARTS, Card.Rank.TEN),
                            new Card(Card.Suit.HEARTS, Card.Rank.SEVEN)));

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

    private static final PokerHand TWO_PAIR_HIGH =
            new PokerHand(
                    PokerHand.Type.PAIR,
                    ImmutableList.of(
                            new Card(Card.Suit.HEARTS, Card.Rank.TWO),
                            new Card(Card.Suit.CLUBS, Card.Rank.TWO),
                            new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                            new Card(Card.Suit.HEARTS, Card.Rank.THREE),
                            new Card(Card.Suit.HEARTS, Card.Rank.SEVEN)));
}
