package au.edu.unimelb.mentalpoker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Test for {@link DeterministicPokerEngine}. */
public class DeterministicPokerEngineTest {
    @Mock private PeerNetwork mockPeerNetwork;

    private static final int LOCAL_PLAYER_ID = 1;
    private static final int OTHER_PLAYER_ID = 2;
    private static final int NUM_PLAYERS = 3;

    private DeterministicPokerEngine poker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockPeerNetwork.getLocalPlayerId()).thenReturn(LOCAL_PLAYER_ID);
        when(mockPeerNetwork.getNumPlayers()).thenReturn(NUM_PLAYERS);

        poker = new DeterministicPokerEngine(mockPeerNetwork);
    }

    @Test
    public void testLifecycle() {
        poker.init();

        List<Card> localPlayerCards = poker.getLocalPlayerCards();
        assertTrue(localPlayerCards.isEmpty());

        poker.draw(LOCAL_PLAYER_ID);

        localPlayerCards = poker.getLocalPlayerCards();
        assertTrue(!localPlayerCards.isEmpty());

        assertEquals(0 /* expected */, poker.getPlayerHand(OTHER_PLAYER_ID).getSize());
        assertTrue(poker.getPlayerHand(OTHER_PLAYER_ID).getOpenCards().isEmpty());

        poker.draw(OTHER_PLAYER_ID);

        assertEquals(1 /* expected */, poker.getPlayerHand(OTHER_PLAYER_ID).getSize());
        assertTrue(poker.getPlayerHand(OTHER_PLAYER_ID).getOpenCards().isEmpty());
//
//        poker.open(OTHER_PLAYER_ID, poker.getPlayerHand(OTHER_PLAYER_ID).getCardIds().get(0));
    }
}
