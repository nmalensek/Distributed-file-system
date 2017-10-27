package cs555.dfs.messages.controllerprocessing;

import cs555.dfs.messages.MajorHeartbeatMessage;
import cs555.dfs.messages.MinorHeartbeatMessage;
import cs555.dfs.messages.NodeInformation;
import cs555.dfs.messages.RequestMajorHeartbeat;
import cs555.dfs.node.NodeRecord;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.ChunkMetadata;
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
                processChunkName(name, nodes, nodeChunksMap, heartbeat.getNodeInfo());
            }

            updateNodeRecord(nodes.get(heartbeat.getNodeInfo()), heartbeat.getFreeSpace(), splitNames.length);

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

        String allChunkData = majorHeartbeat.getAllChunkData();
        String[] splitChunkData = allChunkData.split(",");
        for (String data : splitChunkData) {
            processChunkName(data, nodes, nodeChunksMap, majorHeartbeat.getNodeInfo());
        }
    }

    private synchronized void processChunkName(String chunkData,
                                               ConcurrentHashMap<String, NodeRecord> nodes,
                                               ConcurrentHashMap<String, List<String>> nodeChunks, String nodeID) {
        try {
            String[] splitChunkData = chunkData.split(":");
            String chunkName = splitChunkData[0];
            int versionNumber = Integer.parseInt(splitChunkData[1]);
            String fileName = splitChunkData[2];
            long lastUpdatedTime = Long.parseLong(splitChunkData[3]);

            nodes.get(nodeID).getChunkInfo().put(
                    chunkName, new ChunkMetadata(chunkName, versionNumber, fileName, lastUpdatedTime));

            if (nodeChunks.get(chunkName) == null) {
                ArrayList<String> nodeList = new ArrayList<>();
                nodeList.add(nodeID);

                nodeChunks.put(chunkName, nodeList);
            } else {
                if (!nodeChunks.get(chunkName).contains(nodeID)) {
                    nodeChunks.get(chunkName).add(nodeID);
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            //no chunkdata, chunk server hasn't stored any new chunks
        }

    }

    private synchronized NodeRecord registerNodeData(String nodeID, long freeSpace) throws IOException {
        Socket nodeSocket = new Socket(split.getHost(nodeID), split.getPort(nodeID));

        return new NodeRecord(nodeID, nodeSocket, freeSpace);
    }

    private synchronized void updateNodeRecord(NodeRecord nodeToUpdate, long updatedSpace, int numNewChunks) {
        nodeToUpdate.setUsableSpace(updatedSpace);
        nodeToUpdate.setNumChunks(nodeToUpdate.getNumChunks() + numNewChunks);
    }

    private void sendTestResponse(Socket socket) throws IOException {
        NodeInformation nodeInformation = new NodeInformation();
        nodeInformation.setNodeInfo("test");
        sender.send(socket, nodeInformation.getBytes());
    }
}
