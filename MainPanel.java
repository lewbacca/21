import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ListIterator;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.util.ArrayList;

public class MainPanel extends JPanel{
	private Client client;
	private final JButton[] buttons;
	private JLabel[] points=new JLabel[4];
	private JTextArea[] cards=new JTextArea[4];
	private JLabel[] dealerLabels=new JLabel[4];
	private JTextField betSize;
	private JTextArea textArea;
	private JButton nextRound,sit,bet,hit,stand;
	public MainPanel(Client client) {
		this.client=client;
		Color darkGreen = new Color(11, 102, 35);
		Color lightGreen = new Color(76, 187, 23);
		Color darkRed = new Color(202, 0, 42);
		for(int i=0;i<points.length;i++) {
			points[i]=new JLabel();
			points[i].setFont(new Font("Arial", Font.BOLD, 16));
			points[i].setForeground(darkRed);
			cards[i]=new JTextArea();
			cards[i].setEditable(false);
			cards[i].setBackground(lightGreen);
			cards[i].setFont(new Font("Arial", Font.BOLD, 16));
			dealerLabels[i]=new JLabel("Dealer");
			dealerLabels[i].setFont(new Font("Arial", Font.BOLD, 16));
			points[i].setForeground(darkRed);
		}
		clearEverything();
		this.setLayout(new BorderLayout());
		textArea=new JTextArea("WELCOME!");
		textArea.setForeground(darkRed);
		textArea.setFont(new Font("Arial", Font.BOLD,60));
		textArea.setEditable(false);
		textArea.setBackground(darkGreen);
		JPanel bottomPanel = new JPanel(new GridLayout(1,4));
		JPanel topPanel = new JPanel();
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
		
		sit.addActionListener(client);
		hit.addActionListener(client);
		bet.addActionListener(client);
		stand.addActionListener(client);
		nextRound.addActionListener(client);
		hit.setEnabled(false);
		bet.setEnabled(false);
		stand.setEnabled(false);
		nextRound.setEnabled(false);
		topPanel.add(textArea);
		bottomPanel.add(bottomLeftPanel);
		bottomPanel.add(middleLeftPanel);
		bottomPanel.add(middleRightPanel);
		bottomPanel.add(bottomRightPanel);
		this.add(topPanel, BorderLayout.NORTH);
		this.add(bottomPanel, BorderLayout.CENTER);
	}
	public void clearEverything() {
		for(int i=0;i<dealerLabels.length;i++) {
			dealerLabels[i].setVisible(false);
			cards[i].setText("");
			points[i].setText("");
		}
		System.out.println("out the clear loop.");
	}
	public void display(ServerPackage serverPackage) {
		System.out.println("Package in display.");
		clearEverything();
		textArea.setText("");
		System.out.println("after set text");
		int playersIndex=0;
		System.out.println("SPECTATING:"+client.isSpectating());
		if(!client.isSpectating()) {
			System.out.println("in the spectating loop.");
			for(int i=0;i<serverPackage.getPlayers().size();i++) {
				if(serverPackage.getPlayers().get(i).equals(client.getPlayer())) {
					playersIndex=i;
				}
			}
			System.out.println("out the is spectating loop.");
		}
		System.out.println("on to list iterator.");
		ListIterator<Player> listr=serverPackage.getPlayers().listIterator(playersIndex);
		for(int i=0;i<serverPackage.getPlayers().size();i++){
			Player z=new Player("");
			if(!listr.hasNext()) {
				listr=serverPackage.getPlayers().listIterator(0);	
			}
			z=listr.next();
			if(z.getName().equals(client.getPlayer().getName()) || serverPackage.isRoundFinished()){
				cards[i].setText(z.getName()+"'s cards:\n"+ z.handToString());
			}
			points[i].setText(z.getName()+"'s points: "+z.getPoints());
			if(z.isDealer()) {
				dealerLabels[i].setVisible(true);
			}
			if(z.getName().equals(client.getPlayer().getName())) {
				textArea.append(z.getMessage());
			}
			System.out.println("Out the loop");
		}
		if(!serverPackage.isRoundFinished()) {
			if(serverPackage.isRoundStarted()){
				if(client.getPlayer().isTheirTurn()) {
					if(!client.getPlayer().isDealer()) {
						if(!client.getPlayer().isDoneForTheRound()){
							if(client.getPlayer().getHand().size()>0) {
								hit.setEnabled(true);
								stand.setEnabled(true);
								textArea.append("\nYOUR TURN.");
							}
						}else {
							if(client.getPlayer().getHand().size()==0) {
								bet.setEnabled(true);
								hit.setEnabled(false);
								stand.setEnabled(false);
								textArea.append("\nTIME TO BET!");
							}else {
								hit.setEnabled(false);
								stand.setEnabled(false);
							}
						}
					}else {
						if(!client.getPlayer().isDoneForTheRound()) {
							if(client.getPlayer().getHand().size()>0) {
								bet.setEnabled(false);
								hit.setEnabled(true);
								stand.setEnabled(true);
								textArea.append("\nYOUR TURN!");
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
					textArea.append("\nWAITING FOR OTHERS.");
				}
			}else if(!serverPackage.isRoundStarted() && client.getPlayer().isTheirTurn() && !client.getPlayer().isDealer()){ 
				bet.setEnabled(true);
				textArea.append("\nTIME TO BET.");
			}else if(!serverPackage.isRoundStarted() && client.getPlayer().isDealer()) {
				for(int i=0;i<buttons.length;i++){
					buttons[i].setEnabled(false);
				}
				textArea.append("\nWAITING FOR OTHERS");
			}else if(!serverPackage.isRoundStarted()&& !client.getPlayer().isActive()) { //adding this to counter issue of player only sitting before the dealer clicks next round
				sit.setEnabled(true);
				textArea.append("\nSIT TO PLAY!");
			}else {
				textArea.append("\nWAITING FOR OTHERS TO BET.");
			}
		}else {
			if(client.getPlayer().isDealer()) {
				nextRound.setEnabled(true);
			}
			sit.setEnabled(false);
			bet.setEnabled(false);
			hit.setEnabled(false);
			stand.setEnabled(false);
			textArea.append("\nROUND OVER!");
			client.getPlayer().setMessage("");
		}
	System.out.println("Out of display");
	}
	public JButton getNextRound() {
		return nextRound;
	}
	public void setNextRound(JButton nextRound) {
		this.nextRound = nextRound;
	}
	public JTextField getBetSize() {
		return betSize;
	}
	public JButton getSit() {
		return sit;
	}
	public JButton getBet() {
		return bet;
	}
	public JButton getHit() {
		return hit;
	}
	public JButton getStand() {
		return stand;
	}
	
}
