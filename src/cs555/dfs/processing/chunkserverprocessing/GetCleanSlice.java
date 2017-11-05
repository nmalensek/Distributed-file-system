package cs555.dfs.processing.chunkserverprocessing;

import cs555.dfs.messages.CleanSlices;
import cs555.dfs.messages.Disconnect;
import cs555.dfs.messages.RequestChunk;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Arrays;

import static cs555.dfs.util.Constants.chunkSize;
import static cs555.dfs.util.Constants.sliceSize;
import static cs555.dfs.util.Constants.storageDirectory;

public class GetCleanSlice {

    private String chunkName;
    private int sliceNumber;
    private String requestingServer;
    private int byteStartIndex;
    private Splitter split = new Splitter();

    public GetCleanSlice(RequestChunk chunkRequest) {
        chunkName = chunkRequest.getChunkName().split(":-:")[0];
        sliceNumber = Integer.parseInt(chunkRequest.getChunkName().split(":-:")[1]);
        requestingServer = chunkRequest.getChunkServerAddress().split("::")[0];
        if (sliceNumber == 0) {
            byteStartIndex = 0;
        } else {
            byteStartIndex = (sliceSize * sliceNumber);
        }
    }

    public synchronized void retrieveCleanSlice(String clientID) throws IOException {
        System.out.println("Getting requested slice");

        try(RandomAccessFile file = new RandomAccessFile(storageDirectory + chunkName, "r")) {
            file.seek(byteStartIndex);
            int remainingBytes = (int) (file.length() - byteStartIndex);
            byte[] cleanSlices = new byte[remainingBytes];
            file.readFully(cleanSlices);

            sendSlices(cleanSlices, clientID);
        }
    }

    private synchronized void sendSlices(byte[] sliceArray, String originalClient) throws IOException {
        System.out.println("Preparing message for " + requestingServer + " so it can send to " + originalClient);
        CleanSlices cleanSlices = new CleanSlices();
        cleanSlices.setChunkName(chunkName);
        cleanSlices.setSlicesByteArray(sliceArray);
        cleanSlices.setOriginalClientID(originalClient);


        TCPSender sender = new TCPSender();
        Socket requesterConnection = new Socket(split.getHost(requestingServer), split.getPort(requestingServer));
        sender.send(requesterConnection, cleanSlices.getBytes());
        System.out.println("Sent new version of slice " + sliceNumber);

        Disconnect dc = new Disconnect();
        sender.send(requesterConnection, dc.getBytes());
    }

//    public synchronized void retrieveCleanChunk(String clientID) throws IOException {
//
//        byte[] chunkBytes = new byte[chunkSize];
//        int bytesRead;
//        try (FileInputStream fileInputStream = new FileInputStream(storageDirectory + chunkName)) {
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//            while ((bytesRead = fileInputStream.read(chunkBytes)) != -1) {
//                byteArrayOutputStream.write(chunkBytes, 0, bytesRead);
//            }
//            chunkBytes = byteArrayOutputStream.toByteArray();
//            byteArrayOutputStream.reset();
//        }
//
//        sendSlices(chunkBytes, clientID);
//    }

}
