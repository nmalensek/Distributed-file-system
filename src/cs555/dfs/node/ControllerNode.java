package cs555.dfs.node;

import cs555.dfs.messages.Event;
import cs555.dfs.messages.MajorHeartbeatMessage;
import cs555.dfs.messages.MinorHeartbeatMessage;
import cs555.dfs.messages.NodeInformation;
import cs555.dfs.messages.controllerprocessing.ProcessRegistration;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.transport.TCPServerThread;

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
    private ConcurrentHashMap<String, List<String>> chunkStorageMap = new ConcurrentHashMap<>(); //<chunkname, list<nodeID>>
    private ConcurrentHashMap<String, NodeRecord> nodesInOverlay = new ConcurrentHashMap<>(); //<nodeID, node data>


    private void startup() {
        TCPServerThread serverThread = new TCPServerThread(this, controllerPort);
        serverThread.start();
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof NodeInformation) {
            System.out.println("got a node information");
            ProcessRegistration processRegistration = new ProcessRegistration();
            processRegistration.logNewEntry(nodesInOverlay, (NodeInformation) event);
        } else if (event instanceof MinorHeartbeatMessage) {
            System.out.println("got a minor heartbeat at " + System.currentTimeMillis());
        } else if (event instanceof MajorHeartbeatMessage) {
            System.out.println("got a major heartbeat at " + System.currentTimeMillis());
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
