package cs451.Custom.Message;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/*
 * Class that wraps an outgoing message with the info it needs to be sent on the network
 */
public class MessageNetWrapper {
	
	private Message msg;
	private boolean isAck;
	private int msgFragmentNb;
	private boolean isLastFragment;
	
	private static final int ACK_AND_LAST_FRAG_SERIALIZED_POS = 0;
	private static final int MSG_FRAG_NB_SERIALIZED_POS = ACK_AND_LAST_FRAG_SERIALIZED_POS + Byte.BYTES;
    private static final int SEQ_NB_SERIALIZED_POS = MSG_FRAG_NB_SERIALIZED_POS + Integer.BYTES;
    private static final int DATA_SERIALIZED_POS = SEQ_NB_SERIALIZED_POS + Integer.BYTES;

    /**
     * 
     * @param msg: the message to wrap
     * @param isAck: is this message an ack
     * @param msgFragmentNb: 0 if this message is not a fragment, otherwise its fragment number (the first is 1).
     * @param isLastFragment: false if this message is not a fragment, or if it is a fragment but not last, true otherwise
     */
	public MessageNetWrapper(Message msg, boolean isAck, int msgFragmentNb, boolean isLastFragment) {
		this.msg = msg;
		this.isAck = isAck;
		this.msgFragmentNb = msgFragmentNb;
		this.isLastFragment = isLastFragment;
	}
	
	public MessageNetWrapper(byte[] serializedWrapper, InetAddress srcAddress, int srcPort) {
    	ByteBuffer msgBuffer = ByteBuffer.wrap(serializedWrapper);
    	byte isAckAndLastFrag = msgBuffer.get();
    	this.isAck = (isAckAndLastFrag & 1) > 0? true:false;
    	this.isLastFragment = ((isAckAndLastFrag >> 1) & 1) > 0? true:false;
    	this.msgFragmentNb = msgBuffer.getInt();
    	int seqNb = msgBuffer.getInt();
    	byte[] data = Arrays.copyOfRange(serializedWrapper, DATA_SERIALIZED_POS, serializedWrapper.length);
    	Message deserializedMsg = new Message(seqNb, data, srcAddress, srcPort);
    	this.msg = deserializedMsg;
	}

	
	 public byte[] serializeForNetwork() {
	    	byte[] data = msg.getData();
	    	int bytesNeeded = DATA_SERIALIZED_POS + 1 + data.length;
	    	ByteBuffer serializedMessage = ByteBuffer.allocate(bytesNeeded);
	    	//lsb represents isAck and the next bit represents isLastFrag
	    	byte isAckAndFrag = (byte)((isLastFragment?1:0) << 1 + (isAck?1:0));
	    	serializedMessage.put(ACK_AND_LAST_FRAG_SERIALIZED_POS, isAckAndFrag);
	    	//add msg fragment number
	    	serializedMessage.putInt(MSG_FRAG_NB_SERIALIZED_POS, msgFragmentNb);
	    	//add seqNr
	    	serializedMessage.putInt(SEQ_NB_SERIALIZED_POS, msg.getSequenceNumber());
	    	//add data
	    	byte[] messageArray = serializedMessage.array();
	    	for(int i = 0; i < data.length; ++i) {
	    		messageArray[i + DATA_SERIALIZED_POS] = data[i];
	    	}
	    	return messageArray;
	 }
	 
		
	public Message getMessage() {
		return msg;
	}
	
	
	public boolean isAck() {
		return isAck;
	}
	
	
	public boolean isLastFragment() {
		return isLastFragment;
	}
	 
	 
}
