import java.io.Serializable;

public class ClientPackage implements Serializable {
	private final Player player;
	private boolean sit;
	private boolean hit;
	private boolean stand;
	private boolean bet;
	private boolean nextRound;
	public ClientPackage(Player player) {
		this.player=player;
		sit=false;
		hit=false;
		stand=false;
		bet=false;
	}
	public Player getPlayer() {
		return player;
	}
	public boolean isSit() {
		return sit;
	}
	public void setSit(boolean sit) {
		this.sit = sit;
	}
	public boolean isHit() {
		return hit;
	}
	public void setHit(boolean hit) {
		this.hit = hit;
	}
	public boolean isStand() {
		return stand;
	}
	public void setStand(boolean stand) {
		this.stand = stand;
	}
	public boolean isBet() {
		return bet;
	}
	public void setBet(boolean bet) {
		this.bet = bet;
	}
	public boolean isNextRound() {
		return nextRound;
	}
	public void setNextRound(boolean nextRound) {
		this.nextRound = nextRound;
	}
	
	
}
