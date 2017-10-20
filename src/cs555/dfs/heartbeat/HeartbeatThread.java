package cs555.dfs.heartbeat;

import cs555.dfs.messages.MinorHeartbeatMessage;
import cs555.dfs.node.Node;
import cs555.dfs.transport.TCPSender;

public class HeartbeatThread {

    private String controllerHost;
    private int controllerPort;
    private int heartbeatInterval;
    private Node owner;
    private TCPSender controllerMessager = new TCPSender();

    public HeartbeatThread(String targetHost, int targetPort, int heartbeatInterval, Node owner) {
        this.controllerHost = targetHost;
        this.controllerPort = targetPort;
        this.heartbeatInterval = heartbeatInterval;
        this.owner = owner;
    }

    public void sendMinorHeartbeat() {
        MinorHeartbeatMessage heartbeatMessage = new MinorHeartbeatMessage();
//        heartbeatMessage.setNodeInfo();
    }
    public void sendMajorHeartbeat() {
        System.out.println("sending MAJOR heartbeat to " + controllerHost + ":" + controllerPort);
    }

    public void sendServerHeartbeat() {

    }
}

//have to track which nodes hold which chunks
//        hashmap with filename as key so if client requests it the controller can find where it is
//if one whole file: <Filename, NodeID>
//Controller node side:
//with n chunks: <ChunkName, List<NodeID>
//OR
//with n chunks: <Filename, HashMap<chunkName, List<NodeID>>
//
//Chunk server side:
//new chunks: List<String>
//old chunks: HashMap<ChunkName, ChunkName>