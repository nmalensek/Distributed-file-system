package cs555.dfs.processing.controllerprocessing;

import cs555.dfs.messages.*;
import cs555.dfs.node.NodeRecord;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.DetermineTopThree;
import cs555.dfs.util.Splitter;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessInquiries {

    private TCPSender sender = new TCPSender();
    private ConcurrentHashMap<String, Socket> clientAddressMap = new ConcurrentHashMap<>(); //<clientID, clientSocket>

    public void processWriteFileInquiry(ConcurrentHashMap<String, NodeRecord> nodeMap, WriteFileInquiry writeInquiry) throws IOException {
        String clientAddress = writeInquiry.getClientAddress();
        checkIfKnownClient(clientAddress);

        NodeInformation destinationNodes = new NodeInformation();
        destinationNodes.setNodeInfo(DetermineTopThree.determineTopThreeChunkServers(nodeMap));
        destinationNodes.setInformationType(Protocol.CHUNK_DESTINATION);

        sender.send(clientAddressMap.get(clientAddress), destinationNodes.getBytes());
        System.out.println("Sent destination nodes: " + destinationNodes.getNodeInfo());
    }

    public void processReadFileInquiry(ReadFileInquiry readFileInquiry,
                                       ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> chunkMap) throws IOException {
        String clientAddress = readFileInquiry.getClientAddress();
        checkIfKnownClient(clientAddress);

        String filename = readFileInquiry.getFilename();

        NodeInformation chunkLocations = new NodeInformation();
        chunkLocations.setNodeInfo(findChunkLocations(chunkMap.get(filename)));
        chunkLocations.setInformationType(Protocol.CHUNK_LOCATION);

        sender.send(clientAddressMap.get(clientAddress), chunkLocations.getBytes());
    }

    private String findChunkLocations(ConcurrentHashMap<String, List<String>> chunkLocations) {
        if (chunkLocations == null) {
            return "";
        }

        StringBuilder locations = new StringBuilder();
        locations.append(chunkLocations.keySet().size()); //add #chunks so client knows when it has all chunks
        locations.append("#!#");

        for (String chunkName : chunkLocations.keySet()) {
            locations.append(chunkName).append(":-:");
            for (int i = 0; i < chunkLocations.get(chunkName).size(); i++) {
                locations.append(chunkLocations.get(chunkName).get(i))
                        .append("|");
            }
            locations.append(",,");
        }
        System.out.println(locations.toString());
        return locations.toString();
    }

    private void checkIfKnownClient(String clientAddress) throws IOException {
        if (clientAddressMap.get(clientAddress) == null) {
            Socket clientSocket = new Socket(Splitter.getHost(clientAddress), Splitter.getPort(clientAddress));
            clientAddressMap.put(clientAddress, clientSocket);
        }
    }
}
