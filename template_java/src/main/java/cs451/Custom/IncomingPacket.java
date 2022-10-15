package cs451.Custom;

import java.net.InetAddress;

public class IncomingPacket extends MessagePacket {

	private InetAddress srcAddr;
	private int srcPort;
	
	public IncomingPacket(Message message, InetAddress srcAddr, int srcPort) {
		super(message);
		this.srcAddr = srcAddr;
		this.srcPort = srcPort;
	}
	
	public InetAddress getSrcAddress() {
		return srcAddr;
	}
	
	public int getSrcPort() {
		return srcPort;
	}

}
