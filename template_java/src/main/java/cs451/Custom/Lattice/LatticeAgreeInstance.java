package cs451.Custom.Lattice;

import java.util.HashSet;
import java.util.List;
import cs451.Host;
import cs451.Custom.CommunicationLogger;
import cs451.Custom.Broadcast.BEB;
import cs451.Custom.Links.PerfectLinkNode;
import cs451.Custom.Network.NetworkParams;

public class LatticeAgreeInstance {
	
	private boolean active = false;
	private ActiveProposalHandler proposalHandler = new ActiveProposalHandler();
	private CommunicationLogger logger = CommunicationLogger.getInstance();
	private int nbOfCorrectHosts;
	private int instanceId;
	private BEB beb;
		
	public LatticeAgreeInstance(int instanceId, BEB beb, int nbOfCorrectHosts) {
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
		active = true;
		byte[] serializedProposal = LatticeSerializer.serializeProposalForNet(instanceId, proposalNb, proposal);
		beb.broadcast(serializedProposal);
	}

	
	private void handleProposal(LatticeProposal proposal) {
		//CASE PAST PROPOSAL
		//CASE CURRENT PROPOSAL
		//CASE FUTURE PROPOSAL
	}
	
	/**
	 * @return whether the proposal was delivered after receiving this response
	 */
	private boolean handleResponse(LatticeResponse response) {
		int proposalNb = response.getProposalNb();
		if(!proposalHandler.isActiveProposalNb(proposalNb)) {
			return false;
		}
		boolean delivered = false;
		if(response.isAck()) {
			int nbOfAcks = proposalHandler.addAck(proposalNb);
			if(nbOfAcks >= nbOfCorrectHosts && active) {
				//DELIVERRRRRRRR
				active = false;
				delivered = true;
				logger.logAgree(proposalHandler.getProposal(response.getProposalNb()));
			}
		} else {
			HashSet<Integer> newProposals = proposalHandler.getProposal(proposalNb);
			newProposals.addAll(response.getMissingVals());
			proposalHandler.addNack(proposalNb);
			propose(newProposals);
		}
		return delivered;
	}


	

}
