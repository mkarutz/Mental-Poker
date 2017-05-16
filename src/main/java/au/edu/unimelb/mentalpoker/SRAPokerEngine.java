package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class SRAPokerEngine implements MentalPokerEngine {
    private static final int CARD_OFFSET = 123;
    private final PeerNetwork network;

    /** The un-shuffled, unencrypted list of cards used for playing the game. */
    private static final ImmutableList<Card> CARD_LIST = ImmutableList.copyOf(Card.standardDeck().subList(0, 20));

    /** Map from unencrypted card representations to Card values. */
    private final Map<BigInteger, Card> cardMap = new HashMap<>();

    /** The agreed un-shuffled deck representation. */
    private List<BigInteger> initialDeck;

    /** The shuffled deck. */
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
    public void init() throws TimeoutException {
        // Agree on large prime
//        BigInteger q = new BigInteger("16158503035655503650357438344334975980222051334857742016065172713762327569433945446598600705761456731844358980460949009747059779575245460547544076193224141560315438683650498045875098875194826053398028819192033784138396109321309878080919047169238085235290822926018152521443787945770532904303776199561965192760957166694834171210342487393282284747428088017663161029038902829665513096354230157075129296432088558362971801859230928678799175576150822952201848806616643615613562842355410104862578550863465661734839271290328348967522998634176499319107762583194718667771801067716614802322659239302476074096777926805529926544251");
        BigInteger q = new BigInteger("100012421");
        p = q.shiftLeft(1).add(BigInteger.ONE);

        // Generate private key
        keyPair = SraKeyPair.create(p);

        // Prepare deck
        initializeDeck();
        shuffle();
    }

    private void initializeDeck() {
        cardMap.clear();
        cardInfoList.clear();

        List<BigInteger> cardRepresentations = new ArrayList<>(CARD_LIST.size());
        for (int i = 0; i < CARD_LIST.size(); i++) {
            cardRepresentations.add(BigInteger.valueOf((CARD_OFFSET + i)*(CARD_OFFSET+i)));
            cardMap.put(cardRepresentations.get(i), CARD_LIST.get(i));
        }
        initialDeck = ImmutableList.copyOf(cardRepresentations);
    }

    private void shuffle() throws TimeoutException {
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

    @Override
    public int getNumCardsLeft() {
        int result = 0;
        for (CardInfo card : cardInfoList) {
            if (card.ownerId == CardInfo.NO_OWNER) {
                result++;
            }
        }
        return result;
    }

    private void broadcastDeck(List<BigInteger> deck) throws TimeoutException {
        Proto.NetworkMessage msg = buildDeckMessage(deck);
        network.broadcast(msg);
    }

    private Proto.NetworkMessage buildDeckMessage(List<BigInteger> deck) {
        return Proto.NetworkMessage.newBuilder()
                .setType(Proto.NetworkMessage.Type.SRA_DECK)
                .setSraDeckMessage(
                        Proto.SraDeckMessage.newBuilder()
                                .addAllCard(buildDeck(deck)))
                .build();
    }

    private Iterable<String> buildDeck(List<BigInteger> deck) {
        return Iterables.transform(deck, BigInteger::toString);
    }

    private void sendDeck(List<BigInteger> deck, int playerId) throws TimeoutException {
        Proto.NetworkMessage msg = buildDeckMessage(deck);
        network.send(playerId, msg);
    }

    private List<BigInteger> receiveDeck(int playerId) throws TimeoutException {
        Proto.NetworkMessage msg = network.receive(playerId);
        return deckFromMessage(msg.getSraDeckMessage());
    }

    private void sendCard(BigInteger card, int playerId) throws TimeoutException {
        Proto.NetworkMessage msg =
                Proto.NetworkMessage.newBuilder()
                        .setType(Proto.NetworkMessage.Type.SRA_CARD)
                        .setSraCardMessage(
                                Proto.SraCardMessage.newBuilder()
                                        .setCard(card.toString()))
                        .build();
        network.send(playerId, msg);
    }

    private void broadcastCard(BigInteger card) throws TimeoutException {
        Proto.NetworkMessage msg =
                Proto.NetworkMessage.newBuilder()
                        .setType(Proto.NetworkMessage.Type.SRA_CARD)
                        .setSraCardMessage(
                                Proto.SraCardMessage.newBuilder()
                                        .setCard(card.toString()))
                        .build();
        network.broadcast(msg);
    }

    private BigInteger receiveCard(int playerId) throws TimeoutException {
        Proto.NetworkMessage msg = network.receive(playerId);
        return new BigInteger(msg.getSraCardMessage().getCard());
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
    public void draw(int playerId) throws TimeoutException {
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

        cardInfoList.get(cardId).ownerId = playerId;
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
    public void drawPublic() throws TimeoutException {
        final int cardId = nextCard();

        BigInteger card;
        if (getLocalPlayerId() == 1) {
            card = deck.get(cardId);
        } else {
            card = receiveCard(getLocalPlayerId() - 1);
        }

        card = decrypt(card);

        if (getLocalPlayerId() == getNumPlayers()) {
            broadcastCard(card);
        } else {
            sendCard(card, getLocalPlayerId() + 1);
            card = receiveCard(getNumPlayers());
        }

        cardInfoList.get(cardId).ownerId = CardInfo.PUBLIC;
        cardInfoList.get(cardId).value = card;
        cardInfoList.get(cardId).isOpen = true;
    }

    @Override
    public void open(int playerId) throws TimeoutException {
        for (int i = 0; i < cardInfoList.size(); i++) {
            final CardInfo cardInfo = cardInfoList.get(i);
            if (cardInfo.ownerId != playerId) {
                continue;
            }
            if (cardInfo.isOpen) {
                continue;
            }

            if (getLocalPlayerId() == playerId) {
                broadcastCard(cardInfo.value);
            } else {
                cardInfo.value = receiveCard(playerId);
            }

            cardInfo.isOpen = true;
        }
    }

    @Override
    public void rake() {
        for (CardInfo cardInfo : cardInfoList) {
            if (cardInfo.ownerId == CardInfo.NO_OWNER) {
                continue;
            }
            cardInfo.ownerId = CardInfo.BURNT;
        }
    }

    @Override
    public void finish() throws CheatingDetectedException, TimeoutException {
        broadcastKey();

        List<BigInteger> keys = receiveKeys();

        for (int i = 0; i < cardInfoList.size(); i++) {
            final CardInfo cardInfo = cardInfoList.get(i);
            if (cardInfo.ownerId == CardInfo.NO_OWNER) {
                // No need to check un-dealt cards
                continue;
            }
            if (cardInfo.ownerId == getLocalPlayerId()) {
                // No need to check my own cards
                continue;
            }
            if (!cardInfo.isOpen) {
                // No need to check cards that were never revealed
                continue;
            }

            final BigInteger claimedValue = cardInfo.value;
            BigInteger value = deck.get(i);
            for (BigInteger key : keys) {
                SraKeyPair keyPair = SraKeyPair.create(p, key);
                value = keyPair.decrypt(value);
            }

            if (!value.equals(claimedValue)) {
                throw new CheatingDetectedException();
            }
        }
    }

    private void broadcastKey() throws TimeoutException {
        Proto.NetworkMessage msg =
                Proto.NetworkMessage.newBuilder()
                        .setType(Proto.NetworkMessage.Type.SRA_SECRET)
                        .setSraSecretMessage(
                                Proto.SraSecretMessage.newBuilder().setSecret(keyPair.getSecret().toString()))
                        .build();
        network.broadcast(msg);
    }

    private List<BigInteger> receiveKeys() throws TimeoutException {
        List<BigInteger> result = new ArrayList<>(getNumPlayers());
        for (int playerId = 1; playerId <= getNumPlayers(); playerId++) {
            if (playerId == getLocalPlayerId()) {
                result.add(keyPair.getSecret());
            } else {
                result.add(receiveKey(playerId));
            }
        }
        return result;
    }

    private BigInteger receiveKey(int playerId) throws TimeoutException {
        return new BigInteger(network.receive(playerId).getSraSecretMessage().getSecret());
    }

    @Override
    public Hand getPlayerHand(int playerId) {
        final List<Card> openCards = new ArrayList<>();
        int size = 0;

        for (int i = 0; i < cardInfoList.size(); i++) {
            final CardInfo cardInfo = cardInfoList.get(i);

            if (cardInfo.ownerId != playerId) {
                continue;
            }

            if (cardInfo.isOpen) {
                openCards.add(cardMap.get(cardInfo.value));
            }

            size++;
        }

        return new Hand(size, openCards);
    }

    @Override
    public ImmutableList<Card> getPublicCards() {
        return getPlayerHand(CardInfo.PUBLIC).getOpenCards();
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
        return network.getNumPlayers();
    }
}
