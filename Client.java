import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
/**
 * This class starts the GUI for the client and sends the client packages to the server 
 * @author 2072353l, Lyubomir Lazarov
 *
 */
public class Client extends JFrame implements ActionListener{
	/**
	 * this class receives server packages from the server, adds the changes to the clients player and
	 * changes the view based on the package received.
	 * @author 2072353l, Lyubomir Lazarov
	 *
	 */
	private class ReadWorker extends SwingWorker<Void,Void> {
		private Socket socket=null;
		private ObjectInputStream inputStream = null;
		private Client parent;
		public ReadWorker(Socket s, Client parent) {
			socket=s;
			this.parent=parent;
			try {
				inputStream = new ObjectInputStream(socket.getInputStream());
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		/**
		 * where the packages are received, the player is updated and the package is passed to the MainPanel class to
		 * update the view
		 */
		public Void doInBackground() {
			ServerPackage serverPackage=null;
			try {
				while((serverPackage = (ServerPackage)inputStream.readObject())!=null) {
					boolean inList=false;
					if(player.isNewPlayer()) {
						if(serverPackage.getPlayer().getName().equals(player.getName()) || serverPackage.getPlayer().getName().contains(player.getName())) {
							parent.youAre(serverPackage.getPlayer());  
						//this is important when there is a "unique-fication" of the name in the server
							//after the player sat down
						}
					}
					for(Player p: serverPackage.getPlayers()) {
						if(p.getName().equals(player.getName())){
							parent.youAre(p); //update the player object with changes made in the server
							inList=true; //this signifies this player is in the active players' list
							spectating=false; //used for view order
						}
					}
					if(!inList && !player.isNewPlayer() &&!serverPackage.isRoundFinished()) { //means that the player was removed from the player's list, ran out of points
						player.setActive(false);
						player.setBet(0);
						player.getHand().clear();
						player.setPoints(0);
						player.setTheirTurn(false);
						player.setDealer(false);
						mainPanel.getSit().setEnabled(true); //allow the player to rejoin the game as a new player
						spectating=true; 
						player.setMessage("SIT OR WAIT TO JOIN.");
					}
					mainPanel.display(serverPackage);
				}
			}catch(ClassNotFoundException e) {
				e.printStackTrace();
			}catch(IOException e) {
				e.printStackTrace();
			}finally {
				return null;
			}
		}
	}
	private Socket server=null;
	private ObjectOutputStream outputStream = null;
	private String name;
	private Player player;
	private MainPanel mainPanel;
	private boolean spectating;
	private final int UNIT=20;
	public Client() {
		this.setSize(UNIT*60,UNIT*40);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		name = JOptionPane.showInputDialog(this, "What's your name?");
		player=new Player(name);
		spectating=false;
		mainPanel=new MainPanel(this);
		this.add(mainPanel);
		this.setVisible(true);
		connect();
		try {
			outputStream = new ObjectOutputStream(server.getOutputStream());
		}catch(IOException e) {
			e.printStackTrace();
		} 
		ReadWorker rw = new ReadWorker(server, this);
		rw.execute();
	}
	/**
	 * connect to the server
	 */
	private void connect() {
		try {
			server = new Socket("127.0.0.1",8765);
		}catch(IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	public static void main(String[] args) {
		new Client();
	}
	public void youAre(Player you) {
		player=you;
	}
	/**
	 * this method detects button presses and sends a client package to the server when it happens, based on the input
	 */
	public void actionPerformed(ActionEvent e) {
		ClientPackage clientPackage= new ClientPackage(player);
		boolean badInput=false;
		if(e.getSource() == mainPanel.getSit()) { 
			mainPanel.getSit().setEnabled(false); //turn of the sit button, after having sat
			player.setPoints(1000); //give the player 1000 points to play with
			player.setDoneForTheRound(true); //hasn't bet yet
			clientPackage.setSit(true); //to indicate that the player wants to join the game
			player.setActive(true); 
			player.setNewPlayer(true);   
		}else if(e.getSource()== mainPanel.getBet()) {
			boolean validInput=true;
			for (char c : mainPanel.getBetSize().getText().toCharArray()) {
			    if (!Character.isDigit(c)){//if the input in bet size field contains anything but digits
			        validInput=false; //it's not valid input
			        mainPanel.getBetSize().setText(""); //deletes this input
			        badInput=true; 
			        break;
			    }
			}
			if(validInput) { 
				int input=Integer.parseInt(mainPanel.getBetSize().getText());
				if(input>0 && input<=player.getPoints()){
					player.setBet(input); 
					player.setPoints(player.getPoints()-player.getBet()); //subtract the bet from the points
					player.setDoneForTheRound(false); //ready for hitting or standing
					clientPackage.setBet(true); 
					mainPanel.getBet().setEnabled(false); //only one bet allowed per round
				}
			}
		}else if(e.getSource()== mainPanel.getHit()) {
			clientPackage.setHit(true); 
		}else if(e.getSource()== mainPanel.getStand()){
			clientPackage.setStand(true);
			player.setDoneForTheRound(true); //just waiting for the results of the round 
			mainPanel.getBet().setEnabled(false);
			mainPanel.getHit().setEnabled(false); //doesn't need to do anything else for the round
			mainPanel.getStand().setEnabled(false);
			
		}else if(e.getSource()==mainPanel.getNextRound()) {
			clientPackage.setNextRound(true);
			mainPanel.getNextRound().setEnabled(false); //switch the button off
			
		}
		if(!badInput) { //only send a package if we have ok input from bet size
			try {
				outputStream.writeObject(clientPackage); //send the client package to the server
				outputStream.flush(); 
				outputStream.reset(); //need to do this, otherwise sending doesn't work properly for the next package
			}catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	public boolean isSpectating() {
		return spectating;
	}
	public void setSpectating(boolean spectating) {
		this.spectating = spectating;
	}
	public Player getPlayer() {
		return player;
	}
}
