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
	private CommunicationLogger logger;
	
	
	public BasicLinkNode(InetAddress addr,int port) throws SocketException {
		this.datagramSocket = new DatagramSocket(port, addr);
		this.logger = new CommunicationLogger();
	}

	protected void Send(OutgoingPacket packet) {
		packet.setTimeWhenSent(System.currentTimeMillis());
		ByteArrayOutputStream messageByteStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objStream = new ObjectOutputStream(messageByteStream);
			objStream.writeObject(packet.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		DatagramPacket datagramPacket = new DatagramPacket(messageByteStream.toByteArray(), 
				PACKET_SIZE, packet.getDstAddress(), packet.getDstPort());
		try {
			datagramSocket.send(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.logSend(packet.getMessage().getSequenceNumber());
	}
	
	protected void Deliver(IncomingPacket packet) {
		logger.logDeliver(packet.getSrcPort(), packet.getMessage().getSequenceNumber());

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