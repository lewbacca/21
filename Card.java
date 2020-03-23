import java.io.Serializable;

public class Card implements Serializable {
	private String suit = null;
	private String name = null;
	private int value =0;
	public Card(String suit, String name){
		this.suit=suit;
		this.name=name;
		assignValue();
	}
	private void assignValue() {
		if(name.equals("King") || name.equals("Queen") || name.equals("Jack")) {
			value=10;
		}else if(name.equals("Ace")) {
			value=11;
		}else {
			value=Integer.parseInt(name);
		}
	}
	@Override
	public String toString() {
		return name+" of " + suit;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	
}
