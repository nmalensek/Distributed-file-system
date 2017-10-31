package cs555.dfs.processing.chunkserverprocessing;

import cs555.dfs.messages.CleanSlices;
import cs555.dfs.messages.Disconnect;
import cs555.dfs.messages.RequestChunk;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

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
        requestingServer = chunkRequest.getChunkServerAddress();
        byteStartIndex = (sliceSize * sliceNumber) - 1;
    }

    public void retrieveCleanSlice() throws IOException {

        try(RandomAccessFile file = new RandomAccessFile(storageDirectory + chunkName, "r")) {
            file.seek(byteStartIndex);
            int remainingBytes = (int) (byteStartIndex - file.length());
            byte[] cleanSlices = new byte[remainingBytes];
            file.readFully(cleanSlices);

            sendSlices(cleanSlices);
        }
    }

    private void sendSlices(byte[] sliceArray) throws IOException {
        CleanSlices cleanSlices = new CleanSlices();
        cleanSlices.setChunkName(chunkName);
        cleanSlices.setSlicesByteArray(sliceArray);


        TCPSender sender = new TCPSender();
        Socket requesterConnection = new Socket(split.getHost(requestingServer), split.getPort(requestingServer));
        sender.send(requesterConnection, cleanSlices.getBytes());
        System.out.println("Sent new slice");

        Disconnect dc = new Disconnect();
        sender.send(requesterConnection, dc.getBytes());
    }

}
