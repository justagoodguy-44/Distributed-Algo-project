package cs451.Custom;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PerfectLinks extends BasicLink{
	/*Messages that have been sent but for which confirmation hasn't been received go here
    It maps the message sequence number to the message*/
    private Map<Integer, OutgoingPacket> unAckedMessages;
	
	private Set<MessageID> receivedMessages;
	
	private Queue<OutgoingPacket> waitingForSend;
	
	private float RESEND_MESSAGE_TIMER = 0.5f;
    
	
    public PerfectLinks(InetAddress addr, int port) throws SocketException {
		super(addr, port);
		unAckedMessages = new HashMap<Integer, OutgoingPacket>();
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
    		//HMMMMMMMMMMMMMMMMMMMMMMMMMMMM
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
	protected void Deliver(IncomingPacket receivedPacket) {
    	
	   Message message = receivedPacket.getMessage();
	   int seqNr = message.getSequenceNumber();
	   InetAddress addr = receivedPacket.getSrcAddress();
	   int port = receivedPacket.getSrcPort();
	   
	   if(message.isAck()) {
		   unAckedMessages.remove(seqNr);
		   SendAck(addr, port, seqNr);
	   } else {
		   MessageID newMessageID = new MessageID(seqNr, addr, port);
		   if(!receivedMessages.contains(newMessageID)) {
			   receivedMessages.add(newMessageID);
			   super.Deliver(receivedPacket);
		   }
	   }
	}
   
   protected void SendAck(InetAddress addr, int port, int seqNr) {
	   Message ackMessage = new Message(true, seqNr, null);
	   OutgoingPacket ackPacket = new OutgoingPacket(ackMessage, addr, port);
	   Send(ackPacket);
   }

}
