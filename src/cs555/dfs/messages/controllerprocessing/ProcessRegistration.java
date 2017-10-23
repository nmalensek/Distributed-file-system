package cs555.dfs.messages.controllerprocessing;

import cs555.dfs.messages.NodeInformation;
import cs555.dfs.node.NodeRecord;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessRegistration {

    private Splitter split = new Splitter();
    private TCPSender testSender = new TCPSender();

    public ProcessRegistration() {

    }

    public void logNewEntry(ConcurrentHashMap<String, NodeRecord> nodeMap, NodeInformation information) throws IOException {
        String newNodeHost = split.getHost(information.getNodeInfo());
        int newNodePort = split.getPort(information.getNodeInfo());

        NodeRecord newNode = new NodeRecord(
                information.getNodeInfo(),
                new Socket(newNodeHost, newNodePort),
                information.getUsableSpace());

        nodeMap.put(information.getNodeInfo(), newNode);

        sendTestResponse(newNode.getNodeSocket());
    }

    private void sendTestResponse(Socket socket) throws IOException {
        NodeInformation nodeInformation = new NodeInformation();
        nodeInformation.setNodeInfo("test");
        testSender.send(socket, nodeInformation.getBytes());
    }
}
