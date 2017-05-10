package au.edu.unimelb.mentalpoker;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    private ImmutableList<Card> cards;

    public void display() {
        StringBuilder sb = new StringBuilder();
        for (Card c : cards) {
            sb.append(c);
            sb.append(" ");
        }
        System.out.println(sb);
    }
}
