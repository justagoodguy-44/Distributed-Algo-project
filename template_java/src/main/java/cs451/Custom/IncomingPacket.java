package cs451.Custom;

import java.net.InetAddress;

public class IncomingPacket extends MessagePacket {

	public IncomingPacket(Message message, InetAddress addr, int port) {
		super(message, addr, port);
	}
	
	public InetAddress getSrcAddress() {
		return addr;
	}
	
	public int getSrcPort() {
		return port;
	}

}
