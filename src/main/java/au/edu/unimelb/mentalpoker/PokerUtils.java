package au.edu.unimelb.mentalpoker;

import java.util.*;

/**
 * Created by brandon on 13/05/17.
 */
public class PokerUtils {

    //Find combination of tablecards and add hand
    //number represents the number of cards used from table cards -- usually 3
    public static List<List<Card>> findCombinations(List<Card> playerCards, List<Card> tableCards)
    {
        return findCombinations(playerCards,tableCards,3);
    }

    public static List<List<Card>> findCombinations(List<Card> playerCards, List<Card> tableCards, int number)
    {
        List<List<Card>> result = new ArrayList<>();
        for(List<Card> tableCardCombination: findCombinations(tableCards, number))
        {
            List<Card> tempResult = new ArrayList<>();
            tempResult.addAll(playerCards);
            tempResult.addAll(tableCardCombination);
            result.add(tempResult);
        }
        return result;
    }

    //find combination of table cards
    public static List<List<Card>> findCombinations(List<Card> tableCards, int number)
    {
        List<List<Card>> finalResultTemp = new ArrayList<>();
        List<List<Card>> finalResult = new ArrayList<>();

        if (number == 0 || tableCards.isEmpty())
        {
            return finalResult;
        }

        for(Card card: tableCards)
        {
            List<Card> tempTableCards = tableCards.subList(tableCards.indexOf(card)+1,tableCards.size());

            List<List<Card>> tempList = findCombinations(tempTableCards,number-1);

            if(tempList.isEmpty())
            {
                List<Card> result = new ArrayList<>();
                result.add(card);
                finalResultTemp.add(result);
            }
            else
            {
                for(List<Card> subSet : tempList)
                {
                    List<Card> result = new ArrayList<>();
                    result.add(card);
                    result.addAll(subSet);
                    finalResultTemp.add(result);
                }
            }
        }

        for(List<Card> lc : finalResultTemp)
        {
                if(lc.size()==number)
                {
                    finalResult.add(lc);
                }
        }
        return finalResult;
    }

    //just to check for special case of straight
    public static boolean isRoyal(List<Card> cards){
        boolean check = true;
        Collections.sort(cards,Card.CompareByRank);

        //check if first card is 'Ace'
        if(cards.get(0).getRawRank() != 1)
        {
            return false;
        }

        //check if second card is '10'
        if(cards.get(1).getRawRank() != 10)
        {
            return false;
        }

        for(int i =1 ; i<cards.size()-1;i++)
        {

            if(cards.get(i).getRawRank() != cards.get(i+1).getRawRank()-1)
            {
                check = false;
                break;
            }
        }
        return check;
    }

    public static boolean isStraight(List<Card> cards){
        boolean check = true;
        Collections.sort(cards,Card.CompareByRank);

        if(isRoyal(cards)){
            return true;
        }

        for(int i =0 ; i<cards.size()-1;i++)
        {

            if(cards.get(i).getRawRank() != cards.get(i+1).getRawRank()-1)
            {
                    check = false;
                    break;
            }
        }
        return check;
    }

    public static boolean isFlush(List<Card> cards){
        boolean check =true;

        for(int i =0 ; i<cards.size()-1;i++)
        {
            if(!cards.get(i).getSuit().equals(cards.get(i+1).getSuit()))
            {
                check = false;
                break;
            }
        }
        return check;
    }

    public static boolean isStraightFlush(List<Card> cards){
        return (isFlush(cards) && isStraight(cards));
    }

    public static boolean isRoyalFlush(List<Card> cards){
        return (isRoyal(cards) && isFlush(cards));
    }

    public static boolean isFullHouse(List<Card> cards){
        boolean check = false;
        Collections.sort(cards,Card.CompareByRank);

        int count = 0;
        for(int i =0 ; i<cards.size()-1;i++)
        {
            if(cards.get(i).getRawRank() == cards.get(i+1).getRawRank())
            {
                count++;
            }
            else
            {
                if(count == 1)
                {
                    check = isTriple(cards.subList(i+1,cards.size()));
                }
                if(count == 2)
                {
                    check =isPair(cards.subList(i+1,cards.size()));
                }
            }
        }
        return check;
    }

    public static boolean isQuad(List<Card> cards){
        boolean check = false;
        Collections.sort(cards,Card.CompareByRank);

        int count =0;
        for(int i =0 ; i<cards.size()-1;i++)
        {
            if(cards.get(i).getRawRank() == cards.get(i+1).getRawRank())
            {
                count++;
                if(count == 3)
                {
                    check = true;
                    break;
                }
            }
        }
        return check;
    }

    public static boolean isTriple(List<Card> cards){
        boolean check = false;
        Collections.sort(cards,Card.CompareByRank);

        int count =0;
        for(int i =0 ; i<cards.size()-1;i++)
        {
            if(cards.get(i).getRawRank() == cards.get(i+1).getRawRank())
            {
                count++;
                if(count == 2)
                {
                    check = true;
                    break;
                }
            }
        }
        return check;
    }

    public static boolean isPair(List<Card> cards){
        boolean check = false;
        Collections.sort(cards,Card.CompareByRank);

        for(int i =0 ; i<cards.size()-1;i++)
        {
            if(cards.get(i).getRawRank() == cards.get(i+1).getRawRank())
            {
                check = true;
                break;
            }
        }
        return check;
    }

    public static boolean isDoublePair(List<Card> cards){
        boolean check = false;
        Collections.sort(cards,Card.CompareByRank);

        int count = 0;
        for(int i =0 ; i<cards.size()-1;i++)
        {
            if(cards.get(i).getRawRank() == cards.get(i+1).getRawRank())
            {
                count++;
            }
            else
            {
                if(count == 1)
                {
                    check = isPair(cards.subList(i+1,cards.size()));
                }
            }
        }
        return check;
    }
}
