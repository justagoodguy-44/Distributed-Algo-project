package cs451.Custom.Links;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import cs451.Custom.Network.NetworkParams;
import cs451.Custom.Packet.IncomingPacket;
import cs451.Custom.Packet.OutgoingPacket;
import cs451.Custom.Packet.PacketSerializer;

public class BasicLinkNode {
	
	private DatagramSocket datagramSocket;
	
	
	protected int processId;
	
	
		
	public BasicLinkNode(InetAddress addr,int port, int processId) throws SocketException {
		this.datagramSocket = new DatagramSocket(port, addr);
		this.processId = processId;
	}

	
	public void send(OutgoingPacket packet) {
//		System.out.println("senidng " + packet.getPacketSeqNr());
		packet.setTimeWhenSent(System.currentTimeMillis());
		byte[] serializePacket = PacketSerializer.serializePacket(packet);
		DatagramPacket datagramPacket = new DatagramPacket(serializePacket, 
				serializePacket.length, packet.getAddr(), packet.getPort());
		try {
			datagramSocket.send(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	protected IncomingPacket receive() {
		byte[] nextMessageBuffer = new byte[NetworkParams.MAX_PACKET_SIZE];
		DatagramPacket nextPacket = new DatagramPacket(nextMessageBuffer, NetworkParams.MAX_PACKET_SIZE);
		try {
			datagramSocket.receive(nextPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		IncomingPacket packet = PacketSerializer.deserializeFromNetwork(
				nextPacket.getData(), 
				nextPacket.getAddress(), 
				nextPacket.getPort());
		return packet;
	}
	
}
