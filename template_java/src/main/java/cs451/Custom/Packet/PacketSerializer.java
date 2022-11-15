package cs451.Custom.Packet;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import cs451.Custom.Message.NetMessage;
import cs451.Custom.Message.NetMessageSerializer;

public class PacketSerializer {
	
	private static final int NB_OF_MESSAGES_POS = 0;
	private static final int MESSAGE_LENGTHS_POS = NB_OF_MESSAGES_POS + Integer.BYTES;

	public static byte[] serializePacket(Packet packet) {
		List<NetMessage> messages = packet.getMessages();
		int nbOfMessages = messages.size();
		List<byte[]> msgBytes = new LinkedList<byte[]>();
		int totalMsgBytes = 0;
		for(NetMessage msgInPacket : messages) {
			byte[] serializedMsg = NetMessageSerializer.serializeForNetwork(msgInPacket);
			msgBytes.add(serializedMsg);
			totalMsgBytes += serializedMsg.length;
		}
		int headerLength = MESSAGE_LENGTHS_POS + (nbOfMessages * Integer.BYTES);
		ByteBuffer serializedMessages = ByteBuffer.allocate(headerLength + totalMsgBytes);
		serializedMessages.putInt(nbOfMessages);
		for(int i = 0; i < nbOfMessages; ++i) {
//			System.out.println("The created msg length is " + msgBytes.get(i).length);
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
	
	public static IncomingPacket deserializeFromNetwork(byte[] serializedPacket, InetAddress srcAddr, int srcPort) {
		ByteBuffer packetBuffer = ByteBuffer.wrap(serializedPacket);
		int nbOfMessages = packetBuffer.getInt();
		int messagesOffset = MESSAGE_LENGTHS_POS + (nbOfMessages * Integer.BYTES);
		List<NetMessage> messages = new LinkedList<NetMessage>();
		int thisMessageOffset = messagesOffset;
		for(int i = 0; i < nbOfMessages; ++i) {
			int thisMessageLength = packetBuffer.getInt();
			byte[] msgData = Arrays.copyOfRange(serializedPacket, thisMessageOffset, thisMessageOffset + thisMessageLength);
			NetMessage msg = NetMessageSerializer.deserializeFromNetwork(msgData, srcAddr, srcPort);
			messages.add(msg);
			thisMessageOffset += thisMessageLength;
		}
		return new IncomingPacket(messages);
	}
}
