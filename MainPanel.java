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
/**
 * A single object of this class is used in every client to set up most of the view.
 * It also adjusts, which buttons should be on, based on input in the form of a ServerPackage,
 * thus limiting user behaviour to valid moves in the game of 21.
 * @author 2072353l, Lyubomir Lazarov
 *
 */
public class MainPanel extends JPanel{
	private Client client;
	private final JButton[] buttons;
	private JLabel[] points;
	private JTextArea[] cards;
	private JLabel[] dealerLabels;
	private JTextField betSize;
	private JTextArea textArea;
	private JButton nextRound,sit,bet,hit,stand;
	private JPanel[] bottomPanels;
	private JPanel[] topPanels;
	private JPanel[] playerPanels;
	public MainPanel(Client client) {
		this.client=client;
		Color darkGreen = new Color(11, 102, 35); 
		Color lightGreen = new Color(76, 187, 23);
		Color darkRed = new Color(202, 0, 42);
		dealerLabels=new JLabel[4]; //4, because there are only 4 active players at a time
		cards=new JTextArea[4];
		points=new JLabel[4];
		for(int i=0;i<points.length;i++) { // a loop to set up all the buttons, labels and text areas
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
		textArea=new JTextArea("WELCOME!"); // this text area is at the top of the screen, shows status messages
		textArea.setForeground(darkRed);
		textArea.setFont(new Font("Arial", Font.BOLD,60));
		textArea.setEditable(false);
		textArea.setBackground(darkGreen);
		
		clearEverything(); //remove all labels, as the player has just joined
		this.setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel(); //this section sets up the top of the screen with the status sign
		topPanel.setLayout(new FlowLayout());
		topPanel.setBackground(darkGreen);
		topPanel.add(textArea);
		
		sit = new JButton("Sit"); //set up all the buttons and put them in an array
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
		
		JPanel bottomPanel = new JPanel(new GridLayout(1,4));
		playerPanels=new JPanel[4];
		bottomPanels=new JPanel[4];
		topPanels=new JPanel[4];
		for(int i=0;i<topPanels.length;i++) { //creates all the mini panels (top and bottom for each player), adds labels, text areas and buttons
			playerPanels[i]= new JPanel(new GridLayout(2,1));
			topPanels[i]=new JPanel(new GridLayout(3,1));
			topPanels[i].add(dealerLabels[i]);
			topPanels[i].add(cards[i]);
			topPanels[i].add(points[i]);
			topPanels[i].setBackground(lightGreen);
			bottomPanels[i]=new JPanel();
			bottomPanels[i].setBackground(darkGreen);
			playerPanels[i].add(topPanels[i]);
			playerPanels[i].add(bottomPanels[i]);
			bottomPanel.add(playerPanels[i]);
		}
		
		for(int i=0;i<buttons.length;i++) { // adds all the buttons to the first player's mini bottom panel
			buttons[i].addActionListener(client);
			bottomPanels[0].add(buttons[i]);
			if(i==1) {
				bottomPanels[0].add(betSize); //text field, goes right after the bet button 
			}
		}
		hit.setEnabled(false);
		bet.setEnabled(false);
		stand.setEnabled(false);
		nextRound.setEnabled(false);

		this.add(topPanel, BorderLayout.NORTH);
		this.add(bottomPanel, BorderLayout.CENTER);
	}
	/**
	 * removes resets the parts of the views with the progression of the game
	 */
	public void clearEverything() {
		for(int i=0;i<dealerLabels.length;i++) {
			dealerLabels[i].setVisible(false);
			cards[i].setText("");
			points[i].setText("");
		}
	}
	/**
	 * updates the view, disables and enables particular buttons based on the information from the server package
	 * @param serverPackage - information from the server
	 */
	public void display(ServerPackage serverPackage) {
		clearEverything();
		textArea.setText("");
		int playersIndex=0;
		if(!client.isSpectating()) {
			for(int i=0;i<serverPackage.getPlayers().size();i++) {
				if(serverPackage.getPlayers().get(i).equals(client.getPlayer())) {
					playersIndex=i; //allows the correct points and cards to show up above the right player
				}
			}
		}
		ListIterator<Player> listr=serverPackage.getPlayers().listIterator(playersIndex); 
		for(int i=0;i<serverPackage.getPlayers().size();i++){ // this loop sets up the the points, cards and dealer label
			Player z=new Player("");
			if(!listr.hasNext()) {
				listr=serverPackage.getPlayers().listIterator(0); //in case we're at the end of the list	
			}
			z=listr.next();
			if(z.getName().equals(client.getPlayer().getName()) || serverPackage.isRoundFinished()){
				cards[i].setText(z.getName()+"'s cards:\n"+ z.handToString()); //show the cards
			}
			points[i].setText(z.getName()+"'s points: "+z.getPoints());
			if(z.isDealer()) {
				dealerLabels[i].setVisible(true); //shows who the dealer is for everyone to see
			}
			if(z.getName().equals(client.getPlayer().getName())) {
				textArea.append(z.getMessage()); //put this players specific message at the top of the view -"You lost", etc.
			}
		}
		if(!serverPackage.isRoundFinished()) {
			if(serverPackage.isRoundStarted()){
				if(client.getPlayer().isTheirTurn()) {
					if(!client.getPlayer().isDealer()) {
						if(!client.getPlayer().isDoneForTheRound()){ //means the player has bet
								hit.setEnabled(true); 
								stand.setEnabled(true);
								textArea.append("\nYOUR TURN.");
						}else { 
							hit.setEnabled(false);
							stand.setEnabled(false);
						}
					}else { //means it's the dealers turn to hit or stand
						bet.setEnabled(false);
						hit.setEnabled(true);
						stand.setEnabled(true);
						textArea.append("\nYOUR TURN!");
					}
				}else{ //players who are not in turn shouldn't be able to do anything
					for(int i=0;i<buttons.length;i++){
						buttons[i].setEnabled(false);
					}
					textArea.append("\nWAITING FOR OTHERS.");
				}
			}else if(!serverPackage.isRoundStarted() && client.getPlayer().isTheirTurn() && !client.getPlayer().isDealer()){ 
				bet.setEnabled(true); //before all bets are in the round does not count as started, players need to bet
				textArea.append("\nTIME TO BET.");
			}else if(!serverPackage.isRoundStarted() && client.getPlayer().isDealer()) {
				for(int i=0;i<buttons.length;i++){ //the dealer does nothing before all others have bet and taken turns to hit
					buttons[i].setEnabled(false);
				}
				textArea.append("\nWAITING FOR OTHERS");
			}else if(!serverPackage.isRoundStarted()&& !client.getPlayer().isActive()) {
				sit.setEnabled(true); //players can must be able to sit before sitting players have finished betting
				textArea.append("\nSIT TO PLAY!");
			}else {
				textArea.append("\nWAITING FOR OTHERS TO BET.");
			}
		}else { // the round is finished, points have been re-allocated
			if(client.getPlayer().isDealer()) {
				nextRound.setEnabled(true); //only the dealer can start the next round
			}
			sit.setEnabled(false);
			bet.setEnabled(false);
			hit.setEnabled(false);
			stand.setEnabled(false);
			textArea.append("\nROUND OVER!");
			client.getPlayer().setMessage(""); //clear the player's message
		}
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
