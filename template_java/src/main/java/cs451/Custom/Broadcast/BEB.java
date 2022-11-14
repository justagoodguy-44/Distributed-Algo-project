package cs451.Custom.Broadcast;

import java.util.List;

import cs451.Host;
import cs451.Custom.Deliverable;
import cs451.Custom.Links.PerfectLinkNode;

public class BEB implements BroadcastPrimitive{
	
	private List<Host> hosts;
	private PerfectLinkNode linkNode;
	
	public BEB(List<Host> hosts, PerfectLinkNode linkNode) {
		this.hosts = hosts;
		this.linkNode = linkNode;
	}

	@Override
	public void broadcast(byte[] data) {
		for(Host host: hosts) { 
			linkNode.send(host.getIpInetAddr(), host.getPort(), data);
		}
	}

	@Override
	public Deliverable deliver() {
		return linkNode.deliver();
	}

	
}
