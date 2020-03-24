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
				System.out.println("server streams started");
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
							 System.out.println("UNLOCKED");
							}
							theLock.unlock();
							done=true;
						}else {
							try
								{ 
								Thread.sleep(1000); 
						        System.out.println("Sleeping");
								} 
							catch(InterruptedException e) 
								{ 
						          e.printStackTrace(); 
						        } 
						}
					}
					System.out.println(Thread.currentThread().getName()+" exited");
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
				model.deal(nameMatch);
				nameMatch.setDoneForTheRound(false);
				System.out.println("BET :"+ nameMatch.getBet() + " DONE:" +nameMatch.isDoneForTheRound() + " TURN: "+ nameMatch.isTheirTurn());
			}else if(clientPackage.isStand()) {
				nameMatch.setDoneForTheRound(true);
			}else if(clientPackage.isNextRound()) {
				nextRound=true;
			}
			ServerPackage serverPackage =new ServerPackage(model.getPlayers(),nameMatch);
			serverPackage.setJustANewGuy(newGuy);
			serverPackage.setNextRound(nextRound);
			return serverPackage;
		}
		public ServerPackage advanceGame(ServerPackage serverPackage){ 
			System.out.println("package arrived :"+ serverPackage.getPlayer().getName());
			int active=0;
			boolean noDealer=true;
			boolean newGame=false;
			boolean betsIn=true;
			boolean waitingForBet=false;
			boolean noTurn=true;
			int done = 0;
			ArrayList<Player> blackJacks=new ArrayList<Player>();
			for(Player p:model.getPlayers()) {
				if(p.isActive()) {
					active++;
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
			if(active>1 && noDealer) {
				model.assignDealer();
				for(Player p: model.getPlayers()) {
					if(p.isDealer() && p.getHand().size()==0) {
						model.deal(p);
						p.setDoneForTheRound(false);
						done--;
					}
				}
				model.giveTurn();
				System.out.println("LOOP FOR DEALER");	
			}
			
			if(serverPackage.isNextRound()) {
				model.clearTable();
				newGame=true;
				betsIn=false;
				if(model.getPlayers().size()>1) {
					model.firstToHit();
				}
//				model.firstToHit();
			}
			
			if(!model.isRoundStarted() && active>1) { //a round should start with these conditions, but only if the bets are already in
				if(betsIn) {
					newGame=true;
					model.setRoundStarted(true);
					model.firstToHit();
					System.out.println("LOOP FOR ROUND START");	
				}else { //if the bets are not in, a turn needs to be given to a player in order for the betting to start
					if(noTurn) {
						model.firstToHit();
						System.out.println("LOOP FOR NO TURN");	
					}
				}
			}
			

				
			if (done==0 && model.isRoundStarted() && betsIn){
				for(Player p: model.getPlayers()){	
					if(p.getHand().size()==2 && p.getHandValue()==21){
						blackJacks.add(p);
						System.out.println("BLACKJACK");
					}
				}
				if(blackJacks.size()>0) {
						for(Player b: blackJacks) {
							int pointsToBeAdded = 0;
							for(Player p: model.getPlayers()){
								if(!blackJacks.contains(p)){
									if(!p.isDealer()) {
										pointsToBeAdded+=p.getBet()*2;
										p.setPoints(p.getPoints()-p.getBet());
									}else {
										pointsToBeAdded+=b.getBet()*2;
										p.setPoints(p.getPoints()-b.getBet()*2);
									}
								}
							}
							b.setPoints(b.getPoints()+pointsToBeAdded+b.getBet());
						}
						for(Player p: model.getPlayers()) { //remove the previous dealer
							p.setDealer(false);
						}
//						model.checkLosers();
						blackJacks.get(0).setDealer(true);
						blackJacks.get(0).setTheirTurn(false);
						model.setRoundStarted(false);
						serverPackage.setRoundFinished(true);
						newGame=true;
//						model.clearTable();
				}
				blackJacks.clear();
				System.out.println("LOOP FOR BLACKJACKS");
				 
			}
			if(done==model.getPlayers().size() && betsIn) {
				System.out.println("went in to last");
				model.setRoundStarted(false);
				model.roundEnd();
//				model.clearTable();
				newGame=true;
				serverPackage.setRoundFinished(true);
				System.out.println("LOOP FOR END OF THE ROUND");	
			}
			System.out.println("The round is started :" +model.isRoundStarted());
			System.out.println("This is a new game :" +newGame);
			serverPackage.setRoundStarted(model.isRoundStarted());
			if(!newGame && !serverPackage.isJustANewGuy()) {
				if(!waitingForBet) {
				model.giveTurn();
				System.out.println("IF FOR GIVING TURN");
				}
			}
			System.out.println("The Players : "+model.getPlayers().toString());
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
				System.out.println("package sent :"+ serverPackage.getPlayer().getName());
			}
		}
	}
	public void run() {
		while(true) {
			Socket clientSocket=null;
			try {
				clientSocket= serverSocket.accept();
				System.out.println("clients accepted");
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
