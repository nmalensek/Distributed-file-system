package cs555.dfs.processing.controllerprocessing;

import cs555.dfs.messages.ReadFileInquiry;
import cs555.dfs.node.NodeRecord;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.ChunkMetadata;
import cs555.dfs.util.DetermineTopThree;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static cs555.dfs.util.Splitter.getHost;
import static cs555.dfs.util.Splitter.getPort;

public class InitiateRecovery {

    private TCPSender recoverySender = new TCPSender();
    private ArrayList<String> nodesWithChunks;

    public void removeChunkServer(ConcurrentHashMap<String, NodeRecord> nodeMap,
                                  ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> nodeChunksMap,
                                  String nodeID) {
        NodeRecord failedNode = nodeMap.remove(nodeID);
        for (String chunkName : failedNode.getChunkInfo().keySet()) {
            ChunkMetadata metadata = failedNode.getChunkInfo().get(chunkName);
            removeFromChunksMap(nodeChunksMap, metadata.getFileName(), metadata.getChunkName(), nodeID);
            replicateFile(DetermineTopThree.determineTopThreeChunkServers(nodeMap), metadata.getChunkName());
        }

    }

    private void removeFromChunksMap(ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> nodeChunksMap,
                                     String fileName, String chunkName, String nodeID) {
        ConcurrentHashMap<String, List<String>> chunkMap = nodeChunksMap.get(fileName);
        List<String> locationList = chunkMap.get(chunkName);
        nodesWithChunks = new ArrayList<>();
        for (Iterator<String> iterator = locationList.iterator(); iterator.hasNext();) {
            String nodeAddress = iterator.next();
            if (nodeAddress.equals(nodeID)) {
                iterator.remove();
            } else {
                nodesWithChunks.add(nodeAddress);
            }
        }
    }

    private void replicateFile(String destinations, String chunkName) {
        String[] destinationAddresses = destinations.split(",");
        String replicationAddress = "";
        for (String address : destinationAddresses) {
            if (!nodesWithChunks.contains(address)) {
                replicationAddress = address;
                break;
            }
        }
        if (replicationAddress.isEmpty()) {return;} //no eligible destinations
        try {
            String holderAddress = nodesWithChunks.get(0);
            ReadFileInquiry recoverChunk = new ReadFileInquiry();
            recoverChunk.setClientAddress(replicationAddress);
            recoverChunk.setFilename(chunkName);
            Socket chunkHolderSocket = new Socket(getHost(holderAddress), getPort(holderAddress));
            recoverySender.send(chunkHolderSocket, recoverChunk.getBytes());
            System.out.println("Requesting " + holderAddress + " to send chunk replica to " + replicationAddress);
        } catch (IOException e) {
            System.out.println("Could not contact selected node, retrying with new node...");
            nodesWithChunks.remove(0);
            replicateFile(destinations, chunkName);
        }

    }
}
