package cs451.Custom.Links;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import cs451.Custom.CompactedValueRecord;
import cs451.Custom.WaitingMsgQueue;
import cs451.Custom.Helpers.ProcessIDHelpers;
import cs451.Custom.Network.NetMessage;
import cs451.Custom.Network.NetworkParams;
import cs451.Custom.Packet.IncomingPacket;
import cs451.Custom.Packet.OutgoingPacket;

public class PerfectLinkNode {
	
	private BasicLinkNode basicLinkNode;
	
	/*Messages that have been sent but for which confirmation hasn't been received go here
    It maps the message sequence number to the message*/
    private List<Map<Integer, OutgoingPacket>> unAckedPackets;
    
	private List<CompactedValueRecord> delivered;
	
	//the outer array represents the different hosts, and each has a list of messages that need to be sent to it
	private List<WaitingMsgQueue> waitingForSendOther; 
	private List<WaitingMsgQueue> waitingForSendNew; 
		
	private static AtomicBoolean allowCommunication;
			
	private int maxSmallestUnackedPacketsPerProcess;
	
	private int nextMsgSeqNb;
	
	private int nextDstPid;
	
	private static int MAX_SKIP_COUNT = 100;
	

    
	
    public PerfectLinkNode(InetAddress addr, int port, int processId) throws SocketException {
		basicLinkNode = new BasicLinkNode(addr, port, processId);
		unAckedPackets = unackedPacketsInit();
		delivered = deliveredInit();
		waitingForSendOther = initWaitingForSend(MAX_SKIP_COUNT, Integer.MAX_VALUE);
		waitingForSendNew = initWaitingForSend(MAX_SKIP_COUNT, NetworkParams.WAITING_FOR_SEND_MAX_SIZE);
		allowCommunication = new AtomicBoolean(true);
		maxSmallestUnackedPacketsPerProcess = NetworkParams.getInstance().getMaxSmallestUnackedPacketsPerProcess();
		nextMsgSeqNb = 1;
		nextDstPid = 1;
		startRunningLoops();
	}
    
    /**
     * 
     * @param dstAddr
     * @param dstPort
     * @param data
     * @param isNewData specifies whether this message is new data sent from this host (true), or a message as part of a communication protocol (false)
     */
    public void send(InetAddress dstAddr, int dstPort, byte[] data, boolean isNewData) {
    	NetMessage message = new NetMessage(false, nextMsgSeqNb, data, dstAddr, dstPort);
    	try {
    		if(isNewData) {
    			//will block until space is available
    			waitingForSendNew.get(ProcessIDHelpers.getIdFromPort(dstPort)-1).getWaitingMessages().put(message);	
    		}
    		else {
    			waitingForSendOther.get(ProcessIDHelpers.getIdFromPort(dstPort)-1).getWaitingMessages().put(message);
    		}
    	} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	nextMsgSeqNb++;
    }
    
    
	public List<NetMessage> deliver() {
		while(!allowCommunication.get()) {
		}
		IncomingPacket packet = null;
		Boolean receivedMessageToDeliver = false;
		while(!receivedMessageToDeliver) {
			packet = basicLinkNode.receive();
			receivedMessageToDeliver = handleReceivedPacket(packet);
		}
		return packet.getMessages();
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
    	int nbOfHosts = NetworkParams.getInstance().getNbOfHosts();
    	while(true) {
    		if(!allowCommunication.get()) {
				return;
			}
    		WaitingMsgQueue waitingQueue = waitingForSendOther.get(nextDstPid-1);
    		Queue<NetMessage> waitingMessages = waitingQueue.getWaitingMessages();
    		boolean sendingNewData = false;
    		if(waitingQueue.getWaitingMessages().isEmpty()) {
    			waitingQueue = waitingForSendNew.get(nextDstPid-1);
    			waitingMessages = waitingQueue.getWaitingMessages();
    			sendingNewData = true;
    		}
    		if(waitingMessages.size() < NetworkParams.MAX_NB_OF_MSG_PER_PACKET && waitingQueue.canSkip()) {
    			waitingQueue.skip();
    		}
    		else if(!waitingMessages.isEmpty()) {
    			if(!sendingNewData || smallestUnackedLineCount() < maxSmallestUnackedPacketsPerProcess) {
	//    			System.out.println(waitingForSend.size());
		    		OutgoingPacket packet = makeNextPacketToSend(waitingMessages, NetworkParams.MAX_NB_OF_MSG_PER_PACKET);
		    		waitingQueue.resetSkipCounter();
	//	    		System.out.println("Nb of msg in packet is " + packet.getMessages().size());
		    		if(packet != null) {
			    		basicLinkNode.send(packet);
			    		int dstPid = ProcessIDHelpers.getIdFromPort(packet.getPort());
			    		unAckedPackets.get(dstPid-1).put(packet.getPacketSeqNr(), packet);
		    		}
    			}
    		}
    		
    		nextDstPid = (nextDstPid+1)%(nbOfHosts+1);
    		if(nextDstPid == 0) {
    			nextDstPid++;
    		}
    	}
    }
    
    public void runUnackedSendLoop() {
    	while(true) {
    		for(Map<Integer, OutgoingPacket> unackedForHost : unAckedPackets) {
	    		for(OutgoingPacket packet : unackedForHost.values()) {
	    			if(!allowCommunication.get()) {
	    				return;
	    			}
	//    	    	System.out.println("nb of unacked is " + unAckedPackets.values().size());
	    			long currentTimeMillis = System.currentTimeMillis();
	    			if(currentTimeMillis - packet.getTimeWhenSent() > getPacketMinimumResendWait(packet)) {
	    				packet.setTimeWhenSent(currentTimeMillis);	//will be overwritten in Send but it is necessary to update it immediatly to avoid sending it a bunch of times
	    				packet.incrementNbOfTimesSent();
	    				basicLinkNode.send(packet);
	//    				System.out.println("resend message " + packet.getMessage().getSequenceNumber());
	    			}
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
    	int srcPid = ProcessIDHelpers.getIdFromPort(port);
	   
    	if(messages.size() == 1 && messages.get(0).isAck()) {
//    		System.out.println("Received ack for message " + messages.gintet(0).getSequenceNumber()  + " from " + port);
    		unAckedPackets.get(srcPid-1).remove(packet.getPacketSeqNr());
    	} else {
    		sendAck(addr, port, seqNr);
    		if(!delivered.get(srcPid-1).contains(seqNr)) {
    			delivered.get(srcPid-1).add(seqNr);
			  	shouldDeliver = true;
    		}
    	}
    	return shouldDeliver;
	}
   
    
   private void sendAck(InetAddress addr, int port, int seqNr) {
//	   System.out.println("Send ack for message " + seqNr);
	   NetMessage ackMessage = new NetMessage(true, seqNr, new byte[0], addr, port);
	   OutgoingPacket ackPacket = new OutgoingPacket(ackMessage);
	   basicLinkNode.send(ackPacket);
   }
   
   
   private OutgoingPacket makeNextPacketToSend(Queue<NetMessage> waiting, int maxNbOfMessages) {
	   List<NetMessage> messagesToSend = new LinkedList<>();
	   for(int i = 0; !waiting.isEmpty() && i < maxNbOfMessages; ++i) {
		   messagesToSend.add(waiting.remove());
	   }
	   return new OutgoingPacket(messagesToSend);
   }
   
   
   private List<WaitingMsgQueue> initWaitingForSend(int maxSkipCount, int maxBufferSize) {
		int nbOfHosts = NetworkParams.getInstance().getNbOfHosts();
		List<WaitingMsgQueue> tmp = new ArrayList<WaitingMsgQueue>();
		for(int i = 0; i < nbOfHosts; ++i) {
			WaitingMsgQueue queue = new WaitingMsgQueue(maxSkipCount, maxBufferSize);
			tmp.add(queue);
		}
		return tmp;	
   }
   
   private List<CompactedValueRecord> deliveredInit(){
		List<CompactedValueRecord> lines = new ArrayList<>();
		int nbOfHosts = NetworkParams.getInstance().getNbOfHosts();
		for(int i = 0; i < nbOfHosts; ++i) {
			lines.add(new CompactedValueRecord());
		}
		return lines;
	}
   
   private List<Map<Integer, OutgoingPacket>> unackedPacketsInit(){
	   List<Map<Integer, OutgoingPacket>> tmp = new ArrayList<>();
	   int nbOfHosts = NetworkParams.getInstance().getNbOfHosts();
		for(int i = 0; i < nbOfHosts; ++i) {
			tmp.add(new ConcurrentHashMap<>());
		}
		return tmp;
   }
   
   /**
    * @return the number of waiting packets in the smallest unacked queue
    */
   private int smallestUnackedLineCount() {
	   int smallest = Integer.MAX_VALUE;
	   for(Map map : unAckedPackets) {
		   int nbUnacked = map.values().size();
		   smallest = nbUnacked < smallest? nbUnacked : smallest;
	   }
	   return smallest;
   }
   
   private float getPacketMinimumResendWait(OutgoingPacket packet) {
	   int backoffMultiplier = Math.min(10, packet.getNbOfTimesSent()^2);
	   return NetworkParams.FIRST_RESEND_TIMER_MILLIS * backoffMultiplier;
   }

   

}
