package au.edu.unimelb.mentalpoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Created by brandon on 13/05/17. */
public class PokerUtils {
    private static final int POKER_HAND_SIZE = 5;

    /** Returns a list of all possible hand combination for the given player hand. */
    public static List<List<Card>> getAllHands(List<Card> playerCards, List<Card> tableCards) {
        List<List<Card>> result = new ArrayList<>();
        for (List<Card> tableCardCombination : findCombinations(tableCards, POKER_HAND_SIZE - playerCards.size())) {
            List<Card> tempResult = new ArrayList<>();
            tempResult.addAll(playerCards);
            tempResult.addAll(tableCardCombination);
            result.add(tempResult);
        }
        return result;
    }

    /** Enumerates a list of all combination from table cards of size number. */
    private static List<List<Card>> findCombinations(List<Card> tableCards, int number) {
        List<List<Card>> finalResultTemp = new ArrayList<>();
        List<List<Card>> finalResult = new ArrayList<>();

        if (number == 0 || tableCards.isEmpty()) {
            return finalResult;
        }

        for (Card card : tableCards) {
            List<Card> tempTableCards = tableCards.subList(tableCards.indexOf(card) + 1, tableCards.size());
            List<List<Card>> tempList = findCombinations(tempTableCards, number - 1);

            if (tempList.isEmpty()) {
                List<Card> result = new ArrayList<>();
                result.add(card);
                finalResultTemp.add(result);
            } else {
                for (List<Card> subSet : tempList) {
                    List<Card> result = new ArrayList<>();
                    result.add(card);
                    result.addAll(subSet);
                    finalResultTemp.add(result);
                }
            }
        }

        for (List<Card> lc : finalResultTemp) {
            if (lc.size() == number) {
                finalResult.add(lc);
            }
        }

        return finalResult;
    }


    public static List<List<Card>> getAllPossiblePlayerHand(List<Card> playerCards, List<List<Card>> tableCardCombinations){
        List<List<Card>> finalCombinations = new ArrayList<>();
        for(List<Card> tableCombination: tableCardCombinations){
            List<Card> result = tableCombination;
            result.addAll(playerCards);
            finalCombinations.add(result);
        }
        return finalCombinations;
    }

    public static PokerHand.Type getHandType(List<Card> playerHand){
        if(isRoyalFlush(playerHand)){
            return PokerHand.Type.STRAIGHT_FLUSH;
        }
        if(isStraightFlush(playerHand)){
            return PokerHand.Type.STRAIGHT_FLUSH;
        }
        if(isQuad(playerHand)){
            return PokerHand.Type.QUAD;
        }
        if(isFullHouse(playerHand)){
            return PokerHand.Type.FULL_HOUSE;
        }
        if(isFlush(playerHand)){
            return PokerHand.Type.FLUSH;
        }
        if(isStraight(playerHand)){
            return PokerHand.Type.STRAIGHT;
        }
        if(isTriple(playerHand)){
            return PokerHand.Type.TRIPLE;
        }
        if(isDoublePair(playerHand)){
            return PokerHand.Type.TWO_PAIR;
        }
        if(isPair(playerHand)){
            return PokerHand.Type.PAIR;
        }
        return PokerHand.Type.HIGH_CARD;
    }

    public static PokerHand getBestHand(List<List<Card>> hands){
        List<PokerHand> pokerHands = new ArrayList<>();
        for(List<Card> hand:hands){
            pokerHands.add(new PokerHand(getHandType(hand),hand));
        }
        return Collections.max(pokerHands);
    }

    public static PokerHand getPlayerBestHand(List<Card> playerHand, List<Card> tableCards){
        List<List<Card>> tableCardCombination = findCombinations(tableCards,POKER_HAND_SIZE-playerHand.size());
        List<List<Card>> playerHandCombinations = getAllPossiblePlayerHand(playerHand,tableCardCombination);
        return getBestHand(playerHandCombinations);
    }

    //just to check for special case of straight
    private static boolean isRoyal(List<Card> cards){
        boolean check = true;
        Collections.sort(cards,Card.COMPARE_BY_RANK);

        //check if first card is 'Ace'
        if(cards.get(0).getRawRank() != 1)
        {
            return false;
        }

        if (cards.get(1).getRank() != Card.Rank.TEN) {
            return false;
        }

        return isStraight(cards.subList(2, cards.size()));
    }

    private static boolean isStraight(List<Card> cards) {
        if (isRoyal(cards)) {
            return true;
        }

        for (int i = 0; i < cards.size() - 1; i++) {
            if (cards.get(i).getRawRank() != cards.get(i + 1).getRawRank() - 1) {
                return false;
            }
        }

        return true;
    }

    private static boolean isFlush(List<Card> cards) {
        for (int i = 0; i < cards.size() - 1; i++) {
            if (!cards.get(i).getSuit().equals(cards.get(i + 1).getSuit())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isStraightFlush(List<Card> cards) {
        return isFlush(cards) && isStraight(cards);
    }

    private static boolean isRoyalFlush(List<Card> cards) {
        return isRoyal(cards) && isFlush(cards);
    }

    private static boolean isFullHouse(List<Card> cards) {
        if (cards.get(0).getRawRank() == cards.get(1).getRawRank()
                && cards.get(0).getRawRank() == cards.get(2).getRawRank()) {
            return isPair(cards.subList(3, cards.size()));
        }

        if (cards.get(0).getRawRank() == cards.get(1).getRawRank()) {
            return isTriple(cards.subList(2, cards.size()));
        }

        return false;
    }

    private static boolean isQuad(List<Card> cards) {
        for (int i = 0; i < cards.size() - 2; i++) {
            if (cards.get(i).getRawRank() == cards.get(i + 1).getRawRank()
                    && cards.get(i).getRawRank() == cards.get(i + 2).getRawRank()
                    && cards.get(i).getRawRank() == cards.get(i + 3).getRawRank()) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTriple(List<Card> cards) {
        for (int i = 0; i < cards.size() - 2; i++) {
            if (cards.get(i).getRawRank() == cards.get(i + 1).getRawRank()
                    && cards.get(i).getRawRank() == cards.get(i + 2).getRawRank()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPair(List<Card> cards) {
        for (int i = 0; i < cards.size() - 1; i++) {
            if (cards.get(i).getRawRank() == cards.get(i + 1).getRawRank()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDoublePair(List<Card> cards) {
        int pairCount = 0;
        int i = 0;
        while (i < cards.size() - 1) {
            if (cards.get(i).getRawRank() == cards.get(i + 1).getRawRank()) {
                pairCount++;
                i += 2;
            } else {
                i++;
            }
        }
        return pairCount == 2;
    }
}
