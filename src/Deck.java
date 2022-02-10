
import java.util.ArrayList;
import java.util.Arrays;

/**
 * a deck class to have a normal 52 card deck
 */
public class Deck {

    /**
     * a basic un-shuffled deck array list, storing each "card" as a string with the value
     * because suit is not relevant in blackjack does not store the suit of each card
     */
    private final ArrayList<String> START_DECK = new ArrayList<>(Arrays.asList("A", "A", "A", "A", "2", "2", "2", "2", "3", "3", "3", "3", "4", "4", "4", "4", "5", "5", "5", "5", "6", "6", "6", "6", "7", "7", "7", "7", "8", "8", "8", "8", "9", "9", "9", "9", "10", "10", "10", "10", "J", "J", "J", "J", "Q", "Q", "Q", "Q", "K", "K", "K", "K"));

    /**
     * stores the current deck as an array list of strings
     */
    private ArrayList<String> currentDeck;

    /**
     * constructor for a deck
     */
    public Deck(){
        currentDeck = START_DECK; // makes the current deck the same as a normal un-shuffled 52 card deck
        shuffleDeck(); // shuffles the current deck
    }

    /**
     * shuffles current deck to be in a random order
     */
    public void shuffleDeck(){
        int deckSize = currentDeck.size();
        ArrayList<String> temp = new ArrayList<>();
        for(int i = 0; i < deckSize; i++){
            int rand = (int)(Math.round( Math.random() * (currentDeck.size()-1)));
            temp.add(currentDeck.get(rand));
            currentDeck.remove(rand);
        }
        currentDeck = temp;

    }

    /**
     * deals the top card of the deck and removes it from the deck
     * @return the top card of the deck
     */
    public String dealACard(){
        String card = currentDeck.get(0);
        currentDeck.remove(0);
        return card;
    }

    /**
     * resets current deck to a basic 52 card deck
     */
    public void resetDeck(){
        //for some reason invoking the START_DECK variable here is empty so sets it to a full new deck
        currentDeck = new ArrayList<>(Arrays.asList("A", "A", "A", "A", "2", "2", "2", "2", "3", "3", "3", "3", "4", "4", "4", "4", "5", "5", "5", "5", "6", "6", "6", "6", "7", "7", "7", "7", "8", "8", "8", "8", "9", "9", "9", "9", "10", "10", "10", "10", "J", "J", "J", "J", "Q", "Q", "Q", "Q", "K", "K", "K", "K"));
        shuffleDeck();
    }

    /**
     * scores the hand in accordance with blackjack rules
     * @param hand an array list of the cards in the hand
     * @return the score of the hand
     */
    public int score(ArrayList<String> hand){
        int score = 0;
        int numOfAces = 0;
        for(String card:hand){
            if(card.equals("10") || card.equals("J") || card.equals("Q") || card.equals("K")){
                score += 10;
            }else if(card.equals("A")){
                numOfAces++;
            }else{
                int points;
                try{
                    points = Integer.parseInt(card);
                }catch (NumberFormatException e){
                    throw new IllegalArgumentException("at least one of the cards given to the score method was not a legal card");
                }
                if(points > 9){
                    throw new IllegalArgumentException("at least one of the cards given to the score method was not a legal card");
                }
                score += points;
            }
        }
        int acesAs11 = 0;
        if(numOfAces > 0){
            if(score <= 21){
                for(int i = numOfAces; i >= 0; i--){
                    if(score + 11*i <= 21){
                        acesAs11 = i;
                        break;
                    }
                }
                if(acesAs11 != 0){
                    score += 11 * acesAs11 + numOfAces - acesAs11;
                }else{
                    score += numOfAces;
                }
            }else{
                score += numOfAces;
            }
        }

        return score;

    }

}
