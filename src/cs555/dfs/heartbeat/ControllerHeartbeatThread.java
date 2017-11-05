package cs555.dfs.heartbeat;

import cs555.dfs.messages.Ping;
import cs555.dfs.node.ControllerNode;
import cs555.dfs.node.NodeRecord;
import cs555.dfs.processing.controllerprocessing.InitiateRecovery;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static cs555.dfs.util.Constants.minorHeartbeatInterval;

public class ControllerHeartbeatThread extends Thread {

    private Socket targetSocket;
    private TCPSender heartbeatSender = new TCPSender();
    private String nodeID;
    private ConcurrentHashMap<String, NodeRecord> nodeMap;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> nodeChunksMap;
    private boolean running = true;

    public ControllerHeartbeatThread(Socket targetSocket, String nodeID, ConcurrentHashMap<String, NodeRecord> nodeMap,
                                     ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> nodeChunksMap) {
        this.targetSocket = targetSocket;
        this.nodeID = nodeID;
        this.nodeMap = nodeMap;
        this.nodeChunksMap = nodeChunksMap;
    }

    public synchronized void sendServerHeartbeat() {
        Ping ping = new Ping();
        try {
            heartbeatSender.send(targetSocket, ping.getBytes());
//            System.out.println("Heartbeat to " + nodeID + " successful");
        } catch (IOException e) {
            handleIOException();
            running = false;
        }
    }

    private synchronized void handleIOException() {
            System.out.println("Could not contact chunk server at " + targetSocket.getRemoteSocketAddress()
                    + ", starting recovery.");
            startReReplication();
    }

    private synchronized void startReReplication() {
        InitiateRecovery initiateRecovery = new InitiateRecovery();
        initiateRecovery.removeChunkServer(nodeMap, nodeChunksMap, nodeID);
    }

    @Override
    public void run() {
        while (running) {
            try {
                    Thread.sleep(minorHeartbeatInterval);
                    sendServerHeartbeat();
            } catch (InterruptedException e) {
                System.out.println("Interrupted, attempting to send heartbeat.");
                try {
                    targetSocket = new Socket(Splitter.getHost(nodeID), Splitter.getPort(nodeID));
                } catch (IOException ioe) {
                    handleIOException();
                    running = false;
                }
            }
        }
    }
}