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
import cs451.Custom.Deliverable;
import cs451.Custom.Links.PerfectLinkNode;
import cs451.Custom.Message.NetMessage;

public class URB implements BroadcastPrimitive{
	
	private List<Host> hosts;
	private int pid;
	private BEB beb;
	private Set<Long> delivered;
	//Each element of this list represents a message, and the array of booleans represents which processes have acked this message and which haven´t
	private Map<Long, AckVector> ackRecords;
	private int nextSeqNb = 1;
	private CommunicationLogger logger = CommunicationLogger.getInstance()
;	
	
	public URB(List<Host> hosts, int pid, PerfectLinkNode linkNode) {
		this.hosts = hosts;
		this.pid = pid;
		
		this.beb = new BEB(hosts, linkNode);
		this.delivered = new HashSet<>();
		this.ackRecords = new HashMap<>();
	}

	@Override
	public void broadcast(byte[] data) {
		int seqNb = getNextSeqNb();
		URBMessage urbMessage = new URBMessage(pid, seqNb, data);
		byte[] serializedMsg = URBMessageSerializer.serializeForNet(urbMessage);
		beb.broadcast(serializedMsg);
		System.out.println("size of serialized msg is " + serializedMsg.length);

		logger.logSend(seqNb);
	}

	@Override
	public Deliverable deliver() {
		List<byte[]> newDeliveries = new LinkedList<>();
		int senderPid = -1;
		while(newDeliveries.size() == 0) {
			Deliverable deliverable = beb.deliver();
			senderPid = deliverable.getSenderPid();
			List<byte[]> serializedMessages = deliverable.getData();
			System.out.println("nb of msgs in packet is " + serializedMessages.size());
			System.out.println("and msg sender id is " + senderPid);
			for(byte[] serializedMsg : serializedMessages) {
				URBMessage msg = URBMessageSerializer.deserializeFromNet(serializedMsg);
				long id = msg.getId();
				
				if(!ackRecords.containsKey(id)) {
					AckVector acksForThisMsg = new AckVector(hosts.size());
					acksForThisMsg.addAck(senderPid-1);
					acksForThisMsg.addAck(pid-1);
					ackRecords.put(id, acksForThisMsg);
					if(msg.getSrcPid() != pid) {
						beb.broadcast(serializedMsg);
					}
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
		return new Deliverable(newDeliveries, senderPid);
	}
	
	private int getNextSeqNb() {
		return nextSeqNb++;
	}
	
	

}
