package cs555.dfs.processing.controllerprocessing;

import cs555.dfs.heartbeat.ControllerHeartbeatThread;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessHeartbeats {

    private TCPSender sender = new TCPSender();
    private HashMap<String, ControllerHeartbeatThread> heartbeatThreads = new HashMap<>();

    public synchronized void logNewEntry(ConcurrentHashMap<String, NodeRecord> nodeMap,
                                         ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> nodeChunksMap,
                                         NodeInformation information) throws IOException {
        String newNodeHost = Splitter.getHost(information.getNodeInfo());
        int newNodePort = Splitter.getPort(information.getNodeInfo());

        Socket chunkServerSocket = new Socket(newNodeHost, newNodePort);

        NodeRecord newNode = new NodeRecord(
                information.getNodeInfo(),
                chunkServerSocket,
                information.getUsableSpace());

        nodeMap.put(information.getNodeInfo(), newNode);

        startHeartbeats(chunkServerSocket, information.getNodeInfo(), nodeMap, nodeChunksMap);
    }

    public synchronized void processMinorHeartbeat(ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> nodeChunksMap,
                                                   ConcurrentHashMap<String, NodeRecord> nodes,
                                                   MinorHeartbeatMessage heartbeat) throws IOException {

        if (nodes.get(heartbeat.getNodeInfo()) != null) {
            String newChunksMetadata = heartbeat.getNewChunkData();
            updateNodeRecord(nodes.get(heartbeat.getNodeInfo()), heartbeat.getFreeSpace(),
                    heartbeat.getNumChunks());
                processChunkData(newChunksMetadata, nodes, nodeChunksMap, heartbeat.getNodeInfo());
        } else { //Controller is recovering, request major heartbeat from and restart heartbeats to chunk servers

            NodeRecord nodeInOverlay = registerNodeData(heartbeat.getNodeInfo(), heartbeat.getFreeSpace());
            nodes.put(heartbeat.getNodeInfo(), nodeInOverlay);

            startHeartbeats(nodeInOverlay.getNodeSocket(), nodeInOverlay.toString(), nodes, nodeChunksMap);

            System.out.println("No record of node, requesting major heartbeat from " + heartbeat.getNodeInfo());
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
//        System.out.println(messageData);
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

                if (chunkMap.get(chunkName) == null) {
                    ArrayList<String> chunkLocationList = new ArrayList<>();
                    chunkLocationList.add(nodeID);
                    chunkMap.put(chunkName, chunkLocationList);
                } else {
                    if (!chunkMap.get(chunkName).contains(nodeID)) {
                        chunkMap.get(chunkName).add(nodeID);
                    }
                }
            }
        }
    }

    private synchronized NodeRecord registerNodeData(String nodeID, long freeSpace) throws IOException {
        Socket nodeSocket = new Socket(Splitter.getHost(nodeID), Splitter.getPort(nodeID));

        return new NodeRecord(nodeID, nodeSocket, freeSpace);
    }

    private synchronized void updateNodeRecord(NodeRecord nodeToUpdate, long updatedSpace, int numChunks) {
        nodeToUpdate.setUsableSpace(updatedSpace);
        nodeToUpdate.setNumChunks(numChunks);
    }

    public HashMap<String, ControllerHeartbeatThread> getHeartbeatThreads() {
        return heartbeatThreads;
    }

    private synchronized void startHeartbeats(Socket serverSocket, String hostPort,
                                              ConcurrentHashMap<String, NodeRecord> nodeMap,
                                              ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> nodeChunksMap) {
        ControllerHeartbeatThread heartbeatThread =
                new ControllerHeartbeatThread(serverSocket, hostPort, nodeMap, nodeChunksMap);
        heartbeatThread.start();
        heartbeatThreads.put(hostPort, heartbeatThread);
    }

    //    private void sendTestResponse(Socket socket) throws IOException {
//        NodeInformation nodeInformation = new NodeInformation();
//        nodeInformation.setNodeInfo("test");
//        sender.send(socket, nodeInformation.getBytes());
//    }
}
