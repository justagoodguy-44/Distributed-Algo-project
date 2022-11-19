package cs451;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import cs451.Custom.CommunicationLogger;
import cs451.Custom.Broadcast.FIFO;
import cs451.Custom.Broadcast.URB;
import cs451.Custom.Helpers.ConfigReader;
import cs451.Custom.Helpers.ProcessIDHelpers;
import cs451.Custom.Links.PerfectLinkNode;
import cs451.Custom.Network.NetMessage;
import cs451.Custom.Network.NetworkParams;
import cs451.Custom.Packet.IncomingPacket;

public class Main {
	

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
        PerfectLinkNode.simulateProcessCrash();
        CommunicationLogger logger = CommunicationLogger.getInstance();
        if(logger != null) {
        	CommunicationLogger.writeLogsToFile();
        }
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException, SocketException, UnknownHostException {
    	Parser parser = new Parser(args);
        parser.parse();
        NetworkParams.setInstance(parser.hosts().size());
        CommunicationLogger.setInstance(parser.output());
        initSignalHandlers();

        // example
        int pid = parser.myId();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        
        System.out.println("List of resolved hosts is:");
        
        int myPort = -1;
        InetAddress myIp = InetAddress.getLocalHost();
        
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            if(host.getId() == parser.myId()) {
            	myPort = host.getPort();
            	myIp = InetAddress.getByName(host.getIp());
            }
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Broadcasting and delivering messages...\n");
        
        ConfigReader configReader = new ConfigReader(parser.config());
        System.out.println("Config to send:");
        System.out.println("===============");
        System.out.println(configReader.getNbMessages() + " messages to " + configReader.getDestPid() + "\n");

        PerfectLinkNode linkNode = new PerfectLinkNode(myIp, myPort, parser.myId());
        FIFO fifo = new FIFO(parser.hosts(), pid, linkNode);
        
        
        Thread deliverThread = new Thread() {
         	@Override
             public void run() {
         		while(true) {
            		fifo.deliver();
            	}
             }
         };
         deliverThread.start();
        
        //Enqueue messages to be sent
        long messagesToSend = configReader.getNbMessages();
        int dstPid = configReader.getDestPid();
        InetAddress dstAddr = InetAddress.getByName(parser.hosts().get(dstPid-1).getIp());
        int dstPort = ProcessIDHelpers.getPortFromId(dstPid);
       
        
        
        Thread addNewMessagesThread = new Thread() {
        	@Override
            public void run() {
	        	for(int i = 0; i < messagesToSend; ++i) {
		        	ByteBuffer data = ByteBuffer.allocate(Integer.BYTES); 
	        		data.putInt(i+1); 
	        		fifo.broadcast(data.array(), true);
            	}
        	}
        };
        addNewMessagesThread.start();
    

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
