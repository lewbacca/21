import java.io.Serializable;
import java.util.ArrayList;
/**
 * Objects of this class are passed from the server to the clients.
 * They hold and array list of the active players and a player object, 
 * who sent a client package to get this corresponding server package.
 * They also hold multiple booleans to identify the current state of the game, important for the view of the client.
 *  @author 2072353l, Lyubomir Lazarov
 *
 */
public class ServerPackage implements Serializable{
	private final ArrayList<Player> players;
	private final Player you;
	private boolean roundStarted;
	private boolean justANewGuy;
	private boolean nextRound;
	private boolean roundFinished;
	public ServerPackage(ArrayList<Player> players, Player you) {
		this.players=players;
		this.you=you;
		roundStarted=false;
		nextRound=false;
		justANewGuy=false;
		roundFinished=false;
	}
	public ArrayList<Player> getPlayers() {
		return players;
	}
	public Player getPlayer() {
		return you;
	}
	public boolean isRoundStarted() {
		return roundStarted;
	}
	public void setRoundStarted(boolean roundStarted) {
		this.roundStarted = roundStarted;
	}
	public boolean isJustANewGuy() {
		return justANewGuy;
	}
	public void setJustANewGuy(boolean justANewGuy) {
		this.justANewGuy = justANewGuy;
	}
	public Player getYou() {
		return you;
	}
	public boolean isNextRound() {
		return nextRound;
	}
	public void setNextRound(boolean nextRound) {
		this.nextRound = nextRound;
	}
	public boolean isRoundFinished() {
		return roundFinished;
	}
	public void setRoundFinished(boolean roundFinished) {
		this.roundFinished = roundFinished;
	}
	
}
