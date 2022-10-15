package cs451.Custom;

import java.net.InetAddress;

public abstract class MessagePacket {
	
	private Message message;
	
	public MessagePacket(Message message) {
		this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
}
