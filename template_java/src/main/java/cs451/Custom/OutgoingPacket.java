package cs451.Custom;

import java.net.InetAddress;

public class OutgoingPacket extends MessagePacket {

	private long timeWhenSent = -1;
	private InetAddress dstAddr;
	private int dstPort;
	
	public OutgoingPacket(Message message, InetAddress dstAddr, int dstPort) {
		super(message);
		this.dstAddr = dstAddr;
		this.dstPort = dstPort;
	}
	
	public InetAddress getDstAddress() {
		return dstAddr;
	}
	
	public int getDstPort() {
		return dstPort;
	}
	
	public long getTimeWhenSent() {
		return timeWhenSent;
	}
	
	public void setTimeWhenSent(long timeWhenSent) {
		this.timeWhenSent = timeWhenSent;
	}
	

}