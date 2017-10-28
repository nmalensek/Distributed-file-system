package cs555.dfs.node;

import cs555.dfs.messages.*;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.transport.TCPServerThread;
import cs555.dfs.processing.controllerprocessing.ProcessHeartbeats;
import cs555.dfs.processing.controllerprocessing.ProcessInquiries;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerNode implements Node {

    private static int controllerPort;
    private static String controllerHost;
    private TCPSender controllerSender = new TCPSender();
    private ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> chunkStorageMap
            = new ConcurrentHashMap<>(); //<filename, <chunkname, list<nodeID>>>
    private ConcurrentHashMap<String, NodeRecord> nodesInOverlay = new ConcurrentHashMap<>(); //<nodeID, node data>
    private ProcessHeartbeats processHeartbeats = new ProcessHeartbeats();
    private ProcessInquiries processInquiries = new ProcessInquiries();


    private void startup() {
        TCPServerThread serverThread = new TCPServerThread(this, controllerPort);
        serverThread.start();
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof NodeInformation) {
            System.out.println("got a node information");
            processHeartbeats.logNewEntry(nodesInOverlay, (NodeInformation) event);
        } else if (event instanceof MinorHeartbeatMessage) {
                processHeartbeats.processMinorHeartbeat(chunkStorageMap, nodesInOverlay, (MinorHeartbeatMessage) event);
            System.out.println("got a minor heartbeat at " + System.currentTimeMillis());
        } else if (event instanceof MajorHeartbeatMessage) {
            processHeartbeats.processMajorHeartbeat(chunkStorageMap, nodesInOverlay, (MajorHeartbeatMessage) event);
            System.out.println("got a major heartbeat at " + System.currentTimeMillis());
        } else if (event instanceof WriteFileInquiry) {
            System.out.println("Processing write inquiry");
            processInquiries.processWriteFileInquiry(nodesInOverlay, (WriteFileInquiry) event);
        } else if (event instanceof ReadFileInquiry) {
            processInquiries.processReadFileInquiry((ReadFileInquiry) event, chunkStorageMap);
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        try {
            controllerHost = Inet4Address.getLocalHost().getHostName();
            controllerPort = Integer.parseInt(args[0]);

            ControllerNode controllerNode = new ControllerNode();
            controllerNode.startup();

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Usage: [Server port]");
        }

    }

    @Override
    public void processText(String text) throws IOException { }
}
