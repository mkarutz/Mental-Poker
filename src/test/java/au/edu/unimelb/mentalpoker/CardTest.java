package au.edu.unimelb.mentalpoker;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CardTest {

    private static final String STANDARD_DECK_STRING =
            "2D 2C 2H 2S 3D 3C 3H 3S 4D 4C 4H 4S 5D 5C 5H 5S " +
            "6D 6C 6H 6S 7D 7C 7H 7S 8D 8C 8H 8S 9D 9C 9H 9S " +
            "TD TC TH TS JD JC JH JS QD QC QH QS KD KC KH KS " +
            "AD AC AH AS ";

    @Test
    public void displayStandardDeck() {
        List<Card> deck = Card.standardDeck();

        StringBuilder sb = new StringBuilder();
        for (Card card : deck) {
            sb.append(card);
            sb.append(" ");
        }

        String deckString = sb.toString();
        assertEquals(STANDARD_DECK_STRING, deckString);
    }
}
