package cs451.Custom.Broadcast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cs451.Host;
import cs451.Custom.Links.PerfectLinkNode;
import cs451.Custom.Message.NetMessage;

public class UniformReliableBroadcast implements BroadcastPrimitive{
	
	private List<Host> hosts;
	private int pid;
	private BestEffortBroadcast beb;
	private List<URBMessageID> delivered;
	private List<URBMessage> pending;
	//Each element of this list represents a message, and the array of booleans represents which processes have acked this message and which haven´t
	private Map<URBMessageID, boolean[]> ackRecords;
	private int nextSeqNb = 1;
	
	
	public UniformReliableBroadcast(List<Host> hosts, int pid, PerfectLinkNode linkNode) {
		this.hosts = hosts;
		this.pid = pid;
		
		this.beb = new BestEffortBroadcast(hosts, linkNode);
		this.delivered = new LinkedList<URBMessageID>();
		this.pending = new LinkedList<URBMessage>();
		this.ackRecords = new HashMap<URBMessageID, boolean[]>();
	}

	@Override
	public void broadcast(byte[] data) {
		int seqNb = getNextSeqNb();
		URBMessage urbMessage = new URBMessage(pid, seqNb, data);
		pending.add(urbMessage);
		beb.broadcast(urbMessage.serialize());
	}

	@Override
	public List<byte[]> deliver() {
		
//		URBMessageID msgId = new URBMessageID(msg., nextSeqNb)
		return null;
	}
	
	private int getNextSeqNb() {
		nextSeqNb++;
		return nextSeqNb - 1;
	}
	
	

}
