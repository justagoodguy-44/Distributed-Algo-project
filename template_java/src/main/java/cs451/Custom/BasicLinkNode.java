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

public class BasicLinkNode {
	
	private DatagramSocket datagramSocket;
	
	private int PACKET_SIZE = 128;
	
	protected int processId;
		
	
	public BasicLinkNode(InetAddress addr,int port, int processId) throws SocketException {
		this.datagramSocket = new DatagramSocket(port, addr);
		this.processId = processId;
	}

	protected void Send(OutgoingPacket packet) {
		
		System.out.println("Sending a message");
		
		packet.setTimeWhenSent(System.currentTimeMillis());
		ByteArrayOutputStream messageByteStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objStream = new ObjectOutputStream(messageByteStream);
			objStream.writeObject(packet.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] messageBytes = messageByteStream.toByteArray();
		DatagramPacket datagramPacket = new DatagramPacket(messageBytes, 
				messageBytes.length, packet.getDstAddress(), packet.getDstPort());
		try {
			datagramSocket.send(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	protected IncomingPacket Deliver(IncomingPacket packet) {
		return packet;
	}
	
	protected void handleReceivedPacket(IncomingPacket packet) {
		Deliver(packet);
	}
	
	protected IncomingPacket Receive() {
		byte[] nextMessageBuffer = new byte[PACKET_SIZE];
		DatagramPacket nextPacket = new DatagramPacket(nextMessageBuffer, PACKET_SIZE);
		try {
			datagramSocket.receive(nextPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteArrayInputStream messageByteStream = new ByteArrayInputStream(nextPacket.getData());
		ObjectInputStream objectInputStream = null;
		try {
			objectInputStream = new ObjectInputStream(messageByteStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(objectInputStream == null) {
			return null;
		}
		Message message = null;
		InetAddress srcAddr = null;
		int srcPort = 0;
		try {
			message = (Message)(objectInputStream.readObject());
			srcAddr = nextPacket.getAddress();
			srcPort = nextPacket.getPort();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		if(message == null) {
			return null;
		}
		return new IncomingPacket(message, srcAddr, srcPort);
	}
}
