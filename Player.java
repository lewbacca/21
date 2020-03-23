import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable{
	private ArrayList<Card> hand = new ArrayList<Card>();
	private int handValue;
	private int points;
	private int bet;
	private String name, message;
	private boolean active;
	private boolean newPlayer;
	private boolean theirTurn;
	private boolean dealer;
	private boolean doneForTheRound;
	public Player(String name) {
		this.name=name;
		points=1000;
		active=false;
		newPlayer=true;
		theirTurn=false;
		dealer=false;
		doneForTheRound=true;
		message="";
	}

	public boolean isDoneForTheRound() {
		return doneForTheRound;
	}


	public void setDoneForTheRound(boolean doneForTheRound) {
		this.doneForTheRound = doneForTheRound;
	}


	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public ArrayList<Card> getHand() {
		return hand;
	}

	public int getHandValue() {
		calculateHand();
		return handValue;
	}

	public String getName() {
		return name;
	}
	public void calculateHand() {
		int certainSum=0;
		int aceAmount=0;
		
		for(Card c: hand) {
			if(!c.getName().equals("Ace")){
				certainSum+=c.getValue();
			}else {
				aceAmount++;
			}
		}
		int aces=aceAmount;
		for(int i=0;i<aceAmount;i++) {
			if(certainSum<11 && aces==1) {
				certainSum+=11;
			}else {
				if(aces==0) {
					break;
				}
				aces--;
				certainSum++;
			}
		}
		handValue=certainSum;
		if(handValue>21){
			doneForTheRound=true;
		}
	}

	public int getBet() {
		return bet;
	}

	public void setBet(int bet) {
		this.bet = bet;
	}

	public void setActive(boolean b) {
		active=b;
		
	}

	public boolean isActive() {
		return active;
	}

	public boolean isNewPlayer() {
		return newPlayer;
	}

	public void setNewPlayer(boolean newPlayer) {
		this.newPlayer = newPlayer;
	}	
	public void uniqueName() {
		name+=this.hashCode();
		message+="The name you picked was not availabe. You are: "+ name;
	}

	public boolean isTheirTurn() {
		return theirTurn;
	}

	public void setTheirTurn(boolean theirTurn) {
		this.theirTurn = theirTurn;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isDealer(){
		return dealer;
	}
	public void setDealer(boolean dealer){
		this.dealer=dealer;
	}
	public String handToString() {
		String cards="";
		for(Card c: hand) {
			cards+=c.toString()+"\n";
		}
		return cards;
	}
}
