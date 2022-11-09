package cs451.Custom.Broadcast;

import java.net.InetAddress;
import java.util.List;

import cs451.Host;
import cs451.Custom.Links.PerfectLinkNode;
import cs451.Custom.Message.NetMessage;

public class BestEffortBroadcast implements BroadcastPrimitive{
	
	private List<Host> hosts;
	private PerfectLinkNode linkNode;
	
	public BestEffortBroadcast(List<Host> hosts, PerfectLinkNode linkNode) {
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
	public List<byte[]> deliver() {
		return linkNode.deliver();
	}

	
}
