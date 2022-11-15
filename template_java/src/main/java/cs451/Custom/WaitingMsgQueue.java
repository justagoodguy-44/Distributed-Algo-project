package cs451.Custom;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import cs451.Custom.Message.NetMessage;
import cs451.Custom.Network.NetworkParams;

public class WaitingMsgQueue {
	
	private LinkedBlockingQueue<NetMessage> waitingMessages;
	private int maxSkipCount;
	private int currentSkipCount;
	
	public WaitingMsgQueue(int maxSkipCount) {
		waitingMessages = new LinkedBlockingQueue<>(NetworkParams.WAITING_FOR_SEND_MAX_SIZE);
		this.maxSkipCount = maxSkipCount;
	}
	
	public LinkedBlockingQueue<NetMessage> getWaitingMessages(){
		return this.waitingMessages;
	}
	
	public boolean canSkip() {
		return currentSkipCount < maxSkipCount;
	}
	
	public void skip() {
		currentSkipCount++;
	}
	
	public void resetSkipCounter() {
		currentSkipCount = 0;
	}

}
