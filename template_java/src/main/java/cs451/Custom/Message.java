package cs451.Custom;

import java.io.Serializable;

public class Message implements Serializable {
    
	private boolean isAck;
	
    /*If is an Ack this is the sequence number of the Acked message, otherwise
	it is the sequence number of this message */
    private int seqNr;
    
    //contents of the message
    private String data;

    public Message(boolean isAck, int seqNr, String contents){
    	this.isAck = isAck;
        this.seqNr = seqNr;
        this.data = data;
    }
    
    public boolean isAck() {
    	return isAck;
    }
    
    public int getSequenceNumber() {
    	return seqNr;
    }
    
    public String getData() {
    	return data;
    }
    
    
}
