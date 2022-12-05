package cs451.Custom.Broadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cs451.Host;
import cs451.Custom.AckVector;
import cs451.Custom.CommunicationLogger;
import cs451.Custom.CompactedValueRecord;
import cs451.Custom.Helpers.ProcessIDHelpers;
import cs451.Custom.Links.PerfectLinkNode;
import cs451.Custom.Network.NetMessage;
import cs451.Custom.Network.NetworkParams;

public class URB {
	
	private List<Host> hosts;
	private int pid;
	private BEB beb;
	private List<CompactedValueRecord> delivered;
	//Each element of this list represents a message, and the array of booleans represents which processes have acked this message and which haven´t
	private Map<Long, AckVector> ackRecords;
	private int nextSeqNb = 1;
	private int sentButNotDelivered = 0;
	private int maxSentButNotDelivered;
	private CommunicationLogger logger = CommunicationLogger.getInstance();
;	
	
	public URB(List<Host> hosts, int pid, PerfectLinkNode linkNode) {
		this.hosts = hosts;
		this.pid = pid;
		this.beb = new BEB(hosts, linkNode);
		this.delivered = deliveredInit();
		this.ackRecords = new HashMap<>();
		this.maxSentButNotDelivered = 3;
		System.out.println("My pid is " + pid);
	}

	
	public void broadcast(byte[] data) {
		while(sentButNotDelivered > maxSentButNotDelivered) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int seqNb = getNextSeqNb();
		URBMessage urbMessage = new URBMessage(pid, seqNb, data);
		byte[] serializedMsg = URBMessageSerializer.serializeForNet(urbMessage);
		beb.broadcast(serializedMsg);
		logger.logSend(seqNb);
		sentButNotDelivered++;
	}

	
	public List<URBMessage> deliver() {
		List<URBMessage> toBeDelivered = new LinkedList<>();
		while(toBeDelivered.size() == 0) {
			List<NetMessage> netMessages = beb.deliver();
			int senderPid = ProcessIDHelpers.getIdFromPort(netMessages.get(0).getPort());
//			System.out.println("nb of msgs in packet is " + serializedMessages.size());
//			System.out.println("and msg sender id is " + senderPid);
			for(NetMessage netMsg : netMessages) {
				byte[] serializedMsg = netMsg.getData();
				URBMessage msg = URBMessageSerializer.deserializeFromNet(serializedMsg);
				long id = msg.getId();
				int srcPid = msg.getSrcPid();
				int seqNb = msg.getSeqNb();
				if(!delivered.get(srcPid-1).contains(seqNb)) {
					if(!ackRecords.containsKey(id)) {
						AckVector acksForThisMsg = new AckVector(hosts.size());
						acksForThisMsg.addAck(senderPid-1);
						acksForThisMsg.addAck(pid-1);
						ackRecords.put(id, acksForThisMsg);
						if(srcPid != pid) {
							beb.broadcast(serializedMsg);
						}
					}
					else {
						AckVector acksForThisMsg = ackRecords.get(id);
						acksForThisMsg.addAck(senderPid-1);
						if(acksForThisMsg.getNbOfAcks() > hosts.size()/2) {
							delivered.get(srcPid-1).add(seqNb);
							toBeDelivered.add(msg);
							ackRecords.remove(id);
							if(srcPid == pid) {
								sentButNotDelivered--;
							}
						}
					}
				}
			}
		}
		return toBeDelivered;
	}
	
	private int getNextSeqNb() {
		return nextSeqNb++;
	}
	
	
	  private List<CompactedValueRecord> deliveredInit(){
			List<CompactedValueRecord> lines = new ArrayList<>();
			int nbOfHosts = NetworkParams.getInstance().getNbOfHosts();
			for(int i = 0; i < nbOfHosts; ++i) {
				lines.add(new CompactedValueRecord());
			}
			return lines;
		}
	
	

}
