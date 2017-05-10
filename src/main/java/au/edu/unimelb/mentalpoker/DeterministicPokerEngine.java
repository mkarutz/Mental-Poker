package au.edu.unimelb.mentalpoker;

/**
 * An insecure, deterministic {@link MentalPokerEngine} used for testing.
 */
public class DeterministicPokerEngine implements MentalPokerEngine {


    public DeterministicPokerEngine(int numPlayers, int playerId) {
    }

    @Override
    public void init(int numPlayers, int playerId) {

    }

    @Override
    public void shuffle() {

    }

    @Override
    public void draw(int playerId) {

    }

    @Override
    public void open(int playerId, int cardId) {

    }

    @Override
    public void finish() {

    }

    @Override
    public Hand getPlayerHand(int playerId) {
        return null;
    }

    @Override
    public Hand getPublicCards() {
        return null;
    }
}
