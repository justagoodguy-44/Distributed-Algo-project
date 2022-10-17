package cs451.Custom;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	
	private static final float RESEND_TIMER_MILLIS = 2000f;
		
	private Boolean allowCommunication;
	
	private int nextMessageSeqNr;
	
	protected  CommunicationLogger logger;

    
	
    public PerfectLinkNode(InetAddress addr, int port, int processNb) throws SocketException {
		super(addr, port, processNb);
		unAckedMessages = new ConcurrentHashMap<Integer, OutgoingPacket>();
		receivedMessages = new HashSet<MessageID>();
		waitingForSend = new ConcurrentLinkedQueue<OutgoingPacket>();
		allowCommunication = true;
		nextMessageSeqNr = 1;
		this.logger = new CommunicationLogger(processId);

	}
    
    public void sendNewMessage(InetAddress dstAddr, int dstPort, String data) {
    	Message message = new Message(false, nextMessageSeqNr, data);
    	OutgoingPacket packet = new OutgoingPacket(message, dstAddr, dstPort);
    	waitingForSend.add(packet);
		nextMessageSeqNr++;
    }
    
    private void enqueueForSend(OutgoingPacket packet) {
    	waitingForSend.add(packet);
    }
    
    public void RunSendLoop() {
    	while(true) {
    		if(!allowCommunication) {
				logger.Close();
				return;
			}
    		if(!waitingForSend.isEmpty()) {
	    		OutgoingPacket packet = waitingForSend.remove();
	    		Send(packet);
				logger.logSend(packet.getMessage().getSequenceNumber());
    		}
    	}
    }
    
    public void RunUnackedSendLoop() {
    	while(true) {
    		for(OutgoingPacket packet : unAckedMessages.values()) {
    			if(!allowCommunication) {
    				logger.Close();
    				return;
    			}
    			long currentTimeMillis = System.currentTimeMillis();
    			if(currentTimeMillis - packet.getTimeWhenSent() > RESEND_TIMER_MILLIS) {
    				packet.setTimeWhenSent(currentTimeMillis);	//will be overwritten in Send but it is necessary to update it immediatly to avoid sending it a bunch of times
    				enqueueForSend(packet);
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
    			logger.logDeliver(packet.getSrcPort(), packet.getMessage().getSequenceNumber());

    		}
    	}
    }
    
    public void allowCommunications(Boolean value) {
    	allowCommunication = value;
    }
    
    @Override
    protected void Send(OutgoingPacket packet) {
		super.Send(packet);
		unAckedMessages.put(packet.getMessage().getSequenceNumber(), packet);
	}
    
   
    @Override 
	protected IncomingPacket Deliver(IncomingPacket packet) {
		return packet;
	}
    
    @Override
    protected void handleReceivedPacket(IncomingPacket packet) {
		super.handleReceivedPacket(packet);
		System.out.println("a new message has arrived!");
		   Message message = packet.getMessage();
		   int seqNr = message.getSequenceNumber();
		   InetAddress addr = packet.getSrcAddress();
		   int port = packet.getSrcPort();
		   
		   if(message.isAck()) {
			   System.out.println("Ack recevied from " + port);
			   unAckedMessages.remove(packet.getMessage().getSequenceNumber());
		   } else {
			   MessageID newMessageID = new MessageID(seqNr, addr, port);
			   SendAck(addr, port, seqNr);
			   if(!receivedMessages.contains(newMessageID)) {
				   receivedMessages.add(newMessageID);
				   Deliver(packet);
			   }
		   }
		
	}
   
   protected void SendAck(InetAddress addr, int port, int seqNr) {
	   Message ackMessage = new Message(true, seqNr, null);
	   OutgoingPacket ackPacket = new OutgoingPacket(ackMessage, addr, port);
	   super.Send(ackPacket);
   }


}
