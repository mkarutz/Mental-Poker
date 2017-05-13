package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * An insecure, deterministic {@link MentalPokerEngine} used for testing.
 */
public class DeterministicPokerEngine implements MentalPokerEngine {
    /** The un-shuffled, unencrypted list of cards used for playing the game. */
    private final ImmutableList<Card> cardList = ImmutableList.copyOf(Card.standardDeck());

    /** Deck representation. */
    private final List<CardInfo> cardInfoList = new ArrayList<>();

    private final PeerNetwork peerNetwork;

    /** Data structure representing the global view of a card. */
    private static class CardInfo {
        public static final int NO_OWNER = -1;
        public static final int PUBLIC = 0;
        public static final int BURNT = -2;

        int ownerId;
        boolean isOpen;

        public CardInfo(int ownerId, boolean isOpen) {
            this.ownerId = ownerId;
            this.isOpen = isOpen;
        }
    }

    public DeterministicPokerEngine(PeerNetwork peerNetwork) {
        this.peerNetwork = peerNetwork;
    }

    @Override
    public void init() {
        for (Card card : cardList) {
            cardInfoList.add(new CardInfo(CardInfo.NO_OWNER, false /* isOpen */));
        }
    }

    @Override
    public void draw(int playerId) {
        _draw(playerId);
    }

    private int _draw(int playerId) {
        for (int i = 0; i < cardInfoList.size(); i++) {
            final CardInfo cardInfo = cardInfoList.get(i);
            if (cardInfo.ownerId == CardInfo.NO_OWNER) {
                cardInfo.ownerId = playerId;
                return i;
            }
        }
        throw new RuntimeException();
    }

    @Override
    public void drawPublic() {
        final int playerId = CardInfo.PUBLIC;
        final int cardId = _draw(playerId);
        open(playerId, cardId);
    }

    @Override
    public void open(int playerId, int cardId) {
        final CardInfo cardInfo = cardInfoList.get(cardId);
        if (cardInfo.ownerId != playerId) {
            throw new IllegalArgumentException("The card to be opened must have been previously dealt to the player.");
        }
        cardInfo.isOpen = true;
    }

    @Override
    public void rake() {
        for (CardInfo cardInfo : cardInfoList) {
            cardInfo.ownerId = CardInfo.BURNT;
        }
    }

    @Override
    public void finish() {
        // Nothing to do.
    }

    @Override
    public Hand getPlayerHand(int playerId) {
        final List<Integer> cardIds = new ArrayList<>();
        final List<Card> openCards = new ArrayList<>();
        int size = 0;

        for (int i = 0; i < cardInfoList.size(); i++) {
            final CardInfo cardInfo = cardInfoList.get(i);

            if (cardInfo.ownerId != playerId) {
                continue;
            }

            if (cardInfo.isOpen) {
                openCards.add(cardList.get(i));
            }

            cardIds.add(i + 1 /* Card identifiers start from 1 */);
            size++;
        }

        return new Hand(size, cardIds, openCards);
    }

    @Override
    public Hand getPublicCards() {
        return getPlayerHand(CardInfo.PUBLIC);
    }

    @Override
    public ImmutableList<Card> getLocalPlayerCards() {
        final List<Card> result = new ArrayList<>();

        for (int i = 0; i < cardInfoList.size(); i++) {
            final CardInfo cardInfo = cardInfoList.get(i);

            if (cardInfo.ownerId == getLocalPlayerId()) {
                result.add(cardList.get(i));
            }
        }

        return ImmutableList.copyOf(result);
    }

    @Override
    public int getNumPlayers() {
        return peerNetwork.getNumPlayers();
    }

    private int getLocalPlayerId() {
        return peerNetwork.getLocalPlayerId();
    }
}
