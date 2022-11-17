package cs451.Custom.Broadcast;

import java.nio.ByteBuffer;
import java.util.Arrays;

import cs451.Custom.Message;
import cs451.Custom.Network.NetMessage;
import cs451.Custom.Network.NetMessageID;

/**
 * Represents a uniform reliable broadcast message
 */
public class URBMessage extends Message{
	
	private int srcPid;
	private int seqNb;
	private byte[] data;
	private long id;
	
	
	
	public URBMessage(int srcPid, int seqNb, byte[] data) {
		this.srcPid = srcPid;
		this.seqNb = seqNb;
		this.data = data;
		this.id = (((long)srcPid) << 32) | (seqNb & 0xffffffffL);
	}

	@Override
	public boolean equals(Object o) {
		URBMessage other = (URBMessage)o;
		return other.getId() == id;
	}
	
	public int getSrcPid() {
		return srcPid;
	}
	
	public int getSeqNb() {
		return seqNb;
	}
	
	public byte[] getData() {
		return data;
	}
	
	/**
	 * Returns a unique identifier for this message
	 */
	public long getId() {
		return id;

	}
}
