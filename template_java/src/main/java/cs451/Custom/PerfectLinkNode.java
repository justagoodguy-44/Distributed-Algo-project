package cs451.Custom;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PerfectLinkNode implements LinkNode{
	
	private BasicLinkNode basicLinkNode;
	
	/*Messages that have been sent but for which confirmation hasn't been received go here
    It maps the message sequence number to the message*/
    private Map<Integer, OutgoingPacket> unAckedMessages;
	
	private Set<MessageID> receivedMessages;
	
	private Queue<OutgoingPacket> waitingForSend;
	
	private static final float RESEND_TIMER_MILLIS = 150;
		
	private static Boolean allowCommunication;
		
	protected  CommunicationLogger logger;

    
	
    public PerfectLinkNode(InetAddress addr, int port, int processId) throws SocketException {
		basicLinkNode = new BasicLinkNode(addr, port, processId);
		unAckedMessages = new ConcurrentHashMap<Integer, OutgoingPacket>();
		receivedMessages = new HashSet<MessageID>();
		waitingForSend = new ConcurrentLinkedQueue<OutgoingPacket>();
		allowCommunication = true;
		this.logger = new CommunicationLogger();
	}
    
    
    public void send(OutgoingPacket packet) {
    	waitingForSend.add(packet);
	}
    
    public void send(InetAddress dstAddr, int dstPort, int seqNb, String data) {
    	Message message = new Message(false, seqNb, data);
    	OutgoingPacket packet = new OutgoingPacket(message, dstAddr, dstPort);
    	send(packet);
    }
    
	public IncomingPacket deliver() {
		IncomingPacket packet = null;
		Boolean receivedMessageToDeliver = false;
		while(!receivedMessageToDeliver) {
			packet = basicLinkNode.deliver();
			receivedMessageToDeliver = handleReceivedPacket(packet);
		}
//		System.out.println("Deliver message " + packet.getMessage().getSequenceNumber());
		return packet;
	}

    
    public static void simulateProcessCrash() {
    	CommunicationLogger.writeLogsToFile();
    	allowCommunication = false;
    }
    
    
    public void runSendLoop() {
    	while(true) {
    		if(!allowCommunication) {
				return;
			}
    		if(!waitingForSend.isEmpty()) {
    			System.out.println(waitingForSend.size());
	    		OutgoingPacket packet = waitingForSend.remove();
	    		basicLinkNode.send(packet);
	    		unAckedMessages.put(packet.getMessage().getSequenceNumber(), packet);
				logger.logSend(packet.getMessage().getSequenceNumber());
//				System.out.println("Send message " + packet.getMessage().getSequenceNumber());
    		}
    	}
    }
    
    public void runUnackedSendLoop() {
    	while(true) {
    		for(OutgoingPacket packet : unAckedMessages.values()) {
    			if(!allowCommunication) {
    				return;
    			}
    			long currentTimeMillis = System.currentTimeMillis();
    			if(currentTimeMillis - packet.getTimeWhenSent() > RESEND_TIMER_MILLIS) {
    				packet.setTimeWhenSent(currentTimeMillis);	//will be overwritten in Send but it is necessary to update it immediatly to avoid sending it a bunch of times
    				basicLinkNode.send(packet);
//    				System.out.println("resend message " + packet.getMessage().getSequenceNumber());
    			}
    		}
    	}
    }
    
    public void runDeliverLoop() {
    	while(true) {
    		if(!allowCommunication) {
				return;
			}
    		IncomingPacket packet = deliver();
    		logger.logDeliver(packet.getSrcPort(), packet.getMessage().getSequenceNumber());
    	}
    }    
   
    
    private Boolean handleReceivedPacket(IncomingPacket packet) {
    	Boolean shouldDeliver = false;
    	Message message = packet.getMessage();
    	int seqNr = message.getSequenceNumber();
    	InetAddress addr = packet.getSrcAddress();
    	int port = packet.getSrcPort();
	   
    	if(message.isAck()) {
//    		System.out.println("Ack recevied from " + port);
    		unAckedMessages.remove(packet.getMessage().getSequenceNumber());
    	} else {
    		MessageID newMessageID = new MessageID(seqNr, addr, port);
    		sendAck(addr, port, seqNr);
    		if(!receivedMessages.contains(newMessageID)) {
    			receivedMessages.add(newMessageID);
			  	shouldDeliver = true;
    		}
    	}
    	return shouldDeliver;
	}
   
   private void sendAck(InetAddress addr, int port, int seqNr) {
//	   System.out.println("Ack message " + seqNr);
	   Message ackMessage = new Message(true, seqNr, null);
	   OutgoingPacket ackPacket = new OutgoingPacket(ackMessage, addr, port);
	   basicLinkNode.send(ackPacket);
   }

}
