package cs451.Custom;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PerfectLinkNode extends BasicLinkNode{
	/*Messages that have been sent but for which confirmation hasn't been received go here
    It maps the message sequence number to the message*/
    private Queue<OutgoingPacket> unAckedMessages;
	
	private Set<MessageID> receivedMessages;
	
	private Queue<OutgoingPacket> waitingForSend;
	
	private float RESEND_MESSAGE_TIMER = 0.5f;
		
	private Boolean allowCommunication;
	
	private int nextMessageSeqNr;
    
	
    public PerfectLinkNode(InetAddress addr, int port, int processNb) throws SocketException {
		super(addr, port, processNb);
		unAckedMessages = new ConcurrentLinkedQueue<OutgoingPacket>();
		receivedMessages = new HashSet<MessageID>();
		waitingForSend = new ConcurrentLinkedQueue<OutgoingPacket>();
		allowCommunication = true;
		nextMessageSeqNr = 1;
	}
    
    public void enqueueForSend(InetAddress dstAddr, int dstPort, String data) {
    	Message message = new Message(false, nextMessageSeqNr, data);
    	OutgoingPacket packet = new OutgoingPacket(message, dstAddr, dstPort);
    	waitingForSend.add(packet);
    }
    
    private void enqueueForSend(OutgoingPacket packet) {
    	waitingForSend.add(packet);
    }
    
    public void RunSendLoop() {
    	while(true) {
    		for(OutgoingPacket packet : waitingForSend) {
    			if(!allowCommunication) {
    				logger.Close();
    				return;
    			}
    			Send(packet);
    			waitingForSend.remove(packet);
    		}
    	}
    }
    
    public void RunUnackedSendLoop() {
    	while(true) {
    		for(OutgoingPacket packet : unAckedMessages) {
    			if(!allowCommunication) {
    				logger.Close();
    				return;
    			}
    			if(System.currentTimeMillis() - packet.getTimeWhenSent() > RESEND_MESSAGE_TIMER) {
    				enqueueForSend(packet);
    				unAckedMessages.remove(packet);
    			}
    		}
    	}
    }
    
    public void RunDeliverLoop() {
    	while(true) {
    		if(!allowCommunication) {
				logger.Close();
				return;
			}
    		IncomingPacket packet = Receive();
    		if(packet != null) {
    			Deliver(packet);
    		}
    	}
    }
    
    public void allowCommunications(Boolean value) {
    	allowCommunication = value;
    }
    
    @Override
    protected void Send(OutgoingPacket packet) {
		super.Send(packet);
		unAckedMessages.add(packet);
	}
    
   
    @Override 
	protected void Deliver(IncomingPacket packet) {
    	
	   Message message = packet.getMessage();
	   int seqNr = message.getSequenceNumber();
	   InetAddress addr = packet.getSrcAddress();
	   int port = packet.getSrcPort();
	   
	   if(message.isAck()) {
		   unAckedMessages.remove(seqNr);
	   } else {
		   MessageID newMessageID = new MessageID(seqNr, addr, port);
		   SendAck(addr, port, seqNr);
		   if(!receivedMessages.contains(newMessageID)) {
			   receivedMessages.add(newMessageID);
			   super.Deliver(packet);
		   }
	   }
	}
   
   protected void SendAck(InetAddress addr, int port, int seqNr) {
	   Message ackMessage = new Message(true, seqNr, null);
	   OutgoingPacket ackPacket = new OutgoingPacket(ackMessage, addr, port);
	   Send(ackPacket);
   }


}
