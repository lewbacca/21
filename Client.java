import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends JFrame implements ActionListener{
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
		public Void doInBackground() {
			ServerPackage serverPackage=null;
			try {
				while((serverPackage = (ServerPackage)inputStream.readObject())!=null) {
					boolean inList=false;
					if(player.isNewPlayer()) {
						if(serverPackage.getPlayer().getName().equals(player.getName()) || serverPackage.getPlayer().getName().contains(player.getName())) {
							parent.youAre(serverPackage.getPlayer());
							System.out.println("Package in");
							System.out.println("new player:"+player.isNewPlayer());
						}
					}
					for(Player p: serverPackage.getPlayers()) {
						if(p.getName().equals(player.getName())){
							parent.youAre(p);
							inList=true;
							spectating=false;
						}
					}
					if(!inList) { //means that the player was removed from the player's list, ran out of points
						player.setActive(false);
						player.setBet(0);
						player.getHand().clear();
						player.setPoints(0);
						player.setTheirTurn(false);
						player.setDealer(false);
						mainPanel.getSit().setEnabled(true);
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
	
	public void actionPerformed(ActionEvent e) {
		ClientPackage clientPackage= new ClientPackage(player);
		boolean badInput=false;
		if(e.getSource() == mainPanel.getSit()) {
			mainPanel.getSit().setEnabled(false);
			player.setPoints(1000);
			player.setDoneForTheRound(true);
			clientPackage.setSit(true);
			player.setActive(true);
			player.setNewPlayer(true);
		}else if(e.getSource()== mainPanel.getBet()) {
			boolean validInput=true;
			for (char c : mainPanel.getBetSize().getText().toCharArray()) {
			    if (!Character.isDigit(c)){
			        validInput=false;
			        mainPanel.getBetSize().setText("");
			        badInput=true;
			        break;
			    }
			}
			if(validInput) {
				int input=Integer.parseInt(mainPanel.getBetSize().getText());
				if(input>0 && input<=player.getPoints()){
					player.setBet(input);
					player.setPoints(player.getPoints()-player.getBet());
					player.setDoneForTheRound(false);
					clientPackage.setBet(true);
					mainPanel.getBet().setEnabled(false);
				}
			}
		}else if(e.getSource()== mainPanel.getHit()) {
			clientPackage.setHit(true);
		}else if(e.getSource()== mainPanel.getStand()){
			clientPackage.setStand(true);
			player.setDoneForTheRound(true);
			mainPanel.getBet().setEnabled(false);
			mainPanel.getHit().setEnabled(false);
			mainPanel.getStand().setEnabled(false);
			
		}else if(e.getSource()==mainPanel.getNextRound()) {
			clientPackage.setNextRound(true);
			mainPanel.getNextRound().setEnabled(false);
			
		}
		if(!badInput) {
			try {
				outputStream.writeObject(clientPackage);
				outputStream.flush();
				outputStream.reset();
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
