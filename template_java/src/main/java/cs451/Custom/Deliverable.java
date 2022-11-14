package cs451.Custom;

import java.util.List;

public class Deliverable {
	
	private List<byte[]> data;
	private int senderPid;
	
	public Deliverable(List<byte[]> data, int senderPid) {
		this.data = data;
		this.senderPid = senderPid;
	}
	
	public List<byte[]> getData(){
		return data;
	}
	
	public int getSenderPid() {
		return senderPid;
	}

}
