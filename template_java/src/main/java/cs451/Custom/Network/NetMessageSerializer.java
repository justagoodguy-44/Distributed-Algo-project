package cs451.Custom.Network;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class NetMessageSerializer {
	
	private static final int ACK_SERIALIZED_LEN = Byte.BYTES;
	private static final int SEQ_NB_SERIALIZED_LEN = Integer.BYTES;
	
	private static final int ACK_SERIALIZED_POS = 0;
    private static final int SEQ_NB_SERIALIZED_POS = ACK_SERIALIZED_POS + ACK_SERIALIZED_LEN;
    private static final int DATA_SERIALIZED_POS = SEQ_NB_SERIALIZED_POS + SEQ_NB_SERIALIZED_LEN;
	
	public static byte[] serializeForNetwork(NetMessage msg) {
		byte[] data = msg.getData();
    	int bytesNeeded = DATA_SERIALIZED_POS + data.length;
    	ByteBuffer serializedMessage = ByteBuffer.allocate(bytesNeeded);
    	//lsb represents isAck and the next bit represents isLastFrag
    	byte isAckByte = (byte)(msg.isAck()?1:0);
    	serializedMessage.put(ACK_SERIALIZED_POS, isAckByte);
    	//add seqNr
    	serializedMessage.putInt(SEQ_NB_SERIALIZED_POS, msg.getSequenceNumber());
    	//add data
    	byte[] messageArray = serializedMessage.array();
    	for(int i = 0; i < data.length; ++i) {
    		messageArray[i + DATA_SERIALIZED_POS] = data[i];
    	}
    	return messageArray;
	}
	
	public static NetMessage deserializeFromNetwork(byte[] serializedMsg, InetAddress srcAddress, int srcPort) {
		ByteBuffer msgBuffer = ByteBuffer.wrap(serializedMsg);
    	byte isAckByte = msgBuffer.get();
    	boolean isAck = (isAckByte & 1) > 0? true:false;
    	int seqNb = msgBuffer.getInt();
    	byte[] data = Arrays.copyOfRange(serializedMsg, DATA_SERIALIZED_POS, serializedMsg.length);
    	return new NetMessage(isAck, seqNb, data, srcAddress, srcPort);
	}

}
