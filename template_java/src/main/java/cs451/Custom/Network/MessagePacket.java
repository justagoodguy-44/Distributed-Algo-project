package cs451.Custom.Network;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import cs451.Custom.Message.Message;

public abstract class MessagePacket {
	
	private List<Message> messages;
	private int packetSeqNr;
	
	private int nbOfMessagesPos = 0;
	private int messageLengthsPos = nbOfMessagesPos + Integer.BYTES;
	
	public MessagePacket(List<Message> messages) {
		this.messages = messages;
		this.packetSeqNr = messages.get(0).getSequenceNumber();
	}
	
	public MessagePacket(Message message) {
		this.messages = new LinkedList<Message>();
    	messages.add(message);
	}
	
	public MessagePacket(byte[] serializedPacket) {
		ByteBuffer packetBuffer = ByteBuffer.wrap(serializedPacket);
		int nbOfMessages = packetBuffer.getInt(nbOfMessagesPos);
		int messagesOffset = messageLengthsPos + (nbOfMessages * Integer.BYTES);
		List<Message> messages = new LinkedList<Message>();
		int thisMessageOffset = messagesOffset;
		for(int i = 0; i < nbOfMessages; ++i) {
			int thisMessageLength = packetBuffer.getInt(messageLengthsPos + (i * Integer.BYTES));
			messages.add(new Message(Arrays.copyOfRange(serializedPacket, thisMessageOffset, thisMessageOffset + thisMessageLength)));
			thisMessageOffset += thisMessageLength;
		}
		this.messages = messages;
		this.packetSeqNr = messages.get(0).getSequenceNumber();
	}
	
	public byte[] serialize() {
		int nbOfMessages = messages.size();
		List<byte[]> msgBytes = new LinkedList<byte[]>();
		int totalMsgBytes = 0;
		for(Message message : messages) {
			byte[] serializedMsg = message.serialize();
			msgBytes.add(serializedMsg);
			totalMsgBytes += serializedMsg.length;
		}
		
		int headerLength = messageLengthsPos + (nbOfMessages * Integer.BYTES);
		ByteBuffer serializedMessages = ByteBuffer.allocate(headerLength + totalMsgBytes);
		serializedMessages.putInt(nbOfMessages);
		for(int i = 0; i < nbOfMessages; ++i) {
			serializedMessages.putInt(msgBytes.get(i).length);
		}
		for(int i = 0; i < nbOfMessages; ++i) {
			byte[] bytes = msgBytes.get(i);
			for(int j = 0; j < bytes.length; ++j) {
	    		serializedMessages.put(bytes[j]);
	    	}
		}
		return serializedMessages.array();
	}
	
	public List<Message> getMessages() {
		return messages;
	}
	
	public int getPacketSeqNr() {
		return packetSeqNr;
	}
}
