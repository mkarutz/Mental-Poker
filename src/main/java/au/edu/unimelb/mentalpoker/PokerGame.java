package au.edu.unimelb.mentalpoker;

public class PokerGame extends Thread {
    private static final int CARDS_PER_HAND = 2;

    private final MentalPokerEngine poker;
    private final PeerNetwork network;

    public PokerGame(PeerNetwork network, MentalPokerEngine poker) {
        this.network = network;
        this.poker = poker;
    }

    @Override
    public void run() {
        poker.init();
        playHand();
        poker.finish();
    }

    private void playHand() {
        // Deal hands
        for (int playerId = 1; playerId <= poker.getNumPlayers(); playerId++) {
            for (int i = 0; i < CARDS_PER_HAND; i++) {
                poker.draw(playerId);
            }
        }

        // Print my cards
        displayHand();
    }

    private void displayHand() {
        final StringBuilder sb = new StringBuilder("Your hand: ");
        for (Card card : poker.getLocalPlayerCards()) {
            sb.append(card);
            sb.append(" ");
        }
        System.out.println(sb);
    }
}
