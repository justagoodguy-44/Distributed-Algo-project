package cs451.Custom.Broadcast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FIFOWaitingLine {

	private Map<Integer, URBMessage> waitingLine;
	private int lastDeliveredSeqNb;
	
	public FIFOWaitingLine() {
		waitingLine = new HashMap<>();
		lastDeliveredSeqNb = 0;
	}
	
	/**
	 * 
	 * @param msg: the message to be added to the waiting line
	 * @return: the list of messages that can be delivered after adding this message
	 */
	public List<URBMessage> addAndRetrieve(URBMessage msg){
		int msgSeqNb = msg.getSeqNb();
		if(msgSeqNb != lastDeliveredSeqNb + 1) {
			waitingLine.put(msgSeqNb, msg);
			return null;
		}
		lastDeliveredSeqNb++;
		List<URBMessage> validMessages = popValidMessages();
		validMessages.add(0, msg);
		return validMessages;

	}
	
	private List<URBMessage> popValidMessages(){
		List<URBMessage> validMessages = new LinkedList<>();
		URBMessage nextMsg = null;
		boolean checkNext = true;
		while(checkNext) {
			nextMsg = waitingLine.remove(lastDeliveredSeqNb+1);
			if(nextMsg == null) {
				checkNext = false;
			} else {
				validMessages.add(nextMsg);
				lastDeliveredSeqNb++;
			}
		}
		return validMessages;
	}
	
	
}
