package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;

public interface MentalPokerEngine {
    /**
     * Adds a player to the table. Adding players to the table once the game has started is not supported by all
     * implementations.
     */
    void addPlayer(PlayerConnection playerConnection);

    /**
     * Removes a player from the table. Removing players from the table during gameplay is not supported by all
     * implementations.
     */
    void removePlayer(PlayerConnection playerConnection);

    /** Initialises the Mental Poker engine ready for dealing. */
    void start();

    /** Deals a secret card to the player with ID {@code playerId}. */
    void draw(int playerId);

    /** Deals a face-up card to the table. */
    void drawPublic();

    /**
     * Reveals the face value of a card. The card with ID {@code cardId} must have been previously dealt to the player
     * with ID {@code playerId} with a call to {@link MentalPokerEngine#draw(int)}.
     */
    void open(int playerId, int cardId);

    /** Completes the game. */
    void finish();

    /** Gets the list of players at the table */
    ImmutableList<PlayerConnection> getPlayers();

    /** Gets the Hand of the player with ID {@code playerId}. */
    Hand getPlayerHand(int playerId);

    /** Gets the cards dealt to the table. */
    Hand getPublicCards();
}
