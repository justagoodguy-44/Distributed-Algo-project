package cs451.Custom.Broadcast;

import java.util.List;

import cs451.Host;
import cs451.Custom.Links.PerfectLinkNode;
import cs451.Custom.Network.NetMessage;

public class BEB {
	
	private List<Host> hosts;
	private PerfectLinkNode linkNode;
	
	public BEB(List<Host> hosts, PerfectLinkNode linkNode) {
		this.hosts = hosts;
		this.linkNode = linkNode;
	}

	public void broadcast(byte[] data, boolean isNewData) {
		for(Host host: hosts) { 
			linkNode.send(host.getIpInetAddr(), host.getPort(), data, isNewData);
		}
	}

	public List<NetMessage> deliver() {
		return linkNode.deliver();
	}

	
}
