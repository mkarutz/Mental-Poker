package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class PokerGameTest {
    @Mock private PeerNetwork mockPeerNetwork;
    @Mock private MentalPokerEngine mockPokerEngine;

    private final int PLAYER_ONE_STAKE = 25;
    private final int PLAYER_TWO_STAKE = 50;
    private final int PLAYER_THREE_STAKE = 100;

    private final int PLAYER_ONE_RESULT = 75;
    private final int PLAYER_TWO_RESULT = 25;
    private final int PLAYER_THREE_RESULT = 75;

    private final int NUM_PLAYERS = 3;

    private final Hand PLAYER_ONE_HAND =
            new Hand(
                    Lists.newArrayList(
                            new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                            new Card(Card.Suit.CLUBS, Card.Rank.ACE)));

    private final Hand PLAYER_TWO_HAND =
            new Hand(
                    Lists.newArrayList(
                            new Card(Card.Suit.HEARTS, Card.Rank.KING),
                            new Card(Card.Suit.CLUBS, Card.Rank.KING)));

    private final Hand PLAYER_THREE_HAND =
            new Hand(
                    Lists.newArrayList(
                            new Card(Card.Suit.SPADES, Card.Rank.KING),
                            new Card(Card.Suit.DIAMONDS, Card.Rank.KING)));

    private final Hand HIDDEN_HAND = new Hand(2, Lists.newArrayList());

    private final ImmutableList<Card> PUBLIC_CARDS =
            ImmutableList.of(
                    new Card(Card.Suit.SPADES, Card.Rank.THREE),
                    new Card(Card.Suit.DIAMONDS, Card.Rank.TWO),
                    new Card(Card.Suit.SPADES, Card.Rank.EIGHT),
                    new Card(Card.Suit.DIAMONDS, Card.Rank.SEVEN),
                    new Card(Card.Suit.SPADES, Card.Rank.QUEEN));

    private PokerGame pokerGame;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        pokerGame = new PokerGame(mockPeerNetwork, mockPokerEngine);
    }

    @Test
    public void splitPot() {
        when(mockPokerEngine.getNumPlayers()).thenReturn(NUM_PLAYERS);
        when(mockPokerEngine.getPlayerHand(1)).thenReturn(PLAYER_ONE_HAND);
        when(mockPokerEngine.getPlayerHand(2)).thenReturn(PLAYER_TWO_HAND);
        when(mockPokerEngine.getPlayerHand(3)).thenReturn(PLAYER_THREE_HAND);
        when(mockPokerEngine.getPublicCards()).thenReturn(PUBLIC_CARDS);

        pokerGame.playerInfoList =
                Lists.newArrayList(
                        new PokerGame.PlayerInfo(1, false, 0, PLAYER_ONE_STAKE),
                        new PokerGame.PlayerInfo(2, false, 0, PLAYER_TWO_STAKE),
                        new PokerGame.PlayerInfo(3, false, 0, PLAYER_THREE_STAKE));

        pokerGame.distribute();

        assertEquals(PLAYER_ONE_RESULT, pokerGame.playerInfoList.get(0).balance);
        assertEquals(PLAYER_TWO_RESULT, pokerGame.playerInfoList.get(1).balance);
        assertEquals(PLAYER_THREE_RESULT, pokerGame.playerInfoList.get(2).balance);
    }

    @Test
    public void playersFolded() {
        when(mockPokerEngine.getNumPlayers()).thenReturn(NUM_PLAYERS);
        when(mockPokerEngine.getPlayerHand(1)).thenReturn(HIDDEN_HAND);
        when(mockPokerEngine.getPlayerHand(2)).thenReturn(HIDDEN_HAND);
        when(mockPokerEngine.getPlayerHand(3)).thenReturn(HIDDEN_HAND);
        when(mockPokerEngine.getPublicCards()).thenReturn(PUBLIC_CARDS);

        pokerGame.playerInfoList =
                Lists.newArrayList(
                        new PokerGame.PlayerInfo(1, false, 0, PLAYER_ONE_STAKE),
                        new PokerGame.PlayerInfo(2, true, 0, PLAYER_TWO_STAKE),
                        new PokerGame.PlayerInfo(3, true, 0, PLAYER_THREE_STAKE));

        pokerGame.distribute();

        assertEquals(PLAYER_ONE_RESULT, pokerGame.playerInfoList.get(0).balance);
        assertEquals(PLAYER_TWO_RESULT, pokerGame.playerInfoList.get(1).balance);
        assertEquals(PLAYER_THREE_RESULT, pokerGame.playerInfoList.get(2).balance);
    }
}
