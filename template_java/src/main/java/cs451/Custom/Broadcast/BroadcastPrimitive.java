package cs451.Custom.Broadcast;


import cs451.Custom.Deliverable;


public interface BroadcastPrimitive {
	
	public void broadcast(byte[] data);
	
	public Deliverable deliver();
	
	

}
