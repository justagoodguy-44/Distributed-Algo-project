package cs451.Custom.Links;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import cs451.Custom.Network.IncomingPacket;
import cs451.Custom.Network.OutgoingPacket;

public class BasicLinkNode {
	
	private DatagramSocket datagramSocket;
	
	private int MAX_PACKET_SIZE = 128; //1 + Integer.BYTES + 8*Integer.BYTES + 8*Integer.BYTES;
	
	protected int processId;
	
	
		
	public BasicLinkNode(InetAddress addr,int port, int processId) throws SocketException {
		this.datagramSocket = new DatagramSocket(port, addr);
		this.processId = processId;
	}

	
	public void send(OutgoingPacket packet) {
//		System.out.println("Send message " + packet.getMessage().getSequenceNumber());
		packet.setTimeWhenSent(System.currentTimeMillis());
		byte[] serializedMsgArray = packet.serialize();
		DatagramPacket datagramPacket = new DatagramPacket(serializedMsgArray, 
				serializedMsgArray.length, packet.getDstAddress(), packet.getDstPort());
		try {
			datagramSocket.send(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	public IncomingPacket deliver() {
		IncomingPacket packet = Receive();
		return packet;
	}
	
	
	protected IncomingPacket Receive() {
		byte[] nextMessageBuffer = new byte[MAX_PACKET_SIZE];
		DatagramPacket nextPacket = new DatagramPacket(nextMessageBuffer, MAX_PACKET_SIZE);
		try {
			datagramSocket.receive(nextPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new IncomingPacket(nextPacket.getData(), nextPacket.getAddress(), nextPacket.getPort());
	}
	
}
