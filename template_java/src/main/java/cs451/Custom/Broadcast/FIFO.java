package cs451.Custom.Broadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cs451.Host;
import cs451.Custom.CommunicationLogger;
import cs451.Custom.Links.PerfectLinkNode;
import cs451.Custom.Network.NetworkParams;

public class FIFO {
	
	private List<FIFOWaitingLine> waitingLines;
	private URB urb;
	
	private CommunicationLogger logger = CommunicationLogger.getInstance();

	
	public FIFO(List<Host> hosts, int pid, PerfectLinkNode linkNode) {
		this.waitingLines = waitingLinesInit();
		this.urb = new URB(hosts, pid, linkNode);
	}
	
	public void broadcast(byte[] data) {
		urb.broadcast(data);
	}
	
	public List<URBMessage> deliver() {
		List<URBMessage> urbMessages = urb.deliver();
		int srcPid = urbMessages.get(0).getSrcPid();
		List<URBMessage> toBeDelivered = new LinkedList<>();
		for(URBMessage msg : urbMessages) {
			FIFOWaitingLine waitingLine = waitingLines.get(srcPid-1);
			List<URBMessage> availableMsgsInThisLine = waitingLine.addAndRetrieve(msg);
			if(availableMsgsInThisLine != null) {
				toBeDelivered.addAll(availableMsgsInThisLine);
			}
		}
		for(URBMessage msg : toBeDelivered) {
			logger.logDeliver(msg.getSrcPid(), msg.getSeqNb());
		}
		return toBeDelivered;
	}
	
	
	private List<FIFOWaitingLine> waitingLinesInit(){
		List<FIFOWaitingLine> lines = new ArrayList<>();
		int nbOfHosts = NetworkParams.getInstance().getNbOfHosts();
		for(int i = 0; i < nbOfHosts; ++i) {
			lines.add(new FIFOWaitingLine());
		}
		return lines;
	}
	
}
