package cs451.Custom.Network;

public class NetworkParams {
	
	private static NetworkParams networkParams = null;

	public static final int MAX_NB_OF_MSG_PER_PACKET = 8;
	
	public static final int WAITING_FOR_SEND_MAX_SIZE = 64;
	
	public static final float FIRST_RESEND_TIMER_MILLIS = 3000;
				
	public static final int MAX_UNACKED_PACKETS = 300;
	
	public static final int MAX_PACKET_SIZE = 256;

	
	private static int nbOfHosts;
	
	private static int maxUnackedPacketsPerProcess;
	
	
	public static void setInstance(int nbOfHosts) {
		networkParams = new NetworkParams(nbOfHosts);
	}
	
	public static NetworkParams getInstance() {
		return networkParams;
	}
	
	private NetworkParams(int nbOfHosts) {
		this.nbOfHosts = nbOfHosts;
		this.maxUnackedPacketsPerProcess = MAX_UNACKED_PACKETS / nbOfHosts; 
	}
	
	
	public int getMaxSmallestUnackedPacketsPerProcess() {
		return maxUnackedPacketsPerProcess;
	}
	
	public int getNbOfHosts() {
		return nbOfHosts;
	}
}
