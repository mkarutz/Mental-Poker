package au.edu.unimelb.mentalpoker;

public class PokerGame {
    private static final int CARDS_PER_HAND = 2;

    private final int myPlayerId;
    private final int numPlayers;
    private final PlayerPeer[] players;
    private final MentalPokerEngine poker;

    public PokerGame(int myPlayerId, int numPlayers, PlayerPeer[] players, MentalPokerEngine poker) {
        this.myPlayerId = myPlayerId;
        this.numPlayers = numPlayers;
        this.players = players;
        this.poker = poker;
    }

    public void start() {
        poker.init();
        poker.shuffle();
        playHand();
        poker.finish();
    }

    private void playHand() {
        // Deal hands
        for (int playerId = 0; playerId < numPlayers; playerId++) {
            for (int i = 0; i < CARDS_PER_HAND; i++) {
                poker.draw(playerId);
            }
        }

        // Print my cards
        poker.getMyHand().display();
    }
}
