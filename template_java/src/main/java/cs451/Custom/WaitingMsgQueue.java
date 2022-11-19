package cs451.Custom;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import cs451.Custom.Network.NetMessage;
import cs451.Custom.Network.NetworkParams;

public class WaitingMsgQueue {
	
	private LinkedBlockingQueue<NetMessage> waitingMessages;
	private int maxSkipCount;
	private int currentSkipCount;
	
	public WaitingMsgQueue(int maxSkipCount, int maxBufferSize) {
		waitingMessages = new LinkedBlockingQueue<>(maxBufferSize);
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
