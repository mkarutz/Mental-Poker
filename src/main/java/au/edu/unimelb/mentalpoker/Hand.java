package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    private final List<Card> cards = new ArrayList<>();

    public int getNumClosedCards() {
        return 0;
    }

    public ImmutableList<Card> getOpenCards() {
        return ImmutableList.of();
    }

    public void display() {
        StringBuilder sb = new StringBuilder();
        for (Card c : cards) {
            sb.append(c);
            sb.append(" ");
        }
        System.out.println(sb);
    }
}
