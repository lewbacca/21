import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;
/**
 * This is the model part of the MVC architecture.
 * @author 2072353l, Lyubomir Lazarov
 *
 */
public class Model{
	private ArrayList<Player> players= new ArrayList<Player>();
	private Deck deck;
	private boolean roundStarted;
	private ArrayList<Player> waiting= new ArrayList<Player>();
	private boolean tableCleared;
	public Model(){ //constructor
		deck=new Deck();
		roundStarted=false;
		tableCleared=true;
	}
	/**
	 * adds the player to the right list by checking if it's possible to add him to the current game
	 * @param player - client's player object that just came in to the server
	 */
	public void addPlayer(Player player) {
		
		if(tableCleared && !roundStarted) { //if a round has just ended and we are in the betting stage of the new one
			players.add(player);
		}else {
			waiting.add(player);
			player.setMessage("TABLE IS FULL.");
		}
	}
	/*
	 * set someone as the dealer - traditionally, done by dealing until someone gets an ace, but this is the same chances
	 * @return - the player who became dealer
	 */
	public Player assignDealer() {
		Random rand = new Random();
		int randomNumber=rand.nextInt(players.size()); 
		Player dealer=players.get(randomNumber);
		dealer.setDealer(true);
		return dealer;
	}
	/**
	 * resets everything for a new round 
	 */
	public void clearTable() { 
		
		if(!checkLosers()) { //removes the players with no points, returns false if removed the dealer too
			assignDealer();
		}
		deck=new Deck(); //refresh the deck
		for(Player p: players) {
			p.getHand().clear();
			p.setBet(0);
			p.setTheirTurn(false); 
			p.setDoneForTheRound(true); //not standing or bust 
			p.setMessage("NEW ROUND");
			if(p.isDealer()) {
				p.setDoneForTheRound(false); // the dealer needs to be set to not done for the round, because he can't bet in order to become in turn again 
				deal(p); //since the dealer does not bet they gets their cards first
			}
		}
		
		ArrayList<Player> toBeRemoved= new ArrayList<Player>();
		for(int i=0;i<waiting.size();i++) {
			if(players.size()<5) { //the amount of players active at any time is limited to 4 here, could be easily changed 
				waiting.get(i).setMessage("WELCOME!"); 
				players.add(waiting.get(i));
				toBeRemoved.add(waiting.get(i));
			}else {
				break;
			}
		}
		for(Player p: toBeRemoved) {
			waiting.remove(p);
		}
		tableCleared=true;
	}
	/**
	 * provides the "hit" functionality 
	 * @param player - player to receive an extra card
	 */
	public void hit(Player player) {
		player.getHand().add(deck.getCards().get(0));
		deck.getCards().remove(0); //remove the card the player receive from the deck
		player.calculateHand(); //update the hand value (also checks if they are bust)
	}
	
	/**
	 * deals two cards to a player
	 * @param p - the player
	 */
	public void deal(Player p) {
		for(int i=0;i<2;i++) {
			p.getHand().add(deck.getCards().get(0));
			deck.getCards().remove(0);
		}
		p.calculateHand();
	}
	/**
	 * calculate all active players' hand values
	 */
	public void calculateValues() {
		for(Player p: players) {
			p.calculateHand();
		}
	}
	/**
	 * remove players that have no points from the players' list, returns false if the dealer was removed
	 */
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
	/**
	 * gives the turn to the right 
	 */
	public void giveTurn() {
		if(!roundStarted) { 
			boolean someoneInTurn=false;
			boolean dealerPresent=false;
			int theirIndex=0;
			int dealerIndex=0;
			
			for(int i=0;i<players.size();i++) { //checks for who's turn it is and who the dealer is
				if(players.get(i).isTheirTurn()) {
					someoneInTurn=true;
					theirIndex=i;
				}else if(players.get(i).isDealer()) {
					dealerIndex=i;
					dealerPresent=true;
				}
			}
			if(someoneInTurn){
				ListIterator<Player> l=players.listIterator(theirIndex); //start an iterator at the player who's in turn
				l.next(); //skip the player in turn
				boolean validNext=false;
				while(!validNext) {
					if(l.hasNext()) {
						if(!players.get(l.nextIndex()).isDealer() && players.get(l.nextIndex()).getBet()==0 && !players.get(l.nextIndex()).isTheirTurn()){
							//the turn goes to a player that has not bet yet, who is not already in turn or the dealer
							l.next().setTheirTurn(true); //give them the turn
							validNext=true;// breaks the loop
						}else {
							l.next();
						}
					}else {
						l=players.listIterator(0); //restart from the beginning of the list
					}
				}
				players.get(theirIndex).setTheirTurn(false); //take the turn from the previous player
			}else if(dealerPresent && !someoneInTurn){ //this is the first player to receive a turn 
				if(dealerIndex+1<players.size()) {
						players.get(dealerIndex+1).setTheirTurn(true); //in case he is just after the dealer
				}else {
						players.get(0).setTheirTurn(true); //in case the dealer is last in the list
				}
			}
		}else{ //if the round has started
			for(int i=0;i<players.size();i++) { 
				if(players.get(i).isTheirTurn() && players.get(i).isDoneForTheRound()){ //only moves on when the player is standing or bust  
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
					players.get(i).setTheirTurn(false); //take the turn from the previous player
					break; //the turn has been passed on
				}
			}
		}
	}
	/**
	 * gives the turn to the first player after the dealer or first in the list, used when giveTurn() can't be 
	 */
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
			if(!players.get(0).isDealer()) {
			players.get(0).setTheirTurn(true);
			}
		}
	}
	/**
	 * calculates who wins what from the round
	 */
	public void roundEnd() { // a method that checks everyones scores when no one had a blackjack in the beginning
		Player dealer = null;
		for(Player p: players) {
			if(p.isDealer()) {
				dealer=p;
			}
		}
		calculateValues(); //update hand values
		for(Player p: players) {
			if(!p.isDealer()) {// because everyone essentially plays versus the dealer, we don't need to check from his side
				if(p.getHandValue()<22 && dealer.getHandValue()<22) { //if neither is bust  
					if (p.getHandValue()>dealer.getHandValue()) { //if the player has more
						p.setPoints(p.getPoints()+p.getBet()*2);
						dealer.setPoints(dealer.getPoints()-p.getBet());
						p.setMessage("YOU WIN.");
					}else if(p.getHandValue()==dealer.getHandValue()) { //this is a push
						p.setPoints(p.getPoints()+p.getBet());
						p.setMessage("PUSH.");
					}else { //the dealer has the better hand
						dealer.setPoints(dealer.getPoints()+p.getBet());
						p.setMessage("YOU LOSE.");
					}
				}else if(p.getHandValue()>21 && dealer.getHandValue()>21) { //both the dealer and player are bust
					p.setPoints(p.getPoints()+p.getBet());
					p.setMessage("PUSH.");
				}else if(p.getHandValue()>21){ //the player is bust
					dealer.setPoints(dealer.getPoints()+p.getBet());
					p.setMessage("BUST.");
				}else if(dealer.getHandValue()>21) { //the dealer is bust
					p.setPoints(p.getPoints()+p.getBet()*2);
					dealer.setPoints(dealer.getPoints()-p.getBet());
					p.setMessage("DEALER BUST.");
				}
			}
		}
		tableCleared=false; //the table needs cleared, because the round is finished
	}
	public ArrayList<Player> getWaiting() {
		return waiting;
	}
	public void setTableCleared(boolean tableCleared) {
		this.tableCleared=tableCleared;
	}
	public boolean isTableCleared() {
		return tableCleared;
	}
	
}
