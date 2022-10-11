package cs451.Custom;

import java.net.InetAddress;

public abstract class MessagePacket {
	
	protected Message message;
	protected InetAddress addr;
	protected int port;
	
	public MessagePacket(Message message, InetAddress addr, int port) {
		this.message = message;
		this.addr = addr;
		this.port = port;
	}
	
	public Message getMessage() {
		return message;
	}
}
