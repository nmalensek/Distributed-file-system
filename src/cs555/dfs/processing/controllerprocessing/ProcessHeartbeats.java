package cs555.dfs.processing.controllerprocessing;

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

    public synchronized void processMinorHeartbeat(ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> nodeChunksMap,
                                                   ConcurrentHashMap<String, NodeRecord> nodes,
                                                   MinorHeartbeatMessage heartbeat) throws IOException {

        if (nodes.get(heartbeat.getNodeInfo()) != null) {
            String newChunksMetadata = heartbeat.getNewChunkData();
            updateNodeRecord(nodes.get(heartbeat.getNodeInfo()), heartbeat.getFreeSpace(),
                    heartbeat.getNumChunks());
                processChunkData(newChunksMetadata, nodes, nodeChunksMap, heartbeat.getNodeInfo());
        } else { //Controller is recovering, request major heartbeat

            NodeRecord nodeInOverlay = registerNodeData(heartbeat.getNodeInfo(), heartbeat.getFreeSpace());
            nodes.put(heartbeat.getNodeInfo(), nodeInOverlay);

            RequestMajorHeartbeat heartbeatRequest = new RequestMajorHeartbeat();
            sender.send(nodeInOverlay.getNodeSocket(), heartbeatRequest.getBytes());
        }
    }

    public synchronized void processMajorHeartbeat(ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> nodeChunksMap,
                                                   ConcurrentHashMap<String, NodeRecord> nodes,
                                                   MajorHeartbeatMessage majorHeartbeat) throws IOException {

        if (nodes.get(majorHeartbeat.getNodeInfo()) == null) {
            NodeRecord nodeRecord = registerNodeData(majorHeartbeat.getNodeInfo(), majorHeartbeat.getFreeSpace());
            nodes.put(majorHeartbeat.getNodeInfo(), nodeRecord);
        }
        updateNodeRecord(nodes.get(majorHeartbeat.getNodeInfo()), majorHeartbeat.getFreeSpace(),
                majorHeartbeat.getNumChunks());
        processChunkData(majorHeartbeat.getAllChunkData(), nodes, nodeChunksMap, majorHeartbeat.getNodeInfo());
    }

    private synchronized void processChunkData(String messageData,
                                               ConcurrentHashMap<String, NodeRecord> nodes,
                                               ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> nodeChunks,
                                               String nodeID) {
        if (messageData.isEmpty()) { return; }

        String[] splitDataIntoChunks = messageData.split(",");
        for (String data : splitDataIntoChunks) {

            String[] splitChunkData = data.split(":");
            String chunkName = splitChunkData[0];
            int versionNumber = Integer.parseInt(splitChunkData[1]);
            String fileName = splitChunkData[2];
            long lastUpdatedTime = Long.parseLong(splitChunkData[3]);

            nodes.get(nodeID).getChunkInfo().put(
                    chunkName, new ChunkMetadata(chunkName, versionNumber, fileName, lastUpdatedTime));

            if (nodeChunks.get(fileName) == null) {
                ConcurrentHashMap<String, List<String>> chunkNameMap = new ConcurrentHashMap<>();
                ArrayList<String> nodeList = new ArrayList<>();
                nodeList.add(nodeID);

                chunkNameMap.put(chunkName, nodeList);

                nodeChunks.put(fileName, chunkNameMap);
            } else {
                ConcurrentHashMap<String, List<String>> chunkMap = nodeChunks.get(fileName);

                if (!chunkMap.get(chunkName).contains(nodeID)) {
                    chunkMap.get(chunkName).add(nodeID);
                }
            }
        }
    }

    private synchronized NodeRecord registerNodeData(String nodeID, long freeSpace) throws IOException {
        Socket nodeSocket = new Socket(split.getHost(nodeID), split.getPort(nodeID));

        return new NodeRecord(nodeID, nodeSocket, freeSpace);
    }

    private synchronized void updateNodeRecord(NodeRecord nodeToUpdate, long updatedSpace, int numChunks) {
        nodeToUpdate.setUsableSpace(updatedSpace);
        nodeToUpdate.setNumChunks(numChunks);
    }

    private void sendTestResponse(Socket socket) throws IOException {
        NodeInformation nodeInformation = new NodeInformation();
        nodeInformation.setNodeInfo("test");
        sender.send(socket, nodeInformation.getBytes());
    }
}
