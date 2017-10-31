package cs555.dfs.processing.chunkserverprocessing;

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

    public synchronized void overWriteGoodSlices(byte[] chunkBytes, int corruptedSliceNumber, String chunkName)
            throws IOException {
        int index = 0;
        int maximum = chunkBytes.length;
        int writeSize = sliceSize;

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

    public synchronized void requestChunkLocation(TCPSender controllerSender,
                                                  String chunkName,
                                                  String sourceServer,
                                                  Socket controllerSocket) throws IOException {
        RequestChunk requestChunk = new RequestChunk();
        requestChunk.setChunkName(chunkName);
        requestChunk.setChunkServerAddress(sourceServer);

        controllerSender.send(controllerSocket, requestChunk.getBytes());
    }

    public synchronized void requestChunkFromServer(NodeInformation message, String thisNodeID, String corruptedChunkName,
                                                    int corruptedSliceNumber) throws IOException {
        String[] chunkLocations = message.getNodeInfo().split(",");
        String chunkHolder = "";
        for (String nodeID : chunkLocations) {
            if (!nodeID.equals(thisNodeID)) {
                chunkHolder = nodeID;
                break;
            }
        }
        prepareAndSendMessage(thisNodeID, corruptedChunkName, corruptedSliceNumber, chunkHolder);
    }

    private synchronized void prepareAndSendMessage(String thisNodeID, String corruptedChunkName, int corruptedSliceNumber,
                                                    String chunkHolder) throws IOException {
        RequestChunk chunkRequest = new RequestChunk();
        chunkRequest.setChunkServerAddress(thisNodeID);
        chunkRequest.setChunkName(corruptedChunkName + ":-:" + corruptedSliceNumber);

        Socket holderSocket = new Socket(splitter.getHost(chunkHolder), splitter.getPort(chunkHolder));

        TCPSender sender = new TCPSender();
        sender.send(holderSocket, chunkRequest.getBytes());
    }
}
