import java.util.ArrayList;
/**
 * this class corresponds to the controller part of the classic MVC architecture, it is used to change the state of the model
 * based on an incoming package of information from the client 
 * @author 2072353l, Lyubomir Lazarov
 *
 */
public class Controller {
	private Model model;
	public Controller(Model model){
		this.model=model;
	}
	/**
	 * this method does initial processing like updating player objects based on information sent from the client,
	 * like bet size, desire to hit or stand, etc.
	 * @param clientPackage - the package coming in from the client
	 * @return the half-backed server package 
	 */
	public ServerPackage initialProcessing(ClientPackage clientPackage) {
		Player nameMatch=null; 
		clientPackage.getPlayer().setMessage("");
		for(Player p:model.getPlayers()) {
			if(p.getName().equals(clientPackage.getPlayer().getName())) { //if we have a previous instance of this player 
				nameMatch=p; //save the reference as nameMatch
			}
		}
		if(clientPackage.isSit()) {
			if(nameMatch!=null) { //if we have a match, make this new Player's name unique by adding the hashcode of his object to the name
				clientPackage.getPlayer().uniqueName();
			}
			clientPackage.getPlayer().setNewPlayer(false); //not a new player anymore
			model.addPlayer(clientPackage.getPlayer()); 
			nameMatch=clientPackage.getPlayer(); 
		}else if(clientPackage.isHit()) {
			model.hit(nameMatch); //give the player a new card, check if they're bust
		}else if(clientPackage.isBet()) {
			nameMatch.setBet(clientPackage.getPlayer().getBet());  
			nameMatch.setPoints(clientPackage.getPlayer().getPoints());
			nameMatch.setMessage("HIT OR STAND?");
			model.deal(nameMatch); //the players receive their cards only after they've bet 
			nameMatch.setDoneForTheRound(false); //ready to hit or stand
		}else if(clientPackage.isStand()) {
			nameMatch.setDoneForTheRound(true);
			nameMatch.setMessage("STANDING.");
		}
		ServerPackage serverPackage =new ServerPackage(model.getPlayers(),nameMatch);
		serverPackage.setJustANewGuy(clientPackage.isSit());
		serverPackage.setNextRound(clientPackage.isNextRound()); 
		return serverPackage;
	}
	/**
	 * measures certain attributes of the model and advances the state of the game 
	 * @param serverPackage - the package coming from initial processing and is used to update the state of the model
	 * @return - the package, which reflects the current state of the game 
	 */
	public ServerPackage advanceGame(ServerPackage serverPackage){ 
		int active=0;
		int done=0;
		boolean noDealer=true;
		boolean newGame=false;
		boolean betsIn=true;
		boolean waitingForBet=false;
		boolean noTurn=true;
		ArrayList<Player> blackJacks=new ArrayList<Player>();
		for(Player p:model.getPlayers()) {
			if(p.isActive()) {
				active++; //
			}
			if(p.isDoneForTheRound()) {
				done++;
			}
			if(p.isDealer()) {
				noDealer=false;
			}else if(p.getBet()==0) {
				betsIn=false;
			}
			if(p.isTheirTurn()) {
				noTurn=false;
			}
			if(p.isTheirTurn() && p.getBet()==0 ) { 
				waitingForBet=true; 
			}	
		}
		if(active>1 && noDealer) { //the conditions are right to start a round, a dealer must first be assigned
			Player dealer=model.assignDealer(); 
			model.deal(dealer); 
			dealer.setDoneForTheRound(false); 
			done--; // not done anymore
			model.giveTurn(); //the turn is passed to the first player that will bet	
		}
		if(serverPackage.isNextRound()) { //the dealer has clicked the "Next Round" button
			model.clearTable();
			newGame=true;
			betsIn=false;
			noTurn=true;
		}
		if(!model.isRoundStarted() && active>1 && model.isTableCleared()) { //a round should start with these conditions, but only if the bets are already in
			if(betsIn) {
				newGame=true; //will prevent the turn being passed to a second player
				model.setRoundStarted(true);
				model.firstToHit();	
			}else if(noTurn){ //if the bets are not in, a turn needs to be given to a player in order for the betting to start
					model.firstToHit();	
			}
		}
		if(done==0 && model.isRoundStarted() && betsIn){ //means the round has just started 
			for(Player p: model.getPlayers()){	
				if(p.getHand().size()==2 && p.getHandValue()==21){ //player has a "natural vingt-un"
					blackJacks.add(p);
				}
			}
			if(blackJacks.size()>0) {
					for(Player b: blackJacks) {
						int pointsToBeAdded = 0;
						for(Player p: model.getPlayers()){
							if(!blackJacks.contains(p)){ //blackjack players do not pay each other
								if(!p.isDealer()) {
									pointsToBeAdded+=p.getBet()*2; //a natural vingt-un earns gets the winner/winners twice what each loser bet
									p.setPoints(p.getPoints()-p.getBet());
								}else {
									pointsToBeAdded+=b.getBet()*2; //the dealer pays what the winner bet
									p.setPoints(p.getPoints()-b.getBet()*2); 
								}
							}
						p.setMessage("TWENTY ONE!");	
						}
						b.setPoints(b.getPoints()+pointsToBeAdded+b.getBet()); //add the sum of the winnings to each of the winners
					}
					for(Player p: model.getPlayers()) { //remove the previous dealer
						p.setDealer(false);
					}
					blackJacks.get(0).setDealer(true); //the winner with positional advantage becomes the new dealer
					blackJacks.get(0).setTheirTurn(false); //not their turn at the start of the next round
					model.setRoundStarted(false); //the round has finished
					model.setTableCleared(false); //the table needs cleared
					newGame=true;
			}
		}
		if(done==model.getPlayers().size() && betsIn && !serverPackage.isJustANewGuy()) { //means everyone is standing or bust 
			model.setRoundStarted(false);
			model.roundEnd();
			newGame=true;	
		}
		serverPackage.setRoundStarted(model.isRoundStarted());
		if(!newGame && !serverPackage.isJustANewGuy() && !waitingForBet) { //only move on next player when we have these conditions
			model.giveTurn();	
		}
		serverPackage.setRoundFinished(!model.isTableCleared());
		return serverPackage;
	}
}
