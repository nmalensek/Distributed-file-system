package cs555.dfs.heartbeat;

import cs555.dfs.messages.MajorHeartbeatMessage;
import cs555.dfs.messages.MinorHeartbeatMessage;
import cs555.dfs.node.ChunkServer;
import cs555.dfs.transport.TCPSender;

import java.io.*;
import java.net.Socket;

import static cs555.dfs.util.Constants.minorHeartbeatInterval;
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

    /**
     * Gets all new chunks that the parent chunk has stored since the last minor heartbeat.
     * @return
     */
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

    /**
     * Reads metadata file and returns comma-separated metadata for all files the chunk server holds
     * @return metadata in the form chunkName:version#:filename:lastUpdateTimestamp
     * @throws IOException
     */
    private String getAllChunks() throws IOException {
        StringBuilder allChunksBuilder = new StringBuilder();
        try {
            File metadataFile = new File(metadataFilepath);

            String line;
            try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile))) {
                while ((line = reader.readLine()) != null) {
                    allChunksBuilder.append(line).append(",");
                }
            }

            return allChunksBuilder.toString();
        } catch (FileNotFoundException fnfe) {
            return "";
        }
    }

    /**
     * Tries to reconnect to the controller node location specified on the parent chunk server's startup.
     */
    private void tryToReconnect() {
        try {
            controllerSocket = new Socket(owner.getControllerHost(), owner.getControllerPort());
        } catch (IOException e) {
            System.out.println("Reconnect failed, Controller Node's not back up. Retrying...");
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(minorHeartbeatInterval);
                if (controllerSocket.isClosed()) {
                    tryToReconnect();
                }
                heartbeatCount += minorHeartbeatInterval;
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
//                e.printStackTrace();
                System.out.println("Could not contact Controller Node. Trying to reconnect on next heartbeat...");
                try {
                    controllerSocket.close();
                } catch (IOException ignored) {

                }
            }
        }
    }
}
