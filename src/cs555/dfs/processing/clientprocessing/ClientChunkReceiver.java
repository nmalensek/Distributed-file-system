package cs555.dfs.processing.clientprocessing;

import cs555.dfs.messages.Chunk;
import cs555.dfs.messages.ChunkServerDown;
import cs555.dfs.messages.NodeInformation;
import cs555.dfs.messages.ReadFileInquiry;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

import static cs555.dfs.util.Constants.retrievalDirectory;

public class ClientChunkReceiver {

    private TCPSender clientSender;
    private String parentID;
    private Socket controllerNodeSocket;
    private int totalChunks;
    private int receivedChunkNumber;

    public ClientChunkReceiver(TCPSender clientSender, String parentID, Socket controllerNodeSocket) {
        this.clientSender = clientSender;
        this.parentID = parentID;
        this.controllerNodeSocket = controllerNodeSocket;
    }

    /**
     * Contacts chunk server(s) given by the controller node to request the chunk(s) of the
     * user-specified file that each chunk server holds.
     * @param chunkLocations address(es) of the node(s) holding the chunks of the user-specified file.
     * @throws IOException
     */
    public void processChunkLocations(NodeInformation chunkLocations,
                                      TreeMap<String, List<String>> chunkLocationMap) throws IOException {
        if (chunkLocations.getNodeInfo().isEmpty()) {
            System.out.println("Controller could not locate file, please re-enter.");
            return;
        }
        //50chunks#!#chunkName:-:host:port|host:port|host:port,,chunkName:-:host:port|host:port|host:port,,
        String[] numChunksPlusChunks = chunkLocations.getNodeInfo().split("#!#");
        totalChunks = Integer.parseInt(numChunksPlusChunks[0]);
        String[] chunkNamePlusLocation = numChunksPlusChunks[1].split(",,");
        System.out.println(chunkLocations.getNodeInfo());

        for (String nameAndLocations : chunkNamePlusLocation) {
            String chunkName = nameAndLocations.split(":-:")[0];
            String locations = nameAndLocations.split(":-:")[1];
            List<String> hostPortList = new ArrayList<>(Arrays.asList(locations.split("\\|")));

            chunkLocationMap.put(chunkName, hostPortList);
        }

        requestNextChunk(chunkLocationMap);

    }

    public synchronized void handleIncomingChunk(Chunk chunkMessage, String filenameToRead,
                                    TreeMap<String, List<String>> chunkLocationMap) throws IOException {
        //write and request next chunk
        receivedChunkNumber++;
        writeChunk(chunkMessage.getChunkByteArray(), filenameToRead);
        if (receivedChunkNumber == totalChunks) {
            System.out.println("Got all requested chunks.");
            receivedChunkNumber = 0;
        } else {
            requestNextChunk(chunkLocationMap);
        }
    }

    /**
     * Writes chunks to a file as they come in.
     * @throws IOException
     */
    private void writeChunk(byte[] chunkBytes, String filenameToRead) throws IOException {
        File dir = new File(retrievalDirectory);
        dir.mkdirs();
        try (FileOutputStream fileOutputStream = new FileOutputStream(retrievalDirectory + filenameToRead, true)) {
            fileOutputStream.write(chunkBytes);
        }
    }

    private void requestNextChunk(TreeMap<String, List<String>> chunkLocationMap) throws IOException {
        Map.Entry<String, List<String>> nextChunk = chunkLocationMap.firstEntry();
        int index = 0;
        boolean requestSuccessful = false;

        ReadFileInquiry requestChunk = new ReadFileInquiry();
        requestChunk.setClientAddress(parentID);
        requestChunk.setFilename(nextChunk.getKey());

        while (!requestSuccessful) {
            String chunkHolderID = nextChunk.getValue().get(0); //down nodes are removed, so only get id from first element

            try {
                Socket chunkServerSocket = new Socket(
                        Splitter.getHost(chunkHolderID), Splitter.getPort(chunkHolderID));
                clientSender.send(chunkServerSocket, requestChunk.getBytes());
                chunkLocationMap.remove(nextChunk.getKey());
                requestSuccessful = true;
            } catch (IOException e) {
                ChunkServerDown chunkServerDown = new ChunkServerDown();
                chunkServerDown.setNodeInfo(chunkHolderID);
                clientSender.send(controllerNodeSocket, chunkServerDown.getBytes());
                System.out.println("Unable to request chunks from " + chunkHolderID + ", controller has been notified.");
                System.out.println("Chunk will be requested from secondary location.");
                removeDownChunkServer(chunkLocationMap, chunkHolderID);
                index++;
                if (index > 2) {
                    System.out.println("No servers holding requested chunk responded, aborting chunk retrieval.");
                    chunkLocationMap.clear();
                    break;
                }
            }
        }
    }

    private void removeDownChunkServer(TreeMap<String, List<String>> locationMap, String downServerID) {
        for (String chunkName : locationMap.keySet()) {
            List<String> locationList = locationMap.get(chunkName);
            for (int i = 0; i < locationList.size() - 1; i++) {
                if (locationList.get(i).equals(downServerID)) {
                    locationList.remove(i);
                    break;
                }
            }
        }
        System.out.println(locationMap);
    }
}
