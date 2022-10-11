package cs451.Custom;

import java.net.InetAddress;

public class OutgoingPacket extends MessagePacket {

	public OutgoingPacket(Message message, InetAddress addr, int port) {
		super(message, addr, port);
		// TODO Auto-generated constructor stub
	}
	
	public InetAddress getDstAddress() {
		return addr;
	}
	
	public int getDstPort() {
		return port;
	}
	

}
