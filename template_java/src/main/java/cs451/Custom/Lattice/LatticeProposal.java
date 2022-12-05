package cs451.Custom.Lattice;

import java.util.Set;

public class LatticeProposal extends LatticeMsg {

	private Set<Integer> proposedVals;
	
	public LatticeProposal(int agreementInstanceId, int proposalNb, Set<Integer> proposedVals) {
		super(agreementInstanceId, proposalNb);
		this.proposedVals = proposedVals;
	}
	
	public Set<Integer> getProposedVals(){
		return proposedVals;
	}
	
	
}
