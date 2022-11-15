package cs451.Custom.Broadcast;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class URBMessageSerializer {
	
	private static final int SRC_PID_SERIALIZED_LEN = Integer.BYTES;
	private static final int SEQ_NB_SERIALIZED_LEN = Integer.BYTES;
	
	private static final int SRC_PID_SERIALIZED_POS = 0;
	private static final int SEQ_NB_SERIALIZED_POS = SRC_PID_SERIALIZED_POS + SRC_PID_SERIALIZED_LEN;
	private static final int DATA_SERIALIZED_POS = SEQ_NB_SERIALIZED_POS + SEQ_NB_SERIALIZED_LEN;
	
	public static byte[] serializeForNet(URBMessage msg) {
		int bytesNeeded = DATA_SERIALIZED_POS + msg.getData().length;

		ByteBuffer serializedMessage = ByteBuffer.allocate(bytesNeeded);
		serializedMessage.putInt(SRC_PID_SERIALIZED_POS, msg.getSrcPid());
		serializedMessage.putInt(SEQ_NB_SERIALIZED_POS, msg.getSeqNb());
		byte[] messageArray = serializedMessage.array();
		byte[] data = msg.getData();
    	for(int i = 0; i < data.length; ++i) {
    		messageArray[i + DATA_SERIALIZED_POS] = data[i];
    	}
    	return messageArray;
	}
	
	public static URBMessage deserializeFromNet(byte[] serializedMsg) {
		ByteBuffer msgBuffer = ByteBuffer.wrap(serializedMsg);
    	int srcPid = msgBuffer.getInt();
    	int seqNb = msgBuffer.getInt();
    	byte[] data = Arrays.copyOfRange(serializedMsg, DATA_SERIALIZED_POS, serializedMsg.length);
		return new URBMessage(srcPid, seqNb, data);
	}
	

	

}
