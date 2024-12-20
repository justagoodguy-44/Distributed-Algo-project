package cs451;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import cs451.Custom.CommunicationLogger;
import cs451.Custom.Broadcast.BEB;
import cs451.Custom.Broadcast.FIFO;
import cs451.Custom.Helpers.ConfigReader;
import cs451.Custom.Helpers.ConfigReaderLattice;
import cs451.Custom.Lattice.LatticeAgreeOrganizer;
import cs451.Custom.Lattice.LatticeReader;
import cs451.Custom.Links.PerfectLinkNode;
import cs451.Custom.Network.NetworkParams;

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
        ConfigReaderLattice configReader = new ConfigReaderLattice(parser.config());
        configReader.extractValuesLattice();
        int maxRcvBufferSize = configReader.getMaxTotalDifferentVals() * Integer.BYTES * 8;
        NetworkParams.setInstance(parser.hosts().size(), maxRcvBufferSize);
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


        PerfectLinkNode linkNode = new PerfectLinkNode(myIp, myPort, parser.myId());
        BEB beb = new BEB(parser.hosts(), linkNode);
        
        
        LatticeReader latticeReader = new LatticeReader(parser.config());
        int nbOfSimultaneousOneShots = Math.max(1, 1000/parser.hosts().size());
        LatticeAgreeOrganizer latticeAgree = new LatticeAgreeOrganizer(nbOfSimultaneousOneShots, latticeReader, beb, linkNode);
        
        
        Thread deliverThread = new Thread() {
         	@Override
             public void run() {
         		while(true) {
            		latticeAgree.deliver();
            	}
             }
         };
         deliverThread.start();
        
        //Enqueue messages to be sent
       
        
        
        
    

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
