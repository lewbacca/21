import java.util.ArrayList;
import java.util.Random;
/**
 * Objects of this class are used to generate and access a shuffled array list of 52 cards.
 * @author 2072353l, Lyubomir Lazarov
 *
 */
public class Deck {
	private ArrayList<Card> cards = new ArrayList<Card>();
	public Deck() {
		String[] suits = new String[4];
		suits[0]="Hearts";
		suits[1]="Diamonds";
		suits[2]="Spades";
		suits[3]="Clubs";
		
		for(int i=0;i<suits.length;i++) { 
			for(int j=9;j>0;j--) { // this loop creates the number cards 
				cards.add(new Card(suits[i], ""+(j+1)));
			}
			cards.add(new Card(suits[i], "Jack"));
			cards.add(new Card(suits[i], "Queen"));
			cards.add(new Card(suits[i], "King"));
			cards.add(new Card(suits[i], "Ace"));
		}
		shuffle(); //shuffled immediately because at no point is an unshuffled deck needed
	}

	public ArrayList<Card> getCards() {
		return cards;
	}
	/**
	 * switches the positions of 1 card with another random card for every card in the deck
	 */
	public void shuffle() {
		Random rand= new Random();
		for(int i=0;i<cards.size();i++) {
			int randomNumber=rand.nextInt(cards.size());
			Card temp=cards.get(randomNumber);
			cards.set(randomNumber, cards.get(i));
			cards.set(i, temp);
		}
	}
}
