package cs451.Custom.Network;

public class NetworkParams {

	private static NetworkParams networkParams = null;

	public static final int MAX_NB_OF_MSG_PER_PACKET = 8;

	public static final int WAITING_FOR_SEND_MAX_SIZE = 128;

	public static final float FIRST_RESEND_TIMER_MILLIS = 150;

	public static final int MAX_UNACKED_PACKETS = 256;

	public static final float SEND_NEW_PACKET_ANYWAYS_PROBA = 0.001f;
	
	public static int MAX_SKIP_COUNT = 100;
	

	private int nbOfHosts;

	private int maxUnackedPacketsPerProcess;
	
	private int nbOfCorrectHosts;
	
	private int maxRcvBufferSize;



	public static void setInstance(int nbOfHosts, int maxRcvBufferSize) {
		networkParams = new NetworkParams(nbOfHosts, maxRcvBufferSize);
	}

	public static NetworkParams getInstance() {
		return networkParams;
	}

	private NetworkParams(int nbOfHosts, int maxRcvBufferSize) {
		this.nbOfHosts = nbOfHosts;
		this.maxUnackedPacketsPerProcess = MAX_UNACKED_PACKETS / nbOfHosts;
		this.nbOfCorrectHosts = nbOfHosts/2 + 1;
		this.maxRcvBufferSize = maxRcvBufferSize;
	}


	public int getMaxSmallestUnackedPacketsPerProcess() {
		return maxUnackedPacketsPerProcess;
	}

	public int getNbOfHosts() {
		return nbOfHosts;
	}
	
	public int getNbOfCorrectHosts() {
		return nbOfCorrectHosts;
	}
	
	public int getMaxDifferentProposalVals() {
		return maxRcvBufferSize;
	}
}
