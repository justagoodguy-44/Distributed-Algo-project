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
import cs451.Custom.Deliverable;
import cs451.Custom.Helpers.ProcessIDHelpers;
import cs451.Custom.Message.NetMessage;
import cs451.Custom.Message.NetMessageID;
import cs451.Custom.Network.NetworkParams;
import cs451.Custom.Packet.IncomingPacket;
import cs451.Custom.Packet.OutgoingPacket;

public class PerfectLinkNode {
	
	private BasicLinkNode basicLinkNode;
	
	/*Messages that have been sent but for which confirmation hasn't been received go here
    It maps the message sequence number to the message*/
    private Map<Integer, OutgoingPacket> unAckedPackets;
    
	private Set<NetMessageID> receivedMessages;
	
	private LinkedBlockingQueue<NetMessage> waitingForSend;
		
	private static AtomicBoolean allowCommunication;
			
	private int maxUnackedPacketsPerProcess;
	
	private int nextMsgSeqNb;

    
	
    public PerfectLinkNode(InetAddress addr, int port, int processId) throws SocketException {
		basicLinkNode = new BasicLinkNode(addr, port, processId);
		unAckedPackets = new ConcurrentHashMap<Integer, OutgoingPacket>();
		receivedMessages = new HashSet<NetMessageID>();
		waitingForSend = new LinkedBlockingQueue<NetMessage>(NetworkParams.WAITING_FOR_SEND_MAX_SIZE);
		allowCommunication = new AtomicBoolean(true);
		maxUnackedPacketsPerProcess = NetworkParams.getInstance().getMaxUnackedPacketsPerProcess();
		nextMsgSeqNb = 1;
		startRunningLoops();
	}

    
    public void send(InetAddress dstAddr, int dstPort, byte[] data) {
    	NetMessage message = new NetMessage(false, nextMsgSeqNb, data, dstAddr, dstPort);
    	try {
			waitingForSend.put(message);	//will block until space is available
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	nextMsgSeqNb++;
    }
    
	public Deliverable deliver() {
		while(!allowCommunication.get()) {
		}
		IncomingPacket packet = null;
		Boolean receivedMessageToDeliver = false;
		while(!receivedMessageToDeliver) {
			packet = basicLinkNode.receive();
			receivedMessageToDeliver = handleReceivedPacket(packet);
		}
		List<byte[]> messagesData = new LinkedList<byte[]>();
		for(NetMessage msg : packet.getMessages()) {
			messagesData.add(msg.getData());
		}
		int senderPid = ProcessIDHelpers.getIdFromPort(packet.getPort());
		return new Deliverable(messagesData, senderPid);
	}

    
    public static void simulateProcessCrash() {
    	if(allowCommunication != null) {
    		allowCommunication.compareAndExchangeAcquire(true, false);
    	}
    }
    
    
    private void startRunningLoops() {
         
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
    		if(!waitingForSend.isEmpty() && unAckedPackets.size() < maxUnackedPacketsPerProcess) {
//    			System.out.println(waitingForSend.size());
	    		OutgoingPacket packet = makeNextPacketToSend(NetworkParams.MAX_NB_OF_MSG_PER_PACKET);
	    		if(packet != null) {
		    		basicLinkNode.send(packet);
		    		unAckedPackets.put(packet.getPacketSeqNr(), packet);
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
   
    
    private Boolean handleReceivedPacket(IncomingPacket packet) {
    	Boolean shouldDeliver = false;
    	List<NetMessage> messages = packet.getMessages();
    	int seqNr = packet.getPacketSeqNr();
    	InetAddress addr = packet.getAddr();
    	int port = packet.getPort();
	   
    	if(messages.size()  == 1 && messages.get(0).isAck()) {
    		System.out.println("Received ack for message " + messages.get(0).getSequenceNumber()  + " from " + port);
    		unAckedPackets.remove(packet.getPacketSeqNr());
    	} else {
    		NetMessageID newMessageID = new NetMessageID(seqNr, addr, port);
    		sendAck(addr, port, seqNr);
    		if(!receivedMessages.contains(newMessageID)) {
    			receivedMessages.add(newMessageID);
			  	shouldDeliver = true;
    		}
    	}
    	return shouldDeliver;
	}
   
   private void sendAck(InetAddress addr, int port, int seqNr) {
	   System.out.println("Send ack for message " + seqNr);
	   NetMessage ackMessage = new NetMessage(true, seqNr, new byte[0], addr, port);
	   OutgoingPacket ackPacket = new OutgoingPacket(ackMessage);
	   basicLinkNode.send(ackPacket);
   }
   
   
   private OutgoingPacket makeNextPacketToSend(int maxNbOfMessages) {
	   List<NetMessage> messages = new LinkedList<NetMessage>();
	   NetMessage firstMsg = null;
		try {
			firstMsg = waitingForSend.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	   InetAddress dstAddr = firstMsg.getAddr();
	   int dstPort = firstMsg.getPort();
	   
	   messages.add(firstMsg);
	   for(int i = 0; !waitingForSend.isEmpty() && i < maxNbOfMessages-1; ++i) {
		   NetMessage nextMsg = waitingForSend.peek();
		   if(nextMsg.getAddr().equals(dstAddr) && nextMsg.getPort() == dstPort) {
			   waitingForSend.remove();
			   messages.add(nextMsg);
		   } else {
			   break;
		   }
	   }
	   return new OutgoingPacket(messages);
   }

   

}
