package cs451.Custom.Lattice;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class LatticeSerializer {
	
	private static final int MSG_TYPE_SERIALIZED_LEN = Byte.BYTES;
	private static final int INSTANCE_ID_SERIALIZED_LEN = Integer.BYTES;
	private static final int PROPOSAL_NB_SERIALIZED_LEN = Integer.BYTES;
	
	private static final int RESPONSE_IS_ACK_SERIALIZED_LEN = Byte.BYTES;
	
	private static final int MSG_TYPE_SERIALIZED_POS = 0;
	private static final int INSTANCE_ID_SERIALIZED_POS = MSG_TYPE_SERIALIZED_POS + MSG_TYPE_SERIALIZED_LEN;
	private static final int PROPOSAL_NB_SERIALIZED_POS = INSTANCE_ID_SERIALIZED_POS + INSTANCE_ID_SERIALIZED_LEN;
	private static final int PROPOSAL_SET_SERIALIZED_POS = PROPOSAL_NB_SERIALIZED_POS + PROPOSAL_NB_SERIALIZED_LEN;
	private static final int RESPONSE_IS_ACK_SERIALIZED_POS = PROPOSAL_NB_SERIALIZED_POS + PROPOSAL_NB_SERIALIZED_LEN;
	private static final int RESPONSE_SET_SERIALIZED_POS = RESPONSE_IS_ACK_SERIALIZED_POS + RESPONSE_IS_ACK_SERIALIZED_LEN;

	
	
	public static byte[] serializeProposalForNet(int instanceId, int proposalNb, Set<Integer> proposedVals) {
		int bytesNeeded = PROPOSAL_SET_SERIALIZED_POS + proposedVals.size()*Integer.BYTES;
		ByteBuffer serializedMsg = ByteBuffer.allocate(bytesNeeded);
		byte msgType = 0;	//0 for proposal, would be 1 for proposal
		serializedMsg.put(msgType); 
		serializedMsg.putInt(instanceId);
		serializedMsg.putInt(proposalNb);
		for(int val : proposedVals) {
			serializedMsg.putInt(val);
		}
		return serializedMsg.array();
	}
	
	public static byte[] serializeResponseForNet(LatticeResponse response) {
		if(response.isAck()) {
			return serializeAckForNet(response.getInstanceId(), response.getProposalNb());
		} else {
			return serializeNackForNet(response.getInstanceId(), response.getProposalNb(), response.getMissingVals());
		}
	}
	
	private static byte[] serializeAckForNet(int instanceId, int proposalNb) {
		int bytesNeeded = RESPONSE_SET_SERIALIZED_POS;
		ByteBuffer serializedMsg = ByteBuffer.allocate(bytesNeeded);
		byte msgType = 1;	//1 for response, would be 0 for proposal
		serializedMsg.put(msgType); 
		serializedMsg.putInt(instanceId);
		serializedMsg.putInt(proposalNb);
		byte isAck = 1;	//1 since is an ack, would be 0 for nack
		serializedMsg.put(isAck);
		return serializedMsg.array();
	}
	
	
	private static byte[] serializeNackForNet(int instanceId, int proposalNb, Set<Integer> missingVals) {
		int bytesNeeded = RESPONSE_SET_SERIALIZED_POS + missingVals.size()*Integer.BYTES;
		ByteBuffer serializedMsg = ByteBuffer.allocate(bytesNeeded);
		byte msgType = 1;	//1 for response, would be 0 for proposal
		serializedMsg.put(msgType);
		serializedMsg.putInt(instanceId);
		serializedMsg.putInt(proposalNb);
		byte isAck = 1;	//0 since is an nack, would be 1 for nack
		serializedMsg.put(isAck);
		for(int val : missingVals) {
			serializedMsg.putInt(val);
		}
		return serializedMsg.array();
	}
	
	
	public static LatticeMsg deserializeFromNet(byte[] serializedMsg) {
		ByteBuffer msgBuffer = ByteBuffer.wrap(serializedMsg);
		LatticeMsgType msgType = msgBuffer.get() == 0? LatticeMsgType.PROPOSAL : LatticeMsgType.RESPONSE;
		int instanceId = msgBuffer.getInt();
		int proposalNb = msgBuffer.getInt();
		
		if(msgType == LatticeMsgType.PROPOSAL) {
			Set<Integer> proposedVals = new HashSet<Integer>();
			for(int i = PROPOSAL_SET_SERIALIZED_POS; i < serializedMsg.length; ++i) {
				proposedVals.add(msgBuffer.getInt());
			}
			return new LatticeProposal(instanceId, proposalNb, proposedVals);
		}
		
		else {
			byte isAckByte = msgBuffer.get();
			boolean isAck = isAckByte == 1? true : false;
			Set<Integer> missingVals = null;
			if(!isAck) {
				missingVals = new HashSet<Integer>();
				for(int i = RESPONSE_SET_SERIALIZED_POS; i < serializedMsg.length; ++i) {
					missingVals.add(msgBuffer.getInt());
				}
			}
			return new LatticeResponse(instanceId, proposalNb, isAck, missingVals);
		}
}
	
	
	public static LatticeMsgType getMsgType(byte[] serializedMsg) {
		ByteBuffer msgBuffer = ByteBuffer.wrap(serializedMsg);
		LatticeMsgType msgType = msgBuffer.get() == 0? LatticeMsgType.PROPOSAL : LatticeMsgType.RESPONSE;
		return msgType;
	}
}
