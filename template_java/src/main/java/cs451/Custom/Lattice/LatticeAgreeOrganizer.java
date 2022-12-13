package cs451.Custom.Lattice;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import cs451.Custom.CommunicationLogger;
import cs451.Custom.Broadcast.BEB;
import cs451.Custom.Links.PerfectLinkNode;
import cs451.Custom.Network.NetMessage;
import cs451.Custom.Network.NetworkParams;

public class LatticeAgreeOrganizer {
	
	private CommunicationLogger logger = CommunicationLogger.getInstance();
	private int nbOfSimultaneousOneShots;
	private LatticeReader latticeReader;
	private Map<Integer, LatticeAgreeInstance> instances = new HashMap<>();
	private Map<Integer, Set<Integer>> delivered = new HashMap<>();
	private Map<Integer, HashSet<Integer>> waiting = new HashMap<>();
	private BEB beb;
	private PerfectLinkNode perfectLink;
	
	private boolean noMoreProposalsInFile = false;
	private int resupplyNb = 100;

	
	private int nbOfCorrectHosts = NetworkParams.getInstance().getNbOfCorrectHosts();

	private int nextActiveInstanceId = 1;
	private int nextWaitingInstanceId = 1;
	
	public LatticeAgreeOrganizer(int nbOfSimultaneousOneShots, LatticeReader latticeReader, BEB beb, PerfectLinkNode perfectLink) {
		this.nbOfSimultaneousOneShots = nbOfSimultaneousOneShots;
		this.latticeReader = latticeReader;
		this.beb = beb;
		this.perfectLink = perfectLink;
		initInstances();
	}
	
	private void initInstances(){
		for(int i = 0; i < nbOfSimultaneousOneShots; ++i) {
			LatticeAgreeInstance instance = new LatticeAgreeInstance(beb, nbOfCorrectHosts);
			startNextAgreement(instance);
		}
	}
	
	public void deliver() {
		List<NetMessage> netMessages = beb.deliver();
		for(NetMessage netMsg : netMessages) {
			byte[] serializedMsg = netMsg.getData();
			LatticeMsgType msgType = LatticeSerializer.getMsgType(serializedMsg);
			LatticeMsg msg = LatticeSerializer.deserializeFromNet(serializedMsg);
			if(msgType == LatticeMsgType.PROPOSAL) {
				handleProposal((LatticeProposal)msg, netMsg.getAddr(), netMsg.getPort());
			} else {
				handleResponse((LatticeResponse)msg);
			}
		}
	}
	
	private void startNextAgreement(LatticeAgreeInstance instance){
		if(waiting.isEmpty()) {
			if(noMoreProposalsInFile) {
				return;
			}
			resupplyWaiting(resupplyNb);
		}
		instance.startNewAgreement(nextActiveInstanceId, waiting.remove(nextActiveInstanceId));
		instances.put(nextActiveInstanceId, instance);
		nextActiveInstanceId++;
	}
	
	private boolean resupplyWaiting(int nb) {
		if(noMoreProposalsInFile) {
			return false;
		}
		List<HashSet<Integer>> newProposals = latticeReader.read(nb);
		System.out.println("newproposals " + newProposals.size());
		for(HashSet<Integer> proposal : newProposals) {
			waiting.put(nextWaitingInstanceId, proposal);
			nextWaitingInstanceId++;
		}
		if(newProposals.size() < nb) {
			noMoreProposalsInFile = true;
		}
		return true;
	}
	
	private void handleProposal(LatticeProposal proposal, InetAddress addr, int port) {
		int instanceId = proposal.getInstanceId();
		LatticeResponse response = null;
		Set<Integer> currentProposal = null;
		LatticeAgreeInstance activeInstance = instances.get(instanceId);
		if(activeInstance != null) {
			//There is a current instance to handle this
			response = activeInstance.handleProposal(proposal);
			currentProposal = activeInstance.getLatestProposal();
			currentProposal.addAll(proposal.getProposedVals());
		}
		else if(delivered.containsKey(instanceId)) {
			//This proposal has already been delivered, so we should check what we delivered in order to respond
			response = LatticeAgreeInstance.generateResponseFromProposal(delivered.get(instanceId), proposal);
		} else {
			//We haven't reached this instanceId in our waiting table yet so we should fast-forward our waiting talbe to reach it
			while(!waiting.containsKey(instanceId) && resupplyWaiting(resupplyNb));
			response = LatticeAgreeInstance.generateResponseFromProposal(waiting.get(instanceId), proposal);
			currentProposal = waiting.get(instanceId);
			currentProposal.addAll(proposal.getProposedVals());
		}
		perfectLink.send(addr, port, LatticeSerializer.serializeResponseForNet(response));
	}
	
	private void handleResponse(LatticeResponse response) {
		int instanceId = response.getInstanceId();
		LatticeAgreeInstance instance = instances.get(instanceId);
		if(instance != null) {
			boolean canDeliver = instance.handleResponse(response);
			if(canDeliver) {
				Set<Integer> deliveredProposal = instance.getProposal(response.getProposalNb());
				delivered.put(instanceId, deliveredProposal);
				instances.remove(instanceId);
				logger.logAgree(instanceId, deliveredProposal);
				startNextAgreement(instance);
			}
		}
	}

	

	
	

}
