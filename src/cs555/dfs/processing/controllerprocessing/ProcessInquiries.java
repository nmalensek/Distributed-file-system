package cs555.dfs.processing.controllerprocessing;

import cs555.dfs.messages.NodeInformation;
import cs555.dfs.messages.WriteFileInquiry;
import cs555.dfs.node.NodeRecord;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessInquiries {

    private TCPSender sender = new TCPSender();
    private Splitter splitter = new Splitter();
    private ConcurrentHashMap<String, Socket> clientAddressMap = new ConcurrentHashMap<>(); //<clientID, clientSocket>

    public void processWriteFileInquiry(ConcurrentHashMap<String, NodeRecord> nodeMap, WriteFileInquiry writeInquiry) throws IOException {
        String clientAddress = writeInquiry.getClientAddress();
        if (clientAddressMap.get(clientAddress) == null) {
            Socket clientSocket = new Socket(splitter.getHost(clientAddress), splitter.getPort(clientAddress));
            clientAddressMap.put(clientAddress, clientSocket);
        }

        NodeInformation destinationNodes = new NodeInformation();
        destinationNodes.setNodeInfo(determineTopThreeChunkServers(nodeMap));

        sender.send(clientAddressMap.get(clientAddress), destinationNodes.getBytes());
        System.out.println("Sent destination nodes: " + destinationNodes.getNodeInfo());
    }

    public void processReadFileInquiry() {

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
}
