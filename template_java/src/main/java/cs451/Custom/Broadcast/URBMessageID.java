package cs451.Custom.Broadcast;

import cs451.Custom.Message.NetMessageID;

public class URBMessageID {
	
	private int srcPid;
	private int seqNb;
	
	public URBMessageID(int srcPid, int seqNb) {
		this.srcPid = srcPid;
		this.seqNb = seqNb;
	}
	
	@Override
	public int hashCode() {
		String idString = Integer.toString(srcPid) + Integer.toString(seqNb); 
		return idString.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		URBMessageID idOther = (URBMessageID)o;
		return idOther.seqNb == this.seqNb && idOther.srcPid == this.srcPid;
	}

}
