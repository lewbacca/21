import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;

public class Model{
	private ArrayList<Player> players= new ArrayList<Player>();
	private Deck deck;
	private boolean roundStarted;
	private ArrayList<Player> waiting= new ArrayList<Player>();
	public Model(){
		deck=new Deck();
		roundStarted=false;
	}
	public void addPlayer(Player player) {
		if(!roundStarted) {
			players.add(player);
		}else {
			waiting.add(player);
		}
	}
	public void assignDealer() {
		Random rand = new Random();
		int randomNumber=rand.nextInt(players.size()); 
		players.get(randomNumber).setDealer(true);//this is supposed to be done by someone getting an ace first, but it's the same chances as this
	}
	public void clearTable() { 
		
		if(!checkLosers()) {
			assignDealer();
		}
		deck=new Deck();
		for(Player p: players) {
			p.getHand().clear();
			p.setBet(0);
			p.setTheirTurn(false);
			p.setDoneForTheRound(true);
			p.setMessage("NEW ROUND");
			if(p.isDealer()) {
				p.setDoneForTheRound(false); // the dealer needs to be set to not done for the round, because he can't bet in order to become in turn again 
				deal(p);
			}
		}
		
		ArrayList<Player> toBeRemoved= new ArrayList<Player>();
		for(int i=0;i<waiting.size();i++) {
			if(players.size()<5) {
				players.add(waiting.get(i));
				toBeRemoved.add(waiting.get(i));
			}else {
				break;
			}
		}
		for(Player p: toBeRemoved) {
			waiting.remove(p);
		}
	}
	public void hit(Player player) {
		player.getHand().add(deck.getCards().get(0));
		System.out.println("The card that hit is: "+ deck.getCards().get(0).toString());
		deck.getCards().remove(0);
		player.calculateHand();
	}
	public void deal(Player p) {
		for(int i=0;i<2;i++) {
			p.getHand().add(deck.getCards().get(0));
			deck.getCards().remove(0);
		}
		p.calculateHand();
	}
	public void calculateValues() {
		for(Player p: players) {
			p.calculateHand();
		}
	}
	
	public boolean checkLosers() {
		boolean dealerHere=true;
		ArrayList<Player> toBeRemoved=new ArrayList<Player>();
		for(Player p:players) {
			if(p.getPoints()<=0) {
				if(p.isDealer()) {
					dealerHere=false;
				}
				toBeRemoved.add(p);
			}
		}
		for(Player p: toBeRemoved) {
			players.remove(p);
		}
		return dealerHere;
	}
	public ArrayList<Player> getPlayers() {
		return players;
	}
	public Deck getDeck() {
		return deck;
	}
	public boolean isRoundStarted() {
		return roundStarted;
	}
	public void setRoundStarted(boolean roundStarted) {
		this.roundStarted = roundStarted;
	}
	public void giveTurn() {
		if(!roundStarted) { //if the round has not started, make the player next to dealer the one who has the turn
			boolean someoneInTurn=false;
			boolean dealerPresent=false;
			int theirIndex=0;
			int dealerIndex=0;
			
			for(int i=0;i<players.size();i++) {
				if(players.get(i).isTheirTurn()) {
					someoneInTurn=true;
					theirIndex=i;
				}else if(players.get(i).isDealer()) {
					dealerIndex=i;
					dealerPresent=true;
				}
			}
			if(someoneInTurn){
				ListIterator<Player> l=players.listIterator(theirIndex);
				l.next();
				boolean validNext=false;
				while(!validNext) {
					if(l.hasNext()) {
						if(!players.get(l.nextIndex()).isDealer() && players.get(l.nextIndex()).getBet()==0 && !players.get(l.nextIndex()).isTheirTurn()) {
							System.out.println("Thinks this is the guy: " + players.get(l.nextIndex()).getName());
							l.next().setTheirTurn(true);
							validNext=true;
						}else {
							l.next();
						}
					}else {
						l=players.listIterator(0);
					}
				}
				players.get(theirIndex).setTheirTurn(false);
			}else if(dealerPresent && !someoneInTurn){
				if(dealerIndex+1<players.size()) {
						players.get(dealerIndex+1).setTheirTurn(true);
						System.out.println("The guy at the end of the list is in TURN.");
				}else {
						players.get(0).setTheirTurn(true);
						System.out.println("The guy at the start of the list is in TURN.");
				}
			}
		}else{ //if the round has started
			for(int i=0;i<players.size();i++) { 
				if(players.get(i).isTheirTurn() && players.get(i).isDoneForTheRound()){ //only moves on when the player is  
					boolean validNext=false;
					ListIterator<Player> l=players.listIterator(i);
					l.next(); //moves the cursor of the iterator past the player, who currently has the turn 
					while(!validNext) { //a loop to find an eligible player to play next
						if(l.hasNext()) { //if this is not the end of the list
								l.next().setTheirTurn(true); 
								validNext=true; //to exit the loop

						}else{
							l=players.listIterator(0); //reset to the beginning of the list, because the iterator is at the end
						}
							
						
					}
					players.get(i).setTheirTurn(false);

					break;
				}
			}
		}
	}
	public void firstToHit() {
		int dealersIndex=0;
		for(int i=0;i<players.size();i++) {
			players.get(i).setTheirTurn(false);
			if (players.get(i).isDealer()) {
				dealersIndex=i;
			}
		}
		if(dealersIndex+1<players.size()) {
			players.get(dealersIndex+1).setTheirTurn(true);
		}else {
			players.get(0).setTheirTurn(true);
		}
	}
	public void roundEnd() { // a method that checks everyones scores when no one had a blackjack in the beginning
		Player dealer = null;
		for(Player p: players) {
			if(p.isDealer()) {
				dealer=p;
			}
		}
		calculateValues();
		for(Player p: players) {
			if(!p.isDealer()) {
				if(p.getHandValue()<22 && dealer.getHandValue()<22) {
					if (p.getHandValue()>dealer.getHandValue()) {
						p.setPoints(p.getPoints()+p.getBet()*2);
						dealer.setPoints(dealer.getPoints()-p.getBet());
						p.setMessage("YOU WIN.");
					}else if(p.getHandValue()==dealer.getHandValue()) {
						p.setPoints(p.getPoints()+p.getBet());
						p.setMessage("PUSH");
					}else {
						dealer.setPoints(dealer.getPoints()+p.getBet());
						p.setMessage("YOU LOSE");
					}
				}else if(p.getHandValue()>21 && dealer.getHandValue()>21) {
					p.setPoints(p.getPoints()+p.getBet());
					p.setMessage("PUSH");
				}else if(p.getHandValue()>21){
					dealer.setPoints(dealer.getPoints()+p.getBet());
					p.setMessage("BUST");
				}else if(dealer.getHandValue()>21) {
					p.setPoints(p.getPoints()+p.getBet()*2);
					dealer.setPoints(dealer.getPoints()-p.getBet());
					p.setMessage("DEALER BUST");
				}
			}
		}
	}
	public ArrayList<Player> getWaiting() {
		return waiting;
	}
	
}
