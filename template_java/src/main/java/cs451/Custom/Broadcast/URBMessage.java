package cs451.Custom.Broadcast;

import java.nio.ByteBuffer;
import java.util.Arrays;

import cs451.Custom.Message.NetMessage;
import cs451.Custom.Message.NetMessageID;

/**
 * Represents a uniform reliable broadcast message
 */
public class URBMessage {
	
	private int srcPid;
	private int seqNb;
	private byte[] data;
	private URBMessageID id;
	
	private static final int SRC_PID_SERIALIZED_LEN = Integer.BYTES;
	private static final int SEQ_NB_SERIALIZED_LEN = Integer.BYTES;
	
	private static final int SRC_PID_SERIALIZED_POS = 0;
	private static final int SEQ_NB_SERIALIZED_POS = SRC_PID_SERIALIZED_POS + SRC_PID_SERIALIZED_LEN;
	private static final int DATA_SERIALIZED_POS = SEQ_NB_SERIALIZED_POS + SEQ_NB_SERIALIZED_LEN;
	
	public URBMessage(int srcPid, int seqNb, byte[] data) {
		this.srcPid = srcPid;
		this.seqNb = seqNb;
		this.data = data;
		this.id = new URBMessageID(srcPid, seqNb);
	}
	
	public URBMessage(byte[] serializedMsg) {
		ByteBuffer msgBuffer = ByteBuffer.wrap(serializedMsg);
    	this.srcPid = msgBuffer.getInt();
    	this.seqNb = msgBuffer.getInt();
    	this.data = Arrays.copyOfRange(serializedMsg, DATA_SERIALIZED_POS, serializedMsg.length);
		this.id = new URBMessageID(srcPid, seqNb);
	}
	
	public byte[] serialize() {
		int bytesNeeded = DATA_SERIALIZED_POS + 1 + data.length;
		ByteBuffer serializedMessage = ByteBuffer.allocate(bytesNeeded);
		serializedMessage.putInt(SRC_PID_SERIALIZED_POS, srcPid);
		serializedMessage.putInt(SEQ_NB_SERIALIZED_POS, seqNb);
		byte[] messageArray = serializedMessage.array();
    	for(int i = 0; i < data.length; ++i) {
    		messageArray[i + DATA_SERIALIZED_POS] = data[i];
    	}
    	return messageArray;
	}
	
	@Override
	public boolean equals(Object o) {
		URBMessage other = (URBMessage)o;
		return other.id.equals(id);
	}
	
	/**
	 * Returns a unique identifier for this message
	 */
	public URBMessageID getMessageId() {
		return id;
	}
}
