package cs555.dfs.processing.chunkserverprocessing;

import cs555.dfs.messages.CleanSlices;
import cs555.dfs.messages.NodeInformation;
import cs555.dfs.messages.RequestChunk;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import static cs555.dfs.util.Constants.sliceSize;
import static cs555.dfs.util.Constants.storageDirectory;

public class HandleSliceCorruption {
    private Splitter splitter = new Splitter();
    private String alreadyAskedNode;

    /**
     * Rewrites all good bytes up until the corrupted slice
     * @param chunkBytes all current bytes, including the corrupted ones
     * @param corruptedSliceNumber the slice number containing the corruption
     * @param chunkName corrupted chunk's name
     * @throws IOException
     */
    public synchronized void overWriteGoodSlices(byte[] chunkBytes, int corruptedSliceNumber, String chunkName)
            throws IOException {
        int index = 0;
        int maximum = chunkBytes.length;
        int writeSize = sliceSize;

        System.out.println("Re-writing good bytes");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try(FileOutputStream fileOutputStream = new FileOutputStream(storageDirectory + chunkName)) {
            for (int sliceNumber = 0; sliceNumber < corruptedSliceNumber; sliceNumber++) {
                if (maximum - index < writeSize) {
                    writeSize = maximum - index;
                }
                byteArrayOutputStream.write(chunkBytes, index, writeSize);
                index += writeSize;
            }
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
        }
    }

    /**
     * Creates and sends a new RequestChunk message.
     * @param controllerSender TCPSender instance
     * @param chunkName name of chunk to find.
     * @param sourceServer the server holding the original chunk (this chunk server).
     * @param controllerSocket controller node socket.
     * @param clientID the client that originally requested the chunk.
     * @throws IOException
     */
    public synchronized void requestChunkLocation(TCPSender controllerSender,
                                                  String chunkName,
                                                  String sourceServer,
                                                  Socket controllerSocket, String clientID) throws IOException {
        RequestChunk requestChunk = new RequestChunk();
        requestChunk.setChunkName(chunkName);
        requestChunk.setChunkServerAddress(sourceServer + "::" + clientID);

        controllerSender.send(controllerSocket, requestChunk.getBytes());
    }

    /**
     * Upon receiving the location of an uncorrupted chunk, requests uncorrupted slices from that server.
     * The method takes note of which server is being asked for the clean slices just in case
     * that server's slices are corrupted as well. As the method is written now, the requesting server
     * will alternate between "recovery" servers when there are three file replications.
     * @param message contains chunk location information.
     * @param thisNodeID node that requested the chunk (this chunk server)
     * @param corruptedChunkName name of chunk to replace.
     * @param corruptedSliceNumber slice where corruption occurred.
     * @throws IOException
     */
    public synchronized void requestChunkFromServer(NodeInformation message, String thisNodeID, String corruptedChunkName,
                                                    int corruptedSliceNumber) throws IOException {
        String originalRequester = message.getNodeInfo().split("::")[0];
        String serverAddresses = message.getNodeInfo().split("::")[1];
        String[] chunkLocations = serverAddresses.split(",");
        String chunkHolder = "";
        for (String nodeID : chunkLocations) {
            if (!nodeID.equals(thisNodeID) && !nodeID.equals(alreadyAskedNode)) {
                chunkHolder = nodeID;
                alreadyAskedNode = nodeID;
                break;
            }
        }

        prepareAndSendSliceRequest(thisNodeID, corruptedChunkName, corruptedSliceNumber, chunkHolder, originalRequester);
    }

    /**
     * Sends a request for uncorrupted slices from the server selected in the requestChunkFromServer method
     * @param thisNodeID this node (home of the corrupted slice)
     * @param corruptedChunkName name of corrupted chunk
     * @param corruptedSliceNumber which slice is corrupted
     * @param chunkHolder the server that's holding another copy of the specified chunk
     * @param clientID the client that originally requested the file.
     * @throws IOException
     */
    private synchronized void prepareAndSendSliceRequest(String thisNodeID, String corruptedChunkName, int corruptedSliceNumber,
                                                         String chunkHolder, String clientID) throws IOException {
        RequestChunk chunkRequest = new RequestChunk();
        chunkRequest.setChunkServerAddress(thisNodeID + "::" + clientID);
        chunkRequest.setChunkName(corruptedChunkName + ":-:" + corruptedSliceNumber);

        Socket holderSocket = new Socket(splitter.getHost(chunkHolder), splitter.getPort(chunkHolder));

        System.out.println("Requesting uncorrupted slices from " + chunkHolder);
        TCPSender sender = new TCPSender();
        sender.send(holderSocket, chunkRequest.getBytes());
    }

    /**
     * Appends newly received slices of a file to the existing file.
     * @param cleanSlices
     * @throws IOException
     */
    public synchronized void writeCleanSlices(CleanSlices cleanSlices) throws IOException {
        String chunk = cleanSlices.getChunkName();
        byte[] sliceBytes = cleanSlices.getSlicesByteArray();

        try(FileOutputStream fileOutputStream = new FileOutputStream(storageDirectory + chunk, true)) {
//        try(FileOutputStream fileOutputStream = new FileOutputStream(storageDirectory + chunk)) {
            fileOutputStream.write(sliceBytes);
        }
    }
}
