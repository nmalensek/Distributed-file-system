package cs555.dfs.heartbeat;

import cs555.dfs.messages.MajorHeartbeatMessage;
import cs555.dfs.messages.MinorHeartbeatMessage;
import cs555.dfs.node.ChunkServer;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.ChunkMetadata;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static cs555.dfs.util.Constants.heartbeatInterval;
import static cs555.dfs.util.Constants.majorHeartbeatInterval;

public class ChunkServerHeartbeatThread extends Thread {

    private int heartbeatCount = 0;
    private Socket controllerSocket;
    private ChunkServer owner;
    private TCPSender controllerMessage = new TCPSender();
    private String metadataFilepath;

    public ChunkServerHeartbeatThread(Socket controllerSocket, ChunkServer owner, String metadataFilepath) {
        this.owner = owner;
        this.controllerSocket = controllerSocket;
        this.metadataFilepath = metadataFilepath;
    }

    public void sendMinorHeartbeat() throws IOException {
        MinorHeartbeatMessage heartbeatMessage = new MinorHeartbeatMessage();
        heartbeatMessage.setNodeInfo(owner.getNodeID());
        heartbeatMessage.setNewChunkData(getNewChunks());
        heartbeatMessage.setFreeSpace(owner.getFreeSpace());
        heartbeatMessage.setNumChunks(owner.getChunksResponsibleFor().size());
        controllerMessage.send(controllerSocket, heartbeatMessage.getBytes());
    }

    public void sendMajorHeartbeat() throws IOException {
        MajorHeartbeatMessage majorHeartbeatMessage = new MajorHeartbeatMessage();
        majorHeartbeatMessage.setNodeInfo(owner.getNodeID());
        majorHeartbeatMessage.setAllChunkData(getAllChunks());
        majorHeartbeatMessage.setFreeSpace(owner.getFreeSpace());
        majorHeartbeatMessage.setNumChunks(owner.getChunksResponsibleFor().size());
        controllerMessage.send(controllerSocket, majorHeartbeatMessage.getBytes());
    }

    private String getNewChunks() {
        synchronized (owner.getNewChunksResponsibleFor()) {
            StringBuilder chunkBuilder = new StringBuilder();

            for (String chunkMetadata : owner.getNewChunksResponsibleFor()) {
                chunkBuilder.append(chunkMetadata).append(",");
            }
            System.out.println(chunkBuilder.toString());
            owner.getNewChunksResponsibleFor().clear();

            return chunkBuilder.toString();
        }
    }

    private String getAllChunks() throws IOException {
        StringBuilder allChunksBuilder = new StringBuilder();
        File metadataFile = new File(metadataFilepath);

        BufferedReader reader = new BufferedReader(new FileReader(metadataFile));

        String line;
        while ((line = reader.readLine()) != null) {
            allChunksBuilder.append(line).append(",");
        }

        return allChunksBuilder.toString();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(heartbeatInterval);
                heartbeatCount += heartbeatInterval;
                if (heartbeatCount == majorHeartbeatInterval) {
                    System.out.println("Sending major heartbeat...");
                    sendMajorHeartbeat();
                    heartbeatCount = 0;
                } else {
                    System.out.println("Sending minor heartbeat...");
                    sendMinorHeartbeat();
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
