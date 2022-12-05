package cs451.Custom.Lattice;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs451.Custom.Broadcast.BEB;
import cs451.Custom.Network.NetMessage;
import cs451.Custom.Network.NetworkParams;

public class LatticeAgreeOrganizer {
	
	private int nbOfInstances;
	private File proposalFile;
	private Map<Integer, LatticeAgreeInstance> instances;
	private BEB beb;

	
	private int nbOfCorrectHosts = NetworkParams.getInstance().getNbOfCorrectHosts();

	
	private int nextInstanceId = 1;
	
	public LatticeAgreeOrganizer(int nbOfInstances, File proposalFile, BEB beb) {
		this.nbOfInstances = nbOfInstances;
		this.proposalFile = proposalFile;
		this.beb = beb;
		this.instances = initInstances();
	}
	
	private Map<Integer, LatticeAgreeInstance> initInstances(){
		Map<Integer, LatticeAgreeInstance> instanceList = new HashMap<>();
		for(int i = 0; i < nbOfInstances; ++i) {
			LatticeAgreeInstance instance = new LatticeAgreeInstance(nextInstanceId, beb, nbOfCorrectHosts);
			instanceList.put(nextInstanceId, instance);
			nextInstanceId++;
		}
		return instanceList;
	}
	
	public void deliver() {
		List<NetMessage> netMessages = beb.deliver();
		for(NetMessage netMsg : netMessages) {
			byte[] serializedMsg = netMsg.getData();
			LatticeMsgType msgType = LatticeSerializer.getMsgType(serializedMsg);
			LatticeMsg msg = LatticeSerializer.deserializeFromNet(serializedMsg);
			int instanceId = msg.getInstanceId();
			if(msgType == LatticeMsgType.PROPOSAL) {
				handleProposal((LatticeProposal)msg);
			} else {
				handleResponse((LatticeResponse)msg);
			}
			//WHAT TO DOOOO WITH EARLY AND LAAAATE GUYYYS
		}
	}
	
	

}
