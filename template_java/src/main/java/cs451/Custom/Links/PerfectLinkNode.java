package cs451.Custom.Links;

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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Custom.CommunicationLogger;
import cs451.Custom.Message.Message;
import cs451.Custom.Message.MessageID;
import cs451.Custom.Network.IncomingPacket;
import cs451.Custom.Network.NetworkParams;
import cs451.Custom.Network.OutgoingPacket;

public class PerfectLinkNode {
	
	private BasicLinkNode basicLinkNode;
	
	/*Messages that have been sent but for which confirmation hasn't been received go here
    It maps the message sequence number to the message*/
    private Map<Integer, OutgoingPacket> unAckedPackets;
    
	private Set<MessageID> receivedMessages;
	
	private LinkedBlockingQueue<Message> waitingForSend;
		
	private static AtomicBoolean allowCommunication;
		
	protected static CommunicationLogger logger;
	
	private int maxUnackedPacketsPerProcess;
	
	private int nextMsgSeqNb;

    
	
    public PerfectLinkNode(InetAddress addr, int port, int processId) throws SocketException {
		basicLinkNode = new BasicLinkNode(addr, port, processId);
		unAckedPackets = new ConcurrentHashMap<Integer, OutgoingPacket>();
		receivedMessages = new HashSet<MessageID>();
		waitingForSend = new LinkedBlockingQueue<Message>(NetworkParams.WAITING_FOR_SEND_MAX_SIZE);
		allowCommunication = new AtomicBoolean(true);
		logger = new CommunicationLogger();
		maxUnackedPacketsPerProcess = NetworkParams.getInstance().getMaxUnackedPacketsPerProcess();
		nextMsgSeqNb = 1;
		startRunningLoops();
	}

    
    public void send(InetAddress dstAddr, int dstPort, byte[] data) {
    	Message message = new Message(nextMsgSeqNb, data, dstAddr, dstPort);
    	try {
			waitingForSend.put(message);	//will block until space is available
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	nextMsgSeqNb++;
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
    	if(allowCommunication != null && logger != null) {
    		allowCommunication.compareAndExchangeAcquire(true, false);
        	CommunicationLogger.writeLogsToFile();
    	}
    }
    
    
    private void startRunningLoops() {
    	 Thread deliverThread = new Thread() {
         	@Override
             public void run() {
                 runDeliverLoop();
             }
         };
         deliverThread.start();
         
         Thread sendThread = new Thread() {
         	@Override
             public void run() {
                 runSendLoop();
             }
         };
         sendThread.start();
       
         Thread sendUnackedThread = new Thread() {
         	@Override
             public void run() {
                 runUnackedSendLoop();
             }
         };
         sendUnackedThread.start();
    }
    
    public void runSendLoop() {
    	while(true) {
    		if(!allowCommunication.get()) {
				return;
			}
    		if(!waitingForSend.isEmpty() || unAckedPackets.size() < maxUnackedPacketsPerProcess) {
//    			System.out.println(waitingForSend.size());
	    		OutgoingPacket packet = makeNextPacketToSend(NetworkParams.MAX_NB_OF_MSG_PER_PACKET);
	    		if(packet != null) {
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
    			if(currentTimeMillis - packet.getTimeWhenSent() > NetworkParams.RESEND_TIMER_MILLIS) {
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
	   Message firstMsg = null;
		try {
			firstMsg = waitingForSend.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	   InetAddress dstAddr = firstMsg.getAddr();
	   int dstPort = firstMsg.getPort();
	   
	   messages.add(firstPacket.getMessages().get(0));	//there is only one message anyways
	   for(int i = 0; !waitingForSend.isEmpty() && i < maxNbOfMessages-1; ++i) {
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
