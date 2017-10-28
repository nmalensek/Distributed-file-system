package cs555.dfs.processing.controllerprocessing;

import cs555.dfs.messages.NodeInformation;
import cs555.dfs.messages.Protocol;
import cs555.dfs.messages.ReadFileInquiry;
import cs555.dfs.messages.WriteFileInquiry;
import cs555.dfs.node.NodeRecord;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessInquiries {

    private TCPSender sender = new TCPSender();
    private Splitter splitter = new Splitter();
    private ConcurrentHashMap<String, Socket> clientAddressMap = new ConcurrentHashMap<>(); //<clientID, clientSocket>

    public void processWriteFileInquiry(ConcurrentHashMap<String, NodeRecord> nodeMap, WriteFileInquiry writeInquiry) throws IOException {
        String clientAddress = writeInquiry.getClientAddress();
        checkIfKnownClient(clientAddress);

        NodeInformation destinationNodes = new NodeInformation();
        destinationNodes.setNodeInfo(determineTopThreeChunkServers(nodeMap));
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

    private String determineTopThreeChunkServers(ConcurrentHashMap<String, NodeRecord> nodes) {
        String first = "";
        String second = "";
        String third = "";

        long firstSpace = 0;
        long secondSpace = 0;
        long thirdSpace = 0;

        for (NodeRecord node : nodes.values()) {
            if (node.getUsableSpace() > firstSpace) {

                third = second;
                thirdSpace = secondSpace;

                second = first;
                secondSpace = firstSpace;

                first = node.toString();
                firstSpace = node.getUsableSpace();

            } else if (node.getUsableSpace() > secondSpace && node.getUsableSpace() < firstSpace) {

                third = second;
                thirdSpace = secondSpace;

                second = node.toString();
                secondSpace = node.getUsableSpace();

            } else if (node.getUsableSpace() > thirdSpace && node.getUsableSpace() < secondSpace) {

                third = node.toString();
                thirdSpace = node.getUsableSpace();
            }
        }
        return first + "," + second + "," + third;
    }

    private String findChunkLocations(ConcurrentHashMap<String, List<String>> chunkLocations) {
        if (chunkLocations == null) {
            return "";
        }

        StringBuilder locations = new StringBuilder();
        locations.append(chunkLocations.keySet().size()); //add #chunks so client knows when it has all chunks
        locations.append("#");
        
        for (String chunkName : chunkLocations.keySet()) {
            locations.append(chunkName)
                    .append("|")
                    .append(chunkLocations.get(chunkName).get(0))
                    .append(",");
        }

        return locations.toString();
    }

    private void checkIfKnownClient(String clientAddress) throws IOException {
        if (clientAddressMap.get(clientAddress) == null) {
            Socket clientSocket = new Socket(splitter.getHost(clientAddress), splitter.getPort(clientAddress));
            clientAddressMap.put(clientAddress, clientSocket);
        }
    }
}
