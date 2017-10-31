package cs555.dfs.processing.controllerprocessing;

import cs555.dfs.messages.NodeInformation;
import cs555.dfs.messages.Protocol;
import cs555.dfs.messages.RequestChunk;
import cs555.dfs.node.NodeRecord;
import cs555.dfs.transport.TCPSender;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessChunkLocations {

    private ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> chunkMap;
    private ConcurrentHashMap<String, NodeRecord> nodeMap;

    public void setUpInformation(ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> chunkMap,
                                 ConcurrentHashMap<String, NodeRecord> nodeMap) {
        this.chunkMap = chunkMap;
        this.nodeMap = nodeMap;
    }

    public void sendChunkLocations(RequestChunk message, TCPSender controllerSender) throws IOException {
        String chunkName = message.getChunkName();
        String fileName = chunkName.substring(0, chunkName.lastIndexOf('_'));
        String returnAddress = message.getChunkServerAddress();
        System.out.println("Getting chunk " + chunkName + "of file " + fileName);

        List<String> locations = chunkMap.get(fileName).get(chunkName);
        StringBuilder locationString = new StringBuilder();
        for (String node : locations) {
            locationString.append(node).append(",");
        }

        NodeInformation chunkServerAddresses = new NodeInformation();
        chunkServerAddresses.setInformationType(Protocol.CHUNK_LOCATION);
        chunkServerAddresses.setNodeInfo(locationString.toString());
        controllerSender.send(nodeMap.get(returnAddress).getNodeSocket(), chunkServerAddresses.getBytes());
    }
}