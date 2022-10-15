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
    private Map<Integer, OutgoingPacket> unAckedMessages;
	
	private Set<MessageID> receivedMessages;
	
	private Queue<OutgoingPacket> waitingForSend;
	
	private float RESEND_MESSAGE_TIMER = 0.5f;
	
	private CommunicationLogger logger;
    
	
    public PerfectLinkNode(InetAddress addr, int port) throws SocketException {
		super(addr, port);
		unAckedMessages = new ConcurrentHashMap<Integer, OutgoingPacket>();
		receivedMessages = new HashSet<MessageID>();
		waitingForSend = new ConcurrentLinkedQueue<OutgoingPacket>();
	}
    
    public void enqueueForSend(OutgoingPacket packet) {
    	waitingForSend.add(packet);
    }
    
    public void RunSendLoop() {
    	while(true) {
    		for(OutgoingPacket packet : waitingForSend) {
    			Send(packet);
    		}
    	}
    }
    
    public void RunUnackedSendLoop() {
    	while(true) {
    		for(OutgoingPacket packet : unAckedMessages.values()) {
    			if(System.currentTimeMillis() - packet.getTimeWhenSent() > RESEND_MESSAGE_TIMER) {
    				enqueueForSend(packet);
    			}
    		}
    	}
    }
    
    public void RunDeliverLoop() {
    	while(true) {
    		IncomingPacket packet = Receive();
    		if(packet != null) {
    			Deliver(packet);
    		}
    	}
    }
    
    @Override
    protected void Send(OutgoingPacket packet) {
		super.Send(packet);
		unAckedMessages.put(packet.getMessage().getSequenceNumber(), packet);
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
