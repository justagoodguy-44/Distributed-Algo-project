package cs451.Custom;


public class AckVector {
	
	private boolean[] acks;
	private int nbOfAcks;
	
	public AckVector(int size) {
		this.acks = new boolean[size];
		
	}
	
	public void addAck(int pos) {
		if(acks[pos]) {
			return;
		}
		acks[pos] = true;
		nbOfAcks++;
	}
	
	public boolean[] getAcks() {
		return acks;
	}
	
	public int getNbOfAcks() {
		return nbOfAcks;
	}
}
