package cs451.Custom;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class PerfectLinkNode implements LinkNode{
	
	private BasicLinkNode basicLinkNode;
	
	/*Messages that have been sent but for which confirmation hasn't been received go here
    It maps the message sequence number to the message*/
    private Map<Integer, OutgoingPacket> unAckedPackets;
	
	private Set<MessageID> receivedMessages;
	
	private Queue<OutgoingPacket> waitingForSend;
	
	private static final float RESEND_TIMER_MILLIS = 150;
	
	private static final int WAITING_FOR_SEND_MAX_SIZE = 32;
	
	private static final int MAX_UNACKED_MESSAGES = 15_000;
	
	private static final int MAX_NB_OF_MSG_PER_PACKET = 8;
		
	private static AtomicBoolean allowCommunication;
		
	protected  CommunicationLogger logger;

    
	
    public PerfectLinkNode(InetAddress addr, int port, int processId) throws SocketException {
		basicLinkNode = new BasicLinkNode(addr, port, processId);
		unAckedPackets = new ConcurrentHashMap<Integer, OutgoingPacket>();
		receivedMessages = new HashSet<MessageID>();
		waitingForSend = new ConcurrentLinkedQueue<OutgoingPacket>();
		allowCommunication = new AtomicBoolean(true);
		this.logger = new CommunicationLogger();
	}
    
    
    public void send(OutgoingPacket packet) {
    	while(waitingForSend.size() >= WAITING_FOR_SEND_MAX_SIZE) {
    		//Do nothing and wait until the buffer values are consumed
    	}
    	waitingForSend.add(packet);
	}
    
    public void send(InetAddress dstAddr, int dstPort, int seqNb, byte[] data) {
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
    	allowCommunication.compareAndExchangeAcquire(true, false);
    	CommunicationLogger.writeLogsToFile();
    }
    
    public void runSendLoop() {
    	while(true) {
    		if(!allowCommunication.get()) {
				return;
			}
    		if(!waitingForSend.isEmpty()) {
    			if(unAckedPackets.size() < MAX_UNACKED_MESSAGES) {
	//    			System.out.println(waitingForSend.size());
		    		OutgoingPacket packet = makeNextPacketToSend(MAX_NB_OF_MSG_PER_PACKET);
		    		basicLinkNode.send(packet);
		    		unAckedPackets.put(packet.getPacketSeqNr(), packet);
					for(Message msg : packet.getMessages()) {
						logger.logSend(msg.getSequenceNumber());
					}
    			}
    		}
    	}
    }
    
    public void runUnackedSendLoop() {
    	while(true) {
    		for(OutgoingPacket packet : unAckedPackets.values()) {
    			if(!allowCommunication.get()) {
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
    		if(!allowCommunication.get()) {
				return;
			}
    		IncomingPacket packet = deliver();
    		for(Message msg : packet.getMessages()) {
        		logger.logDeliver(packet.getSrcPort(), msg.getSequenceNumber());
			}
    	}
    }    
   
    
    private Boolean handleReceivedPacket(IncomingPacket packet) {
    	Boolean shouldDeliver = false;
    	List<Message> messages = packet.getMessages();
    	int seqNr = packet.getPacketSeqNr();
    	InetAddress addr = packet.getSrcAddress();
    	int port = packet.getSrcPort();
	   
    	if(messages.size()  == 1 && messages.get(0).isAck()) {
//    		System.out.println("Ack message " + message.getSequenceNumber()  + " from " + port);
    		unAckedPackets.remove(packet.getPacketSeqNr());
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
	   Message ackMessage = new Message(true, seqNr, new byte[0]);
	   OutgoingPacket ackPacket = new OutgoingPacket(ackMessage, addr, port);
	   basicLinkNode.send(ackPacket);
   }
   
   
   private OutgoingPacket makeNextPacketToSend(int maxNbOfMessages) {
	   List<Message> messages = new LinkedList<Message>();
	   OutgoingPacket firstPacket = waitingForSend.remove();
	   InetAddress dstAddr = firstPacket.getDstAddress();
	   int dstPort = firstPacket.getDstPort();
	   
	   messages.add(firstPacket.getMessages().get(0));	//there is only one message anyways
	   for(int i = 0; !waitingForSend.isEmpty() && i < maxNbOfMessages; ++i) {
		   OutgoingPacket nextPacket = waitingForSend.peek();
		   if(nextPacket.getDstAddress().equals(dstAddr) && nextPacket.getDstPort() == dstPort) {
			   waitingForSend.remove();
			   messages.add(nextPacket.getMessages().get(0));
		   } else {
			   break;
		   }
	   }
	   return new OutgoingPacket(messages, dstAddr, dstPort);
   }
   

}
