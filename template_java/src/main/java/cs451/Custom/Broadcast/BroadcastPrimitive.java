package cs451.Custom.Broadcast;

import java.util.List;


public interface BroadcastPrimitive {
	
	public void broadcast(byte[] data);
	
	public List<byte[]> deliver();
	
	

}
