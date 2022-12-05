package cs451.Custom.Lattice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ActiveProposalHandler {

	private List<Integer> ackCount;
	private List<Integer> nackCount;
	private int nextProposalNb = 1;
	private List<Integer> activeProposalNb;
	//List of current proposals which might still receive f+1 acks
	private List<HashSet<Integer>> activeProposedVals;
	
	public ActiveProposalHandler() {
		this.ackCount = new ArrayList<>();
		this.nackCount = new ArrayList<>();
		this.activeProposalNb = new ArrayList<>();
		this.activeProposedVals = new ArrayList<HashSet<Integer>>();
	}
	
	public boolean isActiveProposalNb(int proposalNb) {
		return activeProposalNb.contains(proposalNb);
	}
	
	/**
	 * Adds the proposal and returns the proposal number associated to it
	 */
	public int addProposal(HashSet<Integer> proposedVals) {
		activeProposalNb.add(nextProposalNb);
		nextProposalNb++;
		activeProposedVals.add(proposedVals);
		ackCount.add(0);
		nackCount.add(0);
		return nextProposalNb - 1;
	}
	
	public boolean removeProposal(int proposalNb) {
		int proposalIdx = activeProposalNb.indexOf(proposalNb);
		if(proposalIdx < 0) {
			return false;
		}
		ackCount.remove(proposalIdx);
		nackCount.remove(proposalIdx);
		activeProposalNb.remove(proposalIdx);
		activeProposedVals.remove(proposalIdx);
		return true;
	}
	
	public int addAck(int proposalNb) {
		int proposalIdx = activeProposalNb.indexOf(proposalNb);
		if(proposalIdx < 0) {
			return -1;
		}
		int updatedAcks = ackCount.get(proposalIdx) + 1;
		ackCount.set(proposalIdx, updatedAcks);
		return updatedAcks;
	}
	
	public int addNack(int proposalNb) {
		int proposalIdx = activeProposalNb.indexOf(proposalNb);
		if(proposalIdx < 0) {
			return -1;
		}
		int updatedNacks = nackCount.get(proposalIdx) + 1;
		nackCount.set(proposalIdx, updatedNacks);
		return updatedNacks;
	}
	
	public void clear() {
		activeProposalNb.clear();
		activeProposedVals.clear();
		ackCount.clear();
		nackCount.clear();
	}
	
	public int getAckCount(int proposalNb) {
		int proposalIdx = activeProposalNb.indexOf(proposalNb);
		if(proposalIdx < 0) {
			return -1;
		}
		return ackCount.get(proposalIdx);
	}
	
	public int getNackCount(int proposalNb) {
		int proposalIdx = activeProposalNb.indexOf(proposalNb);
		if(proposalIdx < 0) {
			return -1;
		}
		return nackCount.get(proposalIdx);
	}
	
	public HashSet<Integer> getProposal(int proposalNb){
		int proposalIdx = activeProposalNb.indexOf(proposalNb);
		if(proposalIdx < 0) {
			return null;
		}
		return activeProposedVals.get(proposalIdx);
	}


}
