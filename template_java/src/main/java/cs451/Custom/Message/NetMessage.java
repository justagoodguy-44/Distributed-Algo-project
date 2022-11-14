package cs451.Custom.Message;

import java.io.Serializable;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class NetMessage {
    
	private boolean isAck;
    private int seqNb;
    //contents of the message
    private byte[] data;
    private InetAddress addr;
    private int port;
    

    /**
     * 
     * @param seqNb: the sequence number of the message
     * @param data: the data carried by this message
     * @param addr: the source IP address of the message if it is arriving from the network, the destination IP if 
     * it is being sent to the network
     * @param port: the source port of the message if it is arriving from the network, the destination port if 
     * it is being sent to the network
     */
    public NetMessage(boolean isAck, int seqNb, byte[] data, InetAddress addr, int port){
    	this.isAck = isAck;
        this.seqNb = seqNb;
        this.data = data;
        this.addr = addr;
        this.port = port;
    }
    
    public int getSequenceNumber() {
    	return seqNb;
    }
    
    public byte[] getData() {
    	return data;
    }
    
    /**
     * @return: the source address of the sender if this message was received from the network,
     * otherwise the destination address if this message is to be sent on the network
     */
    public InetAddress getAddr() {
		return addr;
	}
	
    
    /**
     * @return: the source port of the sender if this message was received from the network,
     * otherwise the destination port if this message is to be sent on the network
     */
	public int getPort() {
		return port;
	}
	
	public boolean isAck() {
		return isAck;
	}
        
}
