package cs451.Custom.Packet;

import java.net.InetAddress;
import java.util.List;

import cs451.Custom.Network.NetMessage;

/**
 * Class representing a group of messages going to the same destination host
 */
public class OutgoingPacket extends Packet {

	private long timeWhenSent = -1;
	private int nbOfTimesSent = 1;
	
	public OutgoingPacket(List<NetMessage> messages) {
		super(messages);
	}
	
	public OutgoingPacket(NetMessage msg) {
		super(msg);
	}
	
	public long getTimeWhenSent() {
		return timeWhenSent;
	}
	
	public void setTimeWhenSent(long timeWhenSent) {
		this.timeWhenSent = timeWhenSent;
	}
	
	public int getNbOfTimesSent() {
		return nbOfTimesSent;
	}
	
	public void incrementNbOfTimesSent() {
		nbOfTimesSent++;
	}

	

}
