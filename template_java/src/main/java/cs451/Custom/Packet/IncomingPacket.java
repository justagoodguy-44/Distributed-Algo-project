package cs451.Custom.Packet;

import java.util.List;

import cs451.Custom.Network.NetMessage;

/**
 * Class representing a group of messages coming from the same host
 */
public class IncomingPacket extends Packet {
	
	public IncomingPacket(List<NetMessage> messages) {
		super(messages);
	}
}
