import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements Runnable {
	private class ClientRunner implements Runnable{
		private Socket socket=null;
		private Server parent=null;
		private ObjectInputStream inputStream = null;
		private ObjectOutputStream outputStream = null;
		private ReentrantLock theLock;
		public ClientRunner(Socket s, Server parent, ReentrantLock theLock) {
			socket=s;
			this.parent=parent;
			this.theLock=theLock;
			try {
				outputStream = new ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		public void run() {
			try {
				ClientPackage clientPackage=null;
				while((clientPackage= (ClientPackage)inputStream.readObject())!=null){ //receive the package of information from the client
					System.out.println(Thread.currentThread().getName());
					boolean done=false;
					while(!done) {
						boolean available=theLock.tryLock();
						if(available){
							theLock.lock(); //in case someone sits down in this precise moment and sends a Player to be added to the list
							try {
							ServerPackage initialServerPackage=initialProcessing(clientPackage);
							ServerPackage finishedServerPackage=advanceGame(initialServerPackage);
							sendPackages(finishedServerPackage);
							}catch(Exception e) {
								e.printStackTrace();
							}finally{
							theLock.unlock();
							}
							theLock.unlock();
							done=true;
						}else {
							try
								{ 
								Thread.sleep(500);
								} 
							catch(InterruptedException e) 
								{ 
						          e.printStackTrace(); 
						        } 
						}
					}
				}
				inputStream.close();
			}catch(ClassNotFoundException e) {
				e.printStackTrace();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		public ServerPackage initialProcessing(ClientPackage clientPackage) {
			Player nameMatch=null;
			boolean newGuy=false;
			boolean nextRound=false;
			clientPackage.getPlayer().setMessage("");
			for(Player p:parent.model.getPlayers()) {
				if(p.getName().equals(clientPackage.getPlayer().getName())) { //if we have a previous instance of this player 
					nameMatch=p; //save reference here because they can't be removed from the list while it's being iterated
				}
			}
			if(clientPackage.isSit()) {
				if(nameMatch!=null) { //if we have a match, make this new Player's name unique by adding the hashcode of his object to the name
					clientPackage.getPlayer().uniqueName();
				}
				newGuy=true;
				clientPackage.getPlayer().setNewPlayer(false);
				clientPackage.getPlayer().setActive(true);
				parent.model.addPlayer(clientPackage.getPlayer());
				nameMatch=clientPackage.getPlayer();
			}else if(clientPackage.isHit()) {
				parent.model.hit(nameMatch);
			}else if(clientPackage.isBet()) {
				nameMatch.setBet(clientPackage.getPlayer().getBet());
				nameMatch.setPoints(clientPackage.getPlayer().getPoints());
				nameMatch.setMessage("HIT OR STAND?");
				model.deal(nameMatch);
				nameMatch.setDoneForTheRound(false);
			}else if(clientPackage.isStand()) {
				nameMatch.setDoneForTheRound(true);
				nameMatch.setMessage("STANDING.");
			}else if(clientPackage.isNextRound()) {
				nextRound=true;
			}
			ServerPackage serverPackage =new ServerPackage(model.getPlayers(),nameMatch);
			serverPackage.setJustANewGuy(newGuy);
			serverPackage.setNextRound(nextRound);
			return serverPackage;
		}
		/**
		 * measures certain attributes of the model and advances the state of the game 
		 * @param serverPackage - the package coming from initial processing and is used to update the state of the model
		 * @return - the package, which reflects the current state of the game 
		 */
		public ServerPackage advanceGame(ServerPackage serverPackage){ 
			int active=0;
			boolean noDealer=true;
			boolean newGame=false;
			boolean betsIn=true;
			boolean waitingForBet=false;
			boolean noTurn=true;
			int done=0;
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
				done--; 
				model.giveTurn();
			}
			if(serverPackage.isNextRound()) { //the dealer has clicked the "Next Round" button
				model.clearTable();
				newGame=true;
				betsIn=false;
				noTurn=true;
			}
			if(!model.isRoundStarted() && active>1) { //a round should start with these conditions, but only if the bets are already in
				if(betsIn) {
					newGame=true;
					model.setRoundStarted(true);
					model.firstToHit();
					System.out.println("LOOP FOR ROUND START");	
				}else if(noTurn){ //if the bets are not in, a turn needs to be given to a player in order for the betting to start
						model.firstToHit();
						System.out.println("LOOP FOR NO TURN");	
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
						blackJacks.get(0).setTheirTurn(false);
						model.setRoundStarted(false);
						model.setTableCleared(false);
						serverPackage.setRoundFinished(true);
						newGame=true;
				}
			}
			if(done==model.getPlayers().size() && betsIn) { //means everyone is standing or bust 
				model.setRoundStarted(false);
				model.roundEnd();
				newGame=true;
				serverPackage.setRoundFinished(true);
				System.out.println("LOOP FOR END OF THE ROUND");	
			}
			serverPackage.setRoundStarted(model.isRoundStarted());
			if(!newGame && !serverPackage.isJustANewGuy()) {
				if(!waitingForBet) {
				model.giveTurn();
				}
			}
			return serverPackage;
			
		}
		
		public void sendPackage(ServerPackage s) {
			try{
				outputStream.writeObject(s);
				outputStream.flush();
				outputStream.reset();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	private ServerSocket serverSocket=null;
	private ArrayList<ClientRunner> clients = new ArrayList<ClientRunner>();
	private Model model;
	private ReentrantLock theLock;
	public Server() {
		model=new Model();
		theLock=new ReentrantLock(true);
		try {
			serverSocket = new ServerSocket(8765);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	public void sendPackages(ServerPackage serverPackage) {
		for(ClientRunner c: clients) {
			if(c!=null) {
				c.sendPackage(serverPackage);
			}
		}
	}
	public void run() {
		while(true) {
			Socket clientSocket=null;
			try {
				clientSocket= serverSocket.accept();
				ClientRunner client = new ClientRunner(clientSocket,this, theLock);
				clients.add(client);
				new Thread(client).start();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Thread t = new Thread(new Server());
		t.start();
		try {
			t.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
