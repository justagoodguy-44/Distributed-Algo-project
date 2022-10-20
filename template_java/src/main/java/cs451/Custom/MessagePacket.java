package cs451.Custom;

public abstract class MessagePacket {
	
	private Message message;
	
	public MessagePacket(Message message) {
		this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
}
