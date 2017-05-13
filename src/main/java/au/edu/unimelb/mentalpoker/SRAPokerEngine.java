package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.math.BigInteger;
import java.util.*;

public class SRAPokerEngine implements MentalPokerEngine {
    private final PeerNetwork network;

    /** The un-shuffled, unencrypted list of cards used for playing the game. */
    private final ImmutableList<Card> cardList = ImmutableList.copyOf(Card.standardDeck());

    private final Map<BigInteger, Card> cardMap = new HashMap<>();

    private List<BigInteger> initialDeck;
    private List<BigInteger> deck;

    private BigInteger p;
    private SraKeyPair keyPair;
    private List<Integer> pi;

    private final List<CardInfo> cardInfoList = new ArrayList<>();

    /** Data structure representing the global view of a card. */
    private static class CardInfo {
        public static final int NO_OWNER = -1;
        public static final int PUBLIC = 0;
        public static final int BURNT = -2;

        BigInteger value;
        int ownerId;
        boolean isOpen;

        public CardInfo(BigInteger value, int ownerId, boolean isOpen) {
            this.value = value;
            this.ownerId = ownerId;
            this.isOpen = isOpen;
        }
    }

    public SRAPokerEngine(PeerNetwork network) {
        this.network = network;
    }

    @Override
    public void init() {
        // Agree on large prime
        p = BigInteger.valueOf(100012421);

        // Generate private key
        keyPair = SraKeyPair.create(p);

        // Prepare deck
        initializeDeck();
        shuffle();
    }

    private void initializeDeck() {
        List<BigInteger> cardRepresentations = new ArrayList<>(cardList.size());
        for (int i = 1; i <= cardList.size(); i++) {
            cardRepresentations.add(BigInteger.valueOf(i*i));
            cardMap.put(cardRepresentations.get(i - 1), cardList.get(i - 1));
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

        for (BigInteger card : deck) {
            cardInfoList.add(new CardInfo(card, CardInfo.NO_OWNER, false /* isOpen */));
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

    private void sendCard(BigInteger card, int playerId) {
        Proto.NetworkMessage msg =
                Proto.NetworkMessage.newBuilder()
                        .setType(Proto.NetworkMessage.Type.SRA_CARD)
                        .setSraCardMessage(
                                Proto.SraCardMessage.newBuilder()
                                        .setCard(card.toString()))
                        .build();
        network.send(playerId, msg);
    }

    private BigInteger receiveCard(int playerId) {
        return new BigInteger(network.receive(playerId).getSraCardMessage().getCard());
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
            final int j = i + random.nextInt(size - i);
            int temp = result.get(i);
            result.set(i, result.get(j));
            result.set(j, temp);
        }

        return result;
    }

    private BigInteger encrypt(BigInteger x) {
        return keyPair.encrypt(x);
    }

    private BigInteger decrypt(BigInteger c) {
        return keyPair.decrypt(c);
    }

    @Override
    public void draw(int playerId) {
        int cardId = nextCard();

        BigInteger card;
        if (getLocalPlayerId() == 1) {
            card = deck.get(cardId);
        } else {
            card = receiveCard(getLocalPlayerId() - 1);
        }

        if (getLocalPlayerId() != playerId) {
            card = decrypt(card);
        }

        if (getLocalPlayerId() == getNumPlayers()) {
            sendCard(card, playerId);
        } else {
            sendCard(card, getLocalPlayerId() + 1);
        }

        if (getLocalPlayerId() == playerId) {
            card = receiveCard(getNumPlayers());
            card = decrypt(card);
            cardInfoList.get(cardId).value = card;
        }
    }

    public int nextCard() {
        for (int i = 0; i < cardInfoList.size(); i++) {
            final CardInfo cardInfo = cardInfoList.get(i);
            if (cardInfo.ownerId == CardInfo.NO_OWNER) {
                return i;
            }
        }
        return -1;
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
        final List<Card> result = new ArrayList<>();

        for (final CardInfo cardInfo : cardInfoList) {
            if (cardInfo.ownerId == getLocalPlayerId()) {
                result.add(cardMap.get(cardInfo.value));
            }
        }

        return ImmutableList.copyOf(result);
    }

    @Override
    public int getNumPlayers() {
        return 0;
    }
}
