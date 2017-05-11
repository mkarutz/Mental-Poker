package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;

/**
 * This interface defines the services provided by a mental poker protocol. {@link PokerGame} makes use of a
 * {@link MentalPokerEngine} to perform secure dealing, shuffling and handling of cards.
 */
public interface MentalPokerEngine {
    /** Initialises the Mental Poker engine ready for dealing. */
    void init();

    /** Deals a secret card to the player with ID {@code playerId}. */
    void draw(int playerId);

    /** Deals a face-up card to the table. */
    void drawPublic();

    /**
     * Reveals the face value of a card. The card with ID {@code cardId} must have been previously dealt to the player
     * with ID {@code playerId} with a call to {@link MentalPokerEngine#draw(int)}.
     */
    void open(int playerId, int cardId);

    /** Clears the hands of all players. */
    void rake();

    /** Completes the game, performing any necessary validity verification. */
    void finish();

    /** Gets the Hand of the player with ID {@code playerId}. */
    Hand getPlayerHand(int playerId);

    /** Gets the cards dealt to the table. */
    Hand getPublicCards();

    /** Gets a list of the cards in the local player's hand. */
    ImmutableList<Card> getLocalPlayerCards();
}
