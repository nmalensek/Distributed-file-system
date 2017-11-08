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

    /**
     * Finds and sends locations of requested chunks.
     * @param message message that contains details of the requested chunk.
     * @param controllerSender instance of TCPSender.
     * @throws IOException
     */
    public void sendChunkLocations(RequestChunk message, TCPSender controllerSender) throws IOException {
        String chunkName = message.getChunkName();
        String fileName = chunkName.substring(0, chunkName.lastIndexOf('_'));
        String returnAddress = message.getChunkServerAddress().split("::")[0];
        String originalClient = message.getChunkServerAddress().split("::")[1];
        System.out.println("Getting chunk " + chunkName);

        List<String> locations = chunkMap.get(fileName).get(chunkName);
        StringBuilder locationString = new StringBuilder();
        for (String node : locations) {
            locationString.append(node).append(",");
        }

        NodeInformation chunkServerAddresses = new NodeInformation();
        chunkServerAddresses.setInformationType(Protocol.CHUNK_LOCATION);
        chunkServerAddresses.setNodeInfo(originalClient + "::" + locationString.toString());
        controllerSender.send(nodeMap.get(returnAddress).getNodeSocket(), chunkServerAddresses.getBytes());
    }
}
