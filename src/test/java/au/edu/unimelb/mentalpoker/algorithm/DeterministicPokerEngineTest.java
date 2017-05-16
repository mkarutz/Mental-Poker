package au.edu.unimelb.mentalpoker.algorithm;

import au.edu.unimelb.mentalpoker.algorithm.DeterministicPokerEngine;
import au.edu.unimelb.mentalpoker.net.PeerNetwork;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

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
        testInit();
        testDraw();
        testDrawPublic();
        testOpen();
        testRake();
    }

    private void testInit() {
        poker.init();
    }

    private void testDraw() {
        assertTrue(poker.getLocalPlayerCards().isEmpty());
        assertTrue(poker.getPlayerHand(OTHER_PLAYER_ID).getOpenCards().isEmpty());
        assertEquals(0, poker.getPlayerHand(OTHER_PLAYER_ID).getSize());

        poker.draw(LOCAL_PLAYER_ID);

        assertFalse(poker.getLocalPlayerCards().isEmpty());
        assertEquals(0, poker.getPlayerHand(OTHER_PLAYER_ID).getSize());

        poker.draw(OTHER_PLAYER_ID);

        assertEquals(1, poker.getPlayerHand(OTHER_PLAYER_ID).getSize());
        assertTrue(poker.getPlayerHand(OTHER_PLAYER_ID).getOpenCards().isEmpty());
    }

    private void testDrawPublic() {
        assertTrue(poker.getPublicCards().isEmpty());
        poker.drawPublic();
        assertFalse(poker.getPublicCards().isEmpty());
    }

    private void testOpen() {
        assertTrue(poker.getPlayerHand(OTHER_PLAYER_ID).getOpenCards().isEmpty());
        poker.open(OTHER_PLAYER_ID);
        assertFalse(poker.getPlayerHand(OTHER_PLAYER_ID).getOpenCards().isEmpty());
    }

    private void testRake() {
        poker.rake();
        assertEquals(0, poker.getPlayerHand(OTHER_PLAYER_ID).getSize());
        assertEquals(0, poker.getPlayerHand(LOCAL_PLAYER_ID).getSize());
        assertTrue(poker.getLocalPlayerCards().isEmpty());
    }
}
