import java.util.ArrayList;
import java.util.Random;

public class Deck {
	private ArrayList<Card> cards = new ArrayList<Card>();
	
	public Deck() {
		String[] suits = new String[4];
		suits[0]="Hearts";
		suits[1]="Diamonds";
		suits[2]="Spades";
		suits[3]="Clubs";
		
		for(int i=0;i<suits.length;i++) {
			for(int j=9;j>0;j--) {
				cards.add(new Card(suits[i], ""+(j+1)));
			}
			cards.add(new Card(suits[i], "Jack"));
			cards.add(new Card(suits[i], "Queen"));
			cards.add(new Card(suits[i], "King"));
			cards.add(new Card(suits[i], "Ace"));
		}
		shuffle();
	}

	public ArrayList<Card> getCards() {
		return cards;
	}
	
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
