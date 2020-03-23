import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Server implements Runnable {
	private class ClientRunner implements Runnable{
		private Socket socket=null;
		private Server parent=null;
		private ObjectInputStream inputStream = null;
		private ObjectOutputStream outputStream = null;
		public ClientRunner(Socket s, Server parent) {
			socket=s;
			this.parent=parent;
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
					Player nameMatch=null;
					boolean newGuy=false;
					boolean nextRound=false;
					for(Player p:parent.model.getPlayers()) {
						if(p.getName().equals(clientPackage.getPlayer().getName())) { //if we have a previous instance of this player 
							nameMatch=p; //save reference here because they can't be removed from the list while it's being iterated
						}
					}
					
					if(clientPackage.isSit()) {
						if(nameMatch!=null) { //if we have a match, make this new Player's name unique, make him not new and then add him
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
					advanceGame(serverPackage);
				
				}
				inputStream.close();
			}catch(ClassNotFoundException e) {
				e.printStackTrace();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		public void advanceGame(ServerPackage serverPackage){ 
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
			
			if(!model.isRoundStarted() && active>1) { //we have enough players for a game and the round hasn't started yet, so I start it here 
				if(betsIn) {
					newGame=true;
					model.setRoundStarted(true);
					model.firstToHit();
					System.out.println("LOOP FOR ROUND START");	
				}else {
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
			sendPackages(serverPackage);
			
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
	public Server() {
		model=new Model();
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
				ClientRunner client = new ClientRunner(clientSocket,this);
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
