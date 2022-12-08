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
	private int nbOfInstances;
	private LatticeReader latticeReader;
	private Map<Integer, LatticeAgreeInstance> instances;
	private Map<Integer, Set<Integer>> delivered;
	private Map<Integer, HashSet<Integer>> waiting;
	private BEB beb;
	private PerfectLinkNode perfectLink;
	
	private boolean noMoreProposals = false;
	private int resupplyNb = 100;

	
	private int nbOfCorrectHosts = NetworkParams.getInstance().getNbOfCorrectHosts();

	private int nextActiveInstanceId = 1;
	private int nextWaitingInstanceId = 1;
	
	public LatticeAgreeOrganizer(int nbOfInstances, LatticeReader latticeReader, BEB beb, PerfectLinkNode perfectLink) {
		this.nbOfInstances = nbOfInstances;
		this.latticeReader = latticeReader;
		this.beb = beb;
		this.perfectLink = perfectLink;
		this.instances = initInstances();
		this.delivered = new HashMap<>();
		this.waiting = new HashMap<>();
	}
	
	private Map<Integer, LatticeAgreeInstance> initInstances(){
		Map<Integer, LatticeAgreeInstance> instanceList = new HashMap<>();
		for(int i = 0; i < nbOfInstances; ++i) {
			LatticeAgreeInstance instance = new LatticeAgreeInstance(-1, beb, nbOfCorrectHosts);
			startNextAgreement(instance);
		}
		return instanceList;
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
		if(noMoreProposals) {
			return;
		}
		if(waiting.isEmpty()) {
			resupplyWaiting(resupplyNb);
		}
		instance.startNewAgreement(nextActiveInstanceId, waiting.remove(nextActiveInstanceId));
		instances.put(nextActiveInstanceId, instance);
		nextActiveInstanceId++;
	}
	
	private boolean resupplyWaiting(int nb) {
		if(noMoreProposals) {
			return false;
		}
		List<HashSet<Integer>> newProposals = latticeReader.read(nb);
		for(HashSet<Integer> proposal : newProposals) {
			waiting.put(nextWaitingInstanceId, proposal);
			nextWaitingInstanceId++;
		}
		if(newProposals.size() < nb) {
			noMoreProposals = true;
		}
		return true;
	}
	
	private void handleProposal(LatticeProposal proposal, InetAddress addr, int port) {
		int instanceId = proposal.getInstanceId();
		LatticeAgreeInstance activeInstance = instances.get(instanceId);
		LatticeResponse response = null;
		Set<Integer> currentProposal = null;
		if(activeInstance != null) {
			//There is a current instance to handle this
			currentProposal = activeInstance.getProposal(instanceId);
			response = activeInstance.handleProposal(proposal);
		}
		else if(delivered.containsKey(instanceId)) {
			//This proposal has already been delivered, so we should check what we delivered in order to respond
			currentProposal = delivered.get(instanceId);
			response = LatticeAgreeInstance.generateResponseFromProposal(delivered.get(instanceId), proposal);
		} else {
			//We haven't reached this instanceId in our waiting table yet so we should fast-forward our waiting talbe to reach it
			while(!waiting.containsKey(instanceId) && resupplyWaiting(instanceId));
			currentProposal = waiting.get(instanceId);
			response = LatticeAgreeInstance.generateResponseFromProposal(waiting.get(instanceId), proposal);
		}
		perfectLink.send(addr, port, LatticeSerializer.serializeResponseForNet(response));
		currentProposal.addAll(proposal.getProposedVals());
		
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
				logger.logAgree(deliveredProposal);
				startNextAgreement(instance);
			}
		}
	}

	

	
	

}
