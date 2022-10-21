package cs451.Custom;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Message implements Serializable {
    
	private boolean isAck;
	private int ackSerializedPos = 0;
	
    /*If is an Ack this is the sequence number of the Acked message, otherwise
	it is the sequence number of this message */
    private int seqNr;
    private int seqNrSerializedPos = 1;
    
    //contents of the message
    private byte[] data;
    private int dataSerializedPos = 5;

    public Message(boolean isAck, int seqNr, byte[] data){
    	this.isAck = isAck;
        this.seqNr = seqNr;
        this.data = data;
    }
    
    public Message(byte[] serializedMessage) {
    	isAck = serializedMessage[ackSerializedPos] == 1;    	
    	ByteBuffer msgBuffer = ByteBuffer.wrap(serializedMessage);
    	seqNr = msgBuffer.getInt(seqNrSerializedPos);
    	data = Arrays.copyOfRange(serializedMessage, dataSerializedPos, serializedMessage.length);
    }
    
    public boolean isAck() {
    	return isAck;
    }
    
    public int getSequenceNumber() {
    	return seqNr;
    }
    
    public byte[] getData() {
    	return data;
    }
    
    public byte[] serialize() {
    	
    	int bytesNeeded = 1 + Integer.BYTES + data.length;
    	ByteBuffer serializedMessage = ByteBuffer.allocate(bytesNeeded);
    	//add ack
    	serializedMessage.put(ackSerializedPos, (byte)(isAck?1:0));
    	//add seqNr
    	serializedMessage.putInt(seqNrSerializedPos, seqNr);
    	//add data
    	byte[] messageArray = serializedMessage.array();
    	for(int i = 0; i < data.length; ++i) {
    		messageArray[i + dataSerializedPos] = data[i];
    	}
    	return messageArray;
    }
    
    
    
}
