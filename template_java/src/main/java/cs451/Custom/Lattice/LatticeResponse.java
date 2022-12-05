package cs451.Custom.Lattice;

import java.util.Set;

public class LatticeResponse extends LatticeMsg {
	
	private boolean isAck;
	//Will be null if isAck is true, and otherwise contains the values to be added to the original proposal
	private Set<Integer> missingVals;
	
	public LatticeResponse(int agreementInstanceId, int proposalNb, boolean isAck, Set<Integer> missingVals) {
		super(agreementInstanceId, proposalNb);
		this.isAck = isAck;
		this.missingVals = missingVals;
	}
	
	public boolean isAck() {
		return isAck;
	}
	
	public Set<Integer> getMissingVals(){
		return missingVals;
	}

}
