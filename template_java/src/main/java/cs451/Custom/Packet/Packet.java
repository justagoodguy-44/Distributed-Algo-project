package cs451.Custom.Packet;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import cs451.Custom.Message.NetMessage;


public abstract class Packet {
	
	private List<NetMessage> messages;
	private int packetSeqNr;
	
	public Packet(List<NetMessage> messages) {
		this.messages = messages;
		this.packetSeqNr = getPackageSeqNbFromMessages(messages);
	}
	
	
	public Packet(NetMessage msg) {
		this.messages = new LinkedList<NetMessage>();
    	messages.add(msg);
		this.packetSeqNr = getPackageSeqNbFromMessages(messages);
	}
	
	
	public List<NetMessage> getMessages() {
		return messages;
	}
	
	
	public int getPacketSeqNr() {
		return packetSeqNr;
	}
	
	/**
	 * If outgoing message packet, this returns the destination address of the messages, otherwise it returns the source address
	 */
	public InetAddress getAddr() {
		return messages.get(0).getAddr();
	}
	
	/**
	 * If outgoing message packet, this returns the destination port of the messages, otherwise it returns the source port
	 */
	public int getPort() {
		return messages.get(0).getPort();
	}
	
	
	/**
	 * A sequence number for the package will be given by taking the sequence number of the first message it contains
	 * @return the sequence number of the package
	 */
	private int getPackageSeqNbFromMessages(List<NetMessage> wrappedMessages) {
		return wrappedMessages.get(0).getSequenceNumber();
	}
}
