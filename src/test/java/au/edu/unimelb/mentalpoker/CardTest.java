package au.edu.unimelb.mentalpoker;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CardTest {

    private static final String STANDARD_DECK_STRING =
            "AS 2S 3S 4S 5S 6S 7S 8S 9S 10S JS QS KS "
            + "AD 2D 3D 4D 5D 6D 7D 8D 9D 10D JD QD KD "
            + "AC 2C 3C 4C 5C 6C 7C 8C 9C 10C JC QC KC "
            + "AH 2H 3H 4H 5H 6H 7H 8H 9H 10H JH QH KH ";

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
