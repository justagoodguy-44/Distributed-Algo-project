package cs451;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import cs451.Custom.CommunicationLogger;
import cs451.Custom.ConfigReader;
import cs451.Custom.PerfectLinkNode;
import cs451.Custom.ProcessIDHelpers;

public class Main {
	

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
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

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
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

        System.out.println("Doing some initialization\n");
        PerfectLinkNode thisNode = new PerfectLinkNode(myIp, myPort, parser.myId());

        System.out.println("Broadcasting and delivering messages...\n");
        
        ConfigReader configReader = new ConfigReader(parser.config());
        
        Thread deliverThread = new Thread() {
        	@Override
            public void run() {
                thisNode.RunDeliverLoop();
            }
        };
        deliverThread.run();
        
        //Enqueue messages to be sent
        int messagesToSend = configReader.getNbMessages();
        int dstPid = configReader.getDestPid();
        InetAddress dstAddr = InetAddress.getByName(parser.hosts().get(dstPid-1).getIp());
        int dstPort = ProcessIDHelpers.getPortFromId(dstPid);
        for(int i = 0; i < messagesToSend; ++i) {
        	thisNode.enqueueForSend(dstAddr, dstPort, Integer.toString(i+1));
        }
        
        Thread sendThread = new Thread() {
        	@Override
            public void run() {
                thisNode.RunSendLoop();
            }
        };
        sendThread.run();
        
        Thread sendUnackedThread = new Thread() {
        	@Override
            public void run() {
                thisNode.RunUnackedSendLoop();
            }
        };
        sendUnackedThread.run();

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
