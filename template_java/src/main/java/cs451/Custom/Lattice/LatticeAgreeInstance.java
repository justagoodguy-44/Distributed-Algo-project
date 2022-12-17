package cs451.Custom.Lattice;

import java.util.HashSet;
import java.util.Set;
import cs451.Custom.Broadcast.BEB;

public class LatticeAgreeInstance {
	
	private ActiveProposalHandler proposalHandler = new ActiveProposalHandler();
	private int nbOfCorrectHosts;
	private int instanceId;
	private BEB beb;
		
	public LatticeAgreeInstance(BEB beb, int nbOfCorrectHosts) {
		this.beb = beb;
		this.nbOfCorrectHosts = nbOfCorrectHosts;
	}
	
	public void startNewAgreement(int instanceId, HashSet<Integer> proposal) {
		this.instanceId = instanceId;
		proposalHandler.clear();
		propose(proposal);
	}
	
	private void propose(HashSet<Integer> proposal) {
		int proposalNb = proposalHandler.addProposal(proposal);
		byte[] serializedProposal = LatticeSerializer.serializeProposalForNet(instanceId, proposalNb, proposal);
		beb.broadcast(serializedProposal);
	}
	
	/*
	 * Generates a response and adds values from the incoming proposal into the current proposal
	 */
	public static LatticeResponse generateResponseFromProposal(Set<Integer> myCurrentProposalVals, LatticeProposal incomingProposal) {
		Set<Integer> proposedVals = incomingProposal.getProposedVals();
		Set<Integer> myLatestProposalCopy = new HashSet<Integer>(myCurrentProposalVals);
		myLatestProposalCopy.removeAll(proposedVals);
		boolean isAck = myLatestProposalCopy.isEmpty();
		return new LatticeResponse(incomingProposal.getInstanceId(), incomingProposal.getProposalNb(), isAck, myLatestProposalCopy);
	}
	
	
	public LatticeResponse handleProposal(LatticeProposal incomingProposal) {
		return LatticeAgreeInstance.generateResponseFromProposal(proposalHandler.getLatestProposal(), incomingProposal);
	}
	
	/**
	 * @return whether the proposal was delivered after receiving this response
	 */
	public boolean handleResponse(LatticeResponse response) {
		int proposalNb = response.getProposalNb();
		if(!proposalHandler.isActiveProposalNb(proposalNb)) {
			return false;
		}
		boolean canDeliver = false;
		if(response.isAck()) {
			int nbOfAcks = proposalHandler.addAck(proposalNb);
			if(nbOfAcks >= nbOfCorrectHosts) {
				canDeliver = true;
			}
		} else {
			HashSet<Integer> newProposal = proposalHandler.getProposal(proposalNb);
			newProposal.addAll(response.getMissingVals());
			int nackCount = proposalHandler.addNack(proposalNb);
			if(nackCount >= nbOfCorrectHosts) {
				proposalHandler.removeProposal(proposalNb);
			}
			propose(newProposal);
		}
		return canDeliver;
	}
	
	
	public HashSet<Integer> getProposal(int proposalNb){
		return proposalHandler.getProposal(proposalNb);
	}
	
	public HashSet<Integer> getLatestProposal(){
		return proposalHandler.getLatestProposal();
	}

	
	
	


	

}
