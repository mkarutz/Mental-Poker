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
        try {
            poker.finish();
            System.out.println("Game finished successfully.");
        } catch (CheatingDetectedException e) {
            System.out.println("Cheating detected.");
        }
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

        for (int playerId = 1; playerId <= poker.getNumPlayers(); playerId++) {
            poker.open(playerId);
            System.out.println("Player " + playerId + " had:");
            poker.getPlayerHand(playerId).display();
            System.out.println();
        }
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
