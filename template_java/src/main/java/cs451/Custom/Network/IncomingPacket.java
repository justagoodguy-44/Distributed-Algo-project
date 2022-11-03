package cs451.Custom.Network;

import java.net.InetAddress;
import java.util.List;

import cs451.Custom.Message.Message;

public class IncomingPacket extends MessagePacket {

	private InetAddress srcAddr;
	private int srcPort;
	
	public IncomingPacket(List<Message> messages, InetAddress srcAddr, int srcPort) {
		super(messages);
		this.srcAddr = srcAddr;
		this.srcPort = srcPort;
	}
	
	public IncomingPacket(byte[] serializedPacket, InetAddress srcAddr, int srcPort) {
		super(serializedPacket);
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
