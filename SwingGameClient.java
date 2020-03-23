import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.ListIterator;

public class SwingGameClient extends JFrame implements ActionListener{
	private class ReadWorker extends SwingWorker<Void,Void> {
		private Socket socket=null;
		private ObjectInputStream inputStream = null;
		private SwingGameClient parent;
		public ReadWorker(Socket s, SwingGameClient parent) {
			socket=s;
			this.parent=parent;
			try {
				inputStream = new ObjectInputStream(socket.getInputStream());
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		public Void doInBackground() {
			System.out.println("started read worker");
			ServerPackage serverPackage=null;
			try {
				while((serverPackage = (ServerPackage)inputStream.readObject())!=null) {
					parent.textArea.setText("");
//					boolean inPlayer=false;
					boolean inList=false;
					if(player.isNewPlayer()) {
						if(serverPackage.getPlayer().getName().equals(player.getName()) || serverPackage.getPlayer().getName().contains(player.getName())) {
							parent.youAre(serverPackage.getPlayer());
							System.out.println("INITIAL ASSIGNING");
						}
					}
					for(Player p: serverPackage.getPlayers()) {
						if(p.getName().equals(player.getName())){
							parent.youAre(p);
							inList=true;
							System.out.println("From the list, you are:" + p.getName());
						}
					}
					if(!inList) { //means that the player was removed from the player's list, ran out of points
						player.setActive(false);
						player.setBet(0);
						player.getHand().clear();
						player.setPoints(0);
						player.setTheirTurn(false);
						player.setDealer(false);
						parent.sit.setEnabled(true);
						parent.spectating=true;
						System.out.println("SHOULD HAVE SIT ENABLED after not being in the list");
					}
					parent.display(serverPackage);
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
	private JTextField betSize;
	private JTextArea textArea;
	private JButton nextRound,sit,bet,hit,stand;
	private ObjectOutputStream outputStream = null;
	private String name;
	private Player player;
	private final int UNIT=20;
	private final JButton[] buttons;
	private JLabel[] points=new JLabel[4];
	private JTextArea[] cards=new JTextArea[4];
	private JLabel[] dealerLabels=new JLabel[4];
//	private	JLabel[]	youLabels=new JLabel[4];
	private boolean spectating;
	public SwingGameClient() {
		this.setSize(UNIT*60,UNIT*40);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		name = JOptionPane.showInputDialog(this, "What's your name?");
		player=new Player(name);
		this.setName(name);
		Color darkGreen = new Color(11, 102, 35);
		Color lightGreen = new Color(76, 187, 23);
		for(int i=0;i<points.length;i++) {
			points[i]=new JLabel();
			cards[i]=new JTextArea();
			cards[i].setEditable(false);
			cards[i].setBackground(lightGreen);
			cards[i].setFont(new Font("Arial", Font.BOLD, 12));
			dealerLabels[i]=new JLabel("DEALER");
//			youLabels[i]=new JLabel("YOU");
		}
		
		clearEverything();
		spectating=false;
		JPanel mainPanel = new JPanel(new BorderLayout());
		textArea=new JTextArea();
		textArea.setFont(new Font("Arial", Font.BOLD,60));
		textArea.setText("WELCOME!");
		textArea.setEditable(false);
		textArea.setBackground(darkGreen);
		JPanel bottomPanel = new JPanel(new GridLayout(1,4));
		JPanel topPanel = new JPanel();
		topPanel.setSize(UNIT*58, UNIT*10);
		topPanel.setLayout(new FlowLayout());
		topPanel.setBackground(darkGreen);
		JPanel bottomLeftPanel = new JPanel(new GridLayout(2,1));
		JPanel bottomLeftTopPanel=new JPanel(new GridLayout(3,1));
		JPanel bottomLeftBottomPanel= new JPanel();
		bottomLeftPanel.add(bottomLeftTopPanel);
		bottomLeftPanel.add(bottomLeftBottomPanel);
		
		JPanel middleLeftPanel = new JPanel(new GridLayout(2,1));
		JPanel middleLeftTopPanel= new JPanel(new GridLayout(3,1));
		JPanel middleLeftBottomPanel= new JPanel();
		middleLeftPanel.add(middleLeftTopPanel);
		middleLeftPanel.add(middleLeftBottomPanel);
		
		JPanel middleRightPanel = new JPanel(new GridLayout(2,1));
		JPanel middleRightTopPanel= new JPanel(new GridLayout(3,1));
		JPanel middleRightBottomPanel= new JPanel();
		middleRightPanel.add(middleRightTopPanel);
		middleRightPanel.add(middleRightBottomPanel);
		
		JPanel bottomRightPanel = new JPanel(new GridLayout(2,1));
		JPanel bottomRightTopPanel=new JPanel(new GridLayout(3,1));
		JPanel bottomRightBottomPanel= new JPanel();
		bottomRightPanel.add(bottomRightTopPanel);
		bottomRightPanel.add(bottomRightBottomPanel);
		
		sit = new JButton("Sit");
		bet = new JButton("Bet");
		hit = new JButton("Hit");
		stand = new JButton("Stand");
		nextRound = new JButton("Next Round");
		betSize = new JTextField(10);
		buttons = new JButton[5];
		buttons[0]=sit;
		buttons[1]=bet;
		buttons[2]=hit;
		buttons[3]=stand;
		buttons[4]=nextRound;
		
		
		bottomLeftBottomPanel.add(sit);
		bottomLeftBottomPanel.add(bet);
		bottomLeftBottomPanel.add(betSize);
		bottomLeftBottomPanel.add(hit);
		bottomLeftBottomPanel.add(stand);
		bottomLeftBottomPanel.add(nextRound);
		bottomLeftBottomPanel.setBackground(darkGreen);
		bottomLeftTopPanel.add(dealerLabels[0]);
		bottomLeftTopPanel.add(cards[0]);
		bottomLeftTopPanel.add(points[0]);
		bottomLeftTopPanel.setBackground(lightGreen);
		
		middleLeftTopPanel.add(dealerLabels[1]);
		middleLeftTopPanel.add(cards[1]);
		middleLeftTopPanel.add(points[1]);
		middleLeftTopPanel.setBackground(lightGreen);
		middleLeftBottomPanel.setBackground(darkGreen);
		
		middleRightTopPanel.add(dealerLabels[2]);
		middleRightTopPanel.add(cards[2]);
		middleRightTopPanel.add(points[2]);
		middleRightTopPanel.setBackground(lightGreen);
		middleRightBottomPanel.setBackground(darkGreen);
		
		bottomRightTopPanel.add(dealerLabels[3]);
		bottomRightTopPanel.add(cards[3]);
		bottomRightTopPanel.add(points[3]);
		bottomRightTopPanel.setBackground(lightGreen);
		bottomRightBottomPanel.setBackground(darkGreen);
		
		sit.addActionListener(this);
		hit.addActionListener(this);
		bet.addActionListener(this);
		stand.addActionListener(this);
		nextRound.addActionListener(this);
		hit.setEnabled(false);
		bet.setEnabled(false);
		stand.setEnabled(false);
		nextRound.setEnabled(false);
		topPanel.add(textArea);
		bottomPanel.add(bottomLeftPanel);
		bottomPanel.add(middleLeftPanel);
		bottomPanel.add(middleRightPanel);
		bottomPanel.add(bottomRightPanel);
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(bottomPanel, BorderLayout.CENTER);
		
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
			System.out.println("Connected");
		}catch(IOException e) {
			e.printStackTrace();
		} 
		
	}
	public void clearEverything() {
		for(int i=0;i<dealerLabels.length;i++) {
			dealerLabels[i].setVisible(false);
//			youLabels[i].setVisible(false);
			cards[i].setText("");
			points[i].setText("");
		}
	}
	public static void main(String[] args) {
		new SwingGameClient();
	}
	public void youAre(Player you) {
		player=you;
	}
	public void display(ServerPackage serverPackage) {
//		String message="";
//		for(Player p: serverPackage.getPlayers()) {
//			message+=p.getName()+ ": ";
//			for(Card c: p.getHand()) {
//				message+= c.toString()+" ";
//			}
//			message+=" :"+ p.getBet();
//			message+="\n Round Start: " + serverPackage.isRoundStarted();
//			message+="\n Dealer :" + p.isDealer();
//			message+="\n Turn :" + p.isTheirTurn();
//			message+="\n Done :" + p.isDoneForTheRound();
//			message+="\n Points :" + p.getPoints();
//			message+="\n";
//			
//		}
//		textArea.append(message + '\n');
		clearEverything();
		int playersIndex=0;
		if(!spectating) {
			for(int i=0;i<serverPackage.getPlayers().size();i++) {
				if(serverPackage.getPlayers().get(i).equals(player)) {
					playersIndex=i;
				}
			}
		}
		ListIterator<Player> listr=serverPackage.getPlayers().listIterator(playersIndex);
		
		for(int i=0;i<serverPackage.getPlayers().size();i++){
			Player z=new Player("");
			if(!listr.hasNext()) {
				listr=serverPackage.getPlayers().listIterator(0);	
			}
			z=listr.next();
			if(z.getName().equals(player.getName()) || serverPackage.isRoundFinished()){
				cards[i].setText(z.getName()+"'s cards:\n"+ z.handToString());
//				youLabels[i].setVisible(true);
			}
			points[i].setText(z.getName()+"'s points: "+z.getPoints());
			if(z.isDealer()) {
				dealerLabels[i].setVisible(true);
			}
		}
		
		if(!serverPackage.isRoundFinished()) {
			if(serverPackage.isRoundStarted()){
				if(player.isTheirTurn()) {
					if(!player.isDealer()) {
						if(!player.isDoneForTheRound()){
							if(player.getHand().size()>0) {
								hit.setEnabled(true);
								stand.setEnabled(true);
								textArea.setText("YOUR TURN");
							}
						}else {
							if(player.getHand().size()==0) {
								bet.setEnabled(true);
								hit.setEnabled(false);
								stand.setEnabled(false);	
							}else {
								hit.setEnabled(false);
								stand.setEnabled(false);
							}
						}
					}else {
						if(!player.isDoneForTheRound()) {
							if(player.getHand().size()>0) {
								bet.setEnabled(false);
								hit.setEnabled(true);
								stand.setEnabled(true);
								textArea.setText("YOUR TURN");
							}
						}else {
							hit.setEnabled(false);
							stand.setEnabled(false);
						}
					}
				}else{
					for(int i=0;i<buttons.length;i++){
						buttons[i].setEnabled(false);
					}
					textArea.setText("WAITING FOR OTHERS");
				}
			}else if(!serverPackage.isRoundStarted() && player.isTheirTurn() && !player.isDealer()){ 
				bet.setEnabled(true);
			}else if(!serverPackage.isRoundStarted() && player.isDealer()) {
				for(int i=0;i<buttons.length;i++){
					buttons[i].setEnabled(false);
				}
			}else if(!serverPackage.isRoundStarted()&& !player.isActive()) { //adding this to counter issue of player only sitting before the dealer clicks next round
				sit.setEnabled(true);
			}
		}else {
			if(player.isDealer()) {
				nextRound.setEnabled(true);
			}
			sit.setEnabled(false);
			bet.setEnabled(false);
			hit.setEnabled(false);
			stand.setEnabled(false);
		}
	}
	public void actionPerformed(ActionEvent e) {
		ClientPackage clientPackage= new ClientPackage(player);
		boolean badInput=false;
		if(e.getSource() == sit) {
			sit.setEnabled(false);
			player.setPoints(1000);
			player.setDoneForTheRound(true);
			clientPackage.setSit(true);
			player.setActive(true);
			player.setNewPlayer(true);
		}else if(e.getSource()== bet) {
			boolean validInput=true;
			for (char c : betSize.getText().toCharArray()) {
			    if (!Character.isDigit(c)){
			        validInput=false;
			        betSize.setText("");
			        badInput=true;
			        break;
			    }
			}
			if(validInput) {
				int input=Integer.parseInt(betSize.getText());
				if(input>0 && input<=player.getPoints()){
					player.setBet(input);
					player.setPoints(player.getPoints()-player.getBet());
					player.setDoneForTheRound(false);
					clientPackage.setBet(true);
					bet.setEnabled(false);
				}
			}
		}else if(e.getSource()== hit) {
			clientPackage.setHit(true);
		}else if(e.getSource()== stand){
			clientPackage.setStand(true);
			player.setDoneForTheRound(true);
			bet.setEnabled(false);
			hit.setEnabled(false);
			stand.setEnabled(false);
			
		}else if(e.getSource()==nextRound) {
			clientPackage.setNextRound(true);
			nextRound.setEnabled(false);
			
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

}
