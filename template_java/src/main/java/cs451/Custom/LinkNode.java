package cs451.Custom;

public interface LinkNode {
	
	public void send(OutgoingPacket packet);
	
	public IncomingPacket deliver();

}
