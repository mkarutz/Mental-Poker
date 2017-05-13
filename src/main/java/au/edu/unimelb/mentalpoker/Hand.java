package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Hand {
    private final int size;
    private final ImmutableList<Card> openCards;

    /** Identifiers of all the cards in the hand. A card ID is an index into the prepared deck. */
    private final ImmutableList<Integer> cardIds;

    public Hand(int size, List<Card> openCards) {
        this.size = size;
        this.openCards = ImmutableList.copyOf(openCards);
    }

    public int getSize() {
        return size;
    }

    public int getNumClosedCards() {
        return size - openCards.size();
    }

    public ImmutableList<Integer> getCardIds() {
        return cardIds;
    }

    public ImmutableList<Card> getOpenCards() {
        return openCards;
    }

    public void display() {
        StringBuilder sb = new StringBuilder();
        for (Card c : openCards) {
            sb.append(c);
            sb.append(" ");
        }
        System.out.println(sb);
    }
}
