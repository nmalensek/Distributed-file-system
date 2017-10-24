package cs555.dfs.messages.controllerprocessing;

import cs555.dfs.messages.MajorHeartbeatMessage;
import cs555.dfs.messages.MinorHeartbeatMessage;
import cs555.dfs.messages.NodeInformation;
import cs555.dfs.messages.RequestMajorHeartbeat;
import cs555.dfs.node.NodeRecord;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessHeartbeats {

    private Splitter split = new Splitter();
    private TCPSender sender = new TCPSender();

    public ProcessHeartbeats() {

    }

    public synchronized void logNewEntry(ConcurrentHashMap<String, NodeRecord> nodeMap, NodeInformation information) throws IOException {
        String newNodeHost = split.getHost(information.getNodeInfo());
        int newNodePort = split.getPort(information.getNodeInfo());

        NodeRecord newNode = new NodeRecord(
                information.getNodeInfo(),
                new Socket(newNodeHost, newNodePort),
                information.getUsableSpace());

        nodeMap.put(information.getNodeInfo(), newNode);

        sendTestResponse(newNode.getNodeSocket());
    }

    public synchronized void processMinorHeartbeat(ConcurrentHashMap<String, List<String>> nodeChunksMap,
                                                   ConcurrentHashMap<String, NodeRecord> nodes,
                                                   MinorHeartbeatMessage heartbeat) throws IOException {

        if (nodes.get(heartbeat.getNodeInfo()) != null) {
            String newChunkNames = heartbeat.getNewChunkData();
            String[] splitNames = newChunkNames.split(":");
            for (String name : splitNames) {
                processChunkName(name, nodeChunksMap, heartbeat.getNodeInfo());
            }
        } else { //Controller is recovering, request major heartbeat

            NodeRecord nodeInOverlay = registerNodeData(heartbeat.getNodeInfo(), heartbeat.getFreeSpace());
            nodes.put(heartbeat.getNodeInfo(), nodeInOverlay);

            RequestMajorHeartbeat heartbeatRequest = new RequestMajorHeartbeat();
            sender.send(nodeInOverlay.getNodeSocket(), heartbeatRequest.getBytes());
        }
    }

    public synchronized void processMajorHeartbeat(ConcurrentHashMap<String, List<String>> nodeChunksMap,
                                                   ConcurrentHashMap<String, NodeRecord> nodes,
                                                   MajorHeartbeatMessage majorHeartbeat) throws IOException {

        if (nodes.get(majorHeartbeat.getNodeInfo()) == null) {
            NodeRecord nodeRecord = registerNodeData(majorHeartbeat.getNodeInfo(), majorHeartbeat.getFreeSpace());
            nodes.put(majorHeartbeat.getNodeInfo(), nodeRecord);
        }

        String allChunkNames = majorHeartbeat.getAllChunkData();
        String[] splitChunkNames = allChunkNames.split(":");
        for (String chunkName : splitChunkNames) {
            processChunkName(chunkName, nodeChunksMap, majorHeartbeat.getNodeInfo());
        }
    }

    private synchronized void processChunkName(String chunkName, ConcurrentHashMap<String, List<String>> nodeChunks, String nodeID) {
        if (nodeChunks.get(chunkName) == null) {
            ArrayList<String> nodeList = new ArrayList<>();
            nodeList.add(nodeID);

            nodeChunks.put(chunkName, nodeList);
        } else {
            if (!nodeChunks.get(chunkName).contains(nodeID)) {
                nodeChunks.get(chunkName).add(nodeID);
            }
        }
    }

    private synchronized NodeRecord registerNodeData(String nodeID, long freeSpace) throws IOException {
        Socket nodeSocket = new Socket(split.getHost(nodeID), split.getPort(nodeID));

        return new NodeRecord(nodeID, nodeSocket, freeSpace);
    }

    private void sendTestResponse(Socket socket) throws IOException {
        NodeInformation nodeInformation = new NodeInformation();
        nodeInformation.setNodeInfo("test");
        sender.send(socket, nodeInformation.getBytes());
    }
}
