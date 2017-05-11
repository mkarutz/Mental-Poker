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
        for (int playerId = 1; playerId <= network.getNumPlayers(); playerId++) {
            for (int i = 0; i < CARDS_PER_HAND; i++) {
                poker.draw(playerId);
            }
        }

        // Print my cards
        displayCards(poker.getLocalPlayerCards());
    }

    private void displayCards(Iterable<Card> cards) {
        final StringBuilder sb = new StringBuilder("Your hand: ");
        for (Card card : cards) {
            sb.append(card);
            sb.append(" ");
        }
        System.out.println(sb);
    }
}
