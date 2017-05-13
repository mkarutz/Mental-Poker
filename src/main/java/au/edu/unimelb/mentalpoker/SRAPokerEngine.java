package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SRAPokerEngine implements MentalPokerEngine {
    private final PeerNetwork network;

    /** The un-shuffled, unencrypted list of cards used for playing the game. */
    private final ImmutableList<Card> cardList = ImmutableList.copyOf(Card.standardDeck());

    private List<BigInteger> initialDeck;
    private List<BigInteger> deck;

    private BigInteger p;
    private BigInteger k;
    private BigInteger d;
    private List<Integer> pi;

    public SRAPokerEngine(PeerNetwork network) {
        this.network = network;
    }

    @Override
    public void init() {
        p = BigInteger.valueOf(100012421);
        k = BigInteger.valueOf(network.getLocalPlayerId());
        d = k.modInverse(p.subtract(BigInteger.ONE));
        initializeDeck();
        shuffle();
    }

    private void initializeDeck() {
        List<BigInteger> cardRepresentations = new ArrayList<>(cardList.size());
        for (int i = 0; i < cardList.size(); i++) {
            cardRepresentations.add(BigInteger.valueOf(i));
        }
        initialDeck = ImmutableList.copyOf(cardRepresentations);
    }

    private void shuffle() {
        List<BigInteger> prevDeck;

        // Get previous deck
        if (getLocalPlayerId() == 1) {
            prevDeck = initialDeck;
        } else {
            prevDeck = receiveDeck(getLocalPlayerId() - 1);
        }

        // Shuffle and encrypt the deck
        deck = encryptPermutation(prevDeck);

        // If we are not the last player, pass it on and wait to receive the final deck.
        if (getLocalPlayerId() == getNumPlayers()) {
            broadcastDeck(deck);
        } else {
            sendDeck(deck, getLocalPlayerId() + 1);
            deck = receiveDeck(getNumPlayers());
        }
    }

    private int getLocalPlayerId() {
        return network.getLocalPlayerId();
    }

    private void broadcastDeck(List<BigInteger> deck) {
        Proto.NetworkMessage msg = buildDeckMessage();
        network.broadcast(msg);
    }

    private Proto.NetworkMessage buildDeckMessage() {
        return Proto.NetworkMessage.newBuilder()
                .setType(Proto.NetworkMessage.Type.SRA_DECK)
                .setSraDeckMessage(
                        Proto.SraDeckMessage.newBuilder()
                                .addAllCard(buildDeck()))
                .build();
    }

    private Iterable<String> buildDeck() {
        return Iterables.transform(deck, BigInteger::toString);
    }

    private void sendDeck(List<BigInteger> deck, int playerId) {
        Proto.NetworkMessage msg = buildDeckMessage();
        network.send(playerId, msg);
    }

    private List<BigInteger> receiveDeck(int playerId) {
        Proto.NetworkMessage msg = network.receive(playerId);
        return deckFromMessage(msg.getSraDeckMessage());
    }

    private List<BigInteger> deckFromMessage(Proto.SraDeckMessage msg) {
        List<BigInteger> result = new ArrayList<>(msg.getCardCount());
        for (String card : msg.getCardList()) {
            result.add(new BigInteger(card));
        }
        return result;
    }

    private List<BigInteger> encryptPermutation(List<BigInteger> deck) {
        final List<BigInteger> result = new ArrayList<>(deck);

        pi = permutation(deck.size());

        for (int i = 0; i < result.size(); i++) {
            result.set(i, encrypt(deck.get(pi.get(i))));
        }

        return result;
    }

    private List<Integer> permutation(int size) {
        final List<Integer> result = new ArrayList<>(size);
        final Random random = new Random();

        for (int i = 0; i < size; i++) {
            result.add(i);
        }

        for (int i = 0; i < size; i++) {
            final int j = i + random.nextInt(size);
            int temp = result.get(i);
            result.set(i, result.get(j));
            result.set(j, temp);
        }

        return result;
    }

    private BigInteger encrypt(BigInteger x) {
        return x.modPow(k, p);
    }

    private BigInteger decrypt(BigInteger c) {
        return c.modPow(d, p);
    }

    @Override
    public void draw(int playerId) {
    }

    @Override
    public void drawPublic() {

    }

    @Override
    public void open(int playerId) {

    }

    @Override
    public void rake() {

    }

    @Override
    public void finish() {

    }

    @Override
    public Hand getPlayerHand(int playerId) {
        return null;
    }

    @Override
    public ImmutableList<Card> getPublicCards() {
        return null;
    }

    @Override
    public ImmutableList<Card> getLocalPlayerCards() {
        return null;
    }

    @Override
    public int getNumPlayers() {
        return 0;
    }
}
