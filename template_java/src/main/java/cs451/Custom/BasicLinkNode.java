package cs451.Custom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class BasicLinkNode implements LinkNode{
	
	private DatagramSocket datagramSocket;
	
	private int PACKET_SIZE = 128;
	
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
		byte[] nextMessageBuffer = new byte[PACKET_SIZE];
		DatagramPacket nextPacket = new DatagramPacket(nextMessageBuffer, PACKET_SIZE);
		try {
			datagramSocket.receive(nextPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new IncomingPacket(nextPacket.getData(), nextPacket.getAddress(), nextPacket.getPort());
		}
}
