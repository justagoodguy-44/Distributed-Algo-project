package cs451.Custom.Broadcast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cs451.Host;
import cs451.Custom.AckVector;
import cs451.Custom.CommunicationLogger;
import cs451.Custom.Links.PerfectLinkNode;
import cs451.Custom.Message.NetMessage;

public class URB implements BroadcastPrimitive{
	
	private List<Host> hosts;
	private int pid;
	private BestEffortBroadcast beb;
	private Set<Long> delivered;
	//Each element of this list represents a message, and the array of booleans represents which processes have acked this message and which haven´t
	private Map<Long, AckVector> ackRecords;
	private int nextSeqNb = 1;
	private CommunicationLogger logger = CommunicationLogger.getInstance()
;	
	
	public URB(List<Host> hosts, int pid, PerfectLinkNode linkNode) {
		this.hosts = hosts;
		this.pid = pid;
		
		this.beb = new BestEffortBroadcast(hosts, linkNode);
		this.delivered = new HashSet<>();
		this.ackRecords = new HashMap<>();
	}

	@Override
	public void broadcast(byte[] data) {
		int seqNb = getNextSeqNb();
		URBMessage urbMessage = new URBMessage(pid, seqNb, data);
		beb.broadcast(URBMessageSerializer.serializeForNet(urbMessage));
		logger.logSend(seqNb);
	}

	@Override
	public List<byte[]> deliver() {
		List<byte[]> newDeliveries = new LinkedList<>();
		while(newDeliveries.size() == 0) {
			List<byte[]> serializedMessages = beb.deliver();
			for(byte[] serializedMsg : serializedMessages) {
				URBMessage msg = URBMessageSerializer.deserializeFromNet(serializedMsg);
				long id = msg.getId();
				
				if(!ackRecords.containsKey(id)) {
					AckVector acksForThisMsg = new AckVector(hosts.size());
					acksForThisMsg.addAck(msg.getSrcPid()-1);
					acksForThisMsg.addAck(pid-1);
					ackRecords.put(id, acksForThisMsg);
					beb.broadcast(serializedMsg);
				}
				else {
					AckVector acksForThisMsg = ackRecords.get(id);
					acksForThisMsg.addAck(msg.getSrcPid());
					if(acksForThisMsg.getNbOfAcks() > hosts.size()/2) {
						if(!delivered.contains(id)) {
							delivered.add(id);
							logger.logDeliver(msg.getSrcPid(), msg.getSeqNb());
							newDeliveries.add(msg.getData());
						}
					}
				}
			}
		}
		return newDeliveries;
	}
	
	private int getNextSeqNb() {
		return nextSeqNb++;
	}
	
	

}
