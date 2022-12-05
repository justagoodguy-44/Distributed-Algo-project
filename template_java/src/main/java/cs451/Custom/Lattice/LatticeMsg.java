package cs451.Custom.Lattice;

public class LatticeMsg {
	
	private int instanceId;
	private int proposalNb;
	
	public LatticeMsg(int instanceId, int proposalNb) {
		this.instanceId = instanceId;
		this.proposalNb = proposalNb;
		
	}
	
	public static long getIdFromPidAndSeqNb(int srcPid, int seqNb) {
		return (((long)srcPid) << 32) | (seqNb & 0xffffffffL);
	}
	
	public static int getSrcPidFromMsgId(long msgId) {
		return (int)(msgId >> 32);
	}
	
	public static int getSeqNbFromMsgId(long msgId) {
		return (int)msgId;
	}
		
	public int getProposalNb() {
		return proposalNb;
	}
	
	public int getInstanceId() {
		return instanceId;
	}
	
}
