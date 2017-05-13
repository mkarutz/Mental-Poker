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

    private static final int LOCAL_PLAYER_ID = 0;
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

        assertEquals(0 /* expected */, poker.getPlayerHand(1).getSize());
        assertTrue(poker.getPlayerHand(1).getOpenCards().isEmpty());

        poker.draw(1);

        assertEquals(1 /* expected */, poker.getPlayerHand(1).getSize());
        assertTrue(poker.getPlayerHand(1).getOpenCards().isEmpty());
    }
}
