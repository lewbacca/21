import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
/**
 * This is the server class. It accepts clients and creates a client runner thread for each one that joins.
 * Controls concurency and prevents race conditions with a reentrant lock.
 *  @author 2072353l, Lyubomir Lazarov
 *
 */
public class Server implements Runnable {
	/**
	 * objects form this class handle everything to do with a specific client, by calling Controller methods. 
	 * @author 2072353l, Lyubomir Lazarov
	 *
	 */
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
		/**
		 * this method receives the client packages and allows the controller change the state of the model,
		 * before calling sendPackages() to send a reflection of this change to all players 
		 */
		public void run() {
			try {
				ClientPackage clientPackage=null;
				while((clientPackage= (ClientPackage)inputStream.readObject())!=null){ //receive the package of information from the client
					boolean done=false;
					while(!done) {
						boolean available=theLock.tryLock(); //check if the lock is available
						if(available){
							theLock.lock(); //only 1 thread at a time should access the model and controller to prevent race conditions
							try {
							ServerPackage initialServerPackage=controller.initialProcessing(clientPackage);
							ServerPackage finishedServerPackage=controller.advanceGame(initialServerPackage);
							sendPackages(finishedServerPackage); //send the update game state to all players
							}catch(Exception e) {
								e.printStackTrace();
							}finally{
							theLock.unlock(); //unlock the second lock, having finished the work
							}
							done=true; 
							theLock.unlock(); //unlock the first lock, leave it available for entry by another thread
						}else { //sleep for 500ms and try the lock again
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
		
		public void sendPackage(ServerPackage s) { //send a package to a specific client
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
	private Controller controller;
	private ReentrantLock theLock; //used to prevent race conditions 
	public Server() {//constructor
		model=new Model();
		controller=new Controller(model);
		theLock=new ReentrantLock(true); //reentrant lock used for synchronisation of the client threads
		try {
			serverSocket = new ServerSocket(8765);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	public void sendPackages(ServerPackage serverPackage) { //send the package to all clients
		for(ClientRunner c: clients) {
			if(c!=null) {
				c.sendPackage(serverPackage);
			}
		}
	}
	/**
	 * accepts clients and starts threads for them
	 */
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
		Thread t = new Thread(new Server()); //start a new server
		t.start();
		try {
			t.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
