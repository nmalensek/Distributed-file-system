package cs555.dfs.heartbeat;

import cs555.dfs.messages.MajorHeartbeatMessage;
import cs555.dfs.messages.MinorHeartbeatMessage;
import cs555.dfs.node.ChunkServer;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.ChunkMetadata;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkServerHeartbeatThread extends Thread {

    private int heartbeatInterval = 30000;
    private int majorHeartbeatInterval = 300000;
    private int heartbeatCount = 0;
    private Socket controllerSocket;
    private ChunkServer owner;
    private TCPSender controllerMessage = new TCPSender();

    public ChunkServerHeartbeatThread(Socket controllerSocket, ChunkServer owner) {
        this.owner = owner;
        this.controllerSocket = controllerSocket;
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
            ArrayList<String> newChunks = new ArrayList<>(owner.getNewChunksResponsibleFor());
            StringBuilder chunkBuilder = new StringBuilder();

            for (String chunkMetadata : newChunks) {
                chunkBuilder.append(chunkMetadata).append(",");
            }

            newChunks.clear();

            return chunkBuilder.toString();
        }
    }

    private String getAllChunks() {
        ConcurrentHashMap<String, ChunkMetadata> allChunks = new ConcurrentHashMap<>(owner.getChunksResponsibleFor());
        StringBuilder allChunksBuilder = new StringBuilder();

        for (String chunkName : allChunks.keySet()) {
            allChunksBuilder.append(allChunks.get(chunkName).toString()).append(",");
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
