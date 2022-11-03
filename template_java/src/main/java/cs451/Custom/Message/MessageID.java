package cs451.Custom.Message;

import java.net.InetAddress;
import java.net.SocketAddress;

public class MessageID {
	
	private int seqNr;
	private InetAddress addr;
	private int port;
	
	public MessageID(int seqNr, InetAddress addr, int port) {
		this.seqNr = seqNr;
		this.addr = addr;
		this.port = port;
	}
	
	public InetAddress getAddress() {
		return addr;
	}
	
	public int getPort() {
		return port;
	}
	
	@Override
	public int hashCode() {
		String packetString = Integer.toString(seqNr) + addr.toString() + Integer.toString(port); 
		return packetString.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		MessageID idOther = (MessageID)o;
		return idOther.seqNr == this.seqNr && idOther.port == this.port && idOther.addr.equals(this.addr);
	}
}
