package cs555.dfs.processing.chunkserverprocessing;

import cs555.dfs.messages.Chunk;
import cs555.dfs.messages.ReadFileInquiry;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.*;
import java.net.Socket;

import static cs555.dfs.util.Constants.chunkSize;
import static cs555.dfs.util.Constants.storageDirectory;

public class RetrieveChunk {

    private TCPSender sender = new TCPSender();
    private Splitter splitter = new Splitter();

    public void retrieveChunk(ReadFileInquiry chunkRequest) throws IOException {
        String chunkName = chunkRequest.getFilename();
        String requestor = chunkRequest.getClientAddress();
        Socket clientSocket = new Socket(splitter.getHost(requestor), splitter.getPort(requestor));

        Chunk retrievedChunk = new Chunk();
        retrievedChunk.setFileName(chunkName);
        retrievedChunk.setReplicationNodes("N/A");

        FileInputStream fileInputStream = new FileInputStream(storageDirectory + chunkName);

        byte[] chunkArray = new byte[chunkSize];

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int read;
        try {
            while ((read = fileInputStream.read(chunkArray)) != -1) {
                byteArrayOutputStream.write(chunkArray, 0, read);
                retrievedChunk.setChunkByteArray(byteArrayOutputStream.toByteArray());
                sender.send(clientSocket, retrievedChunk.getBytes());
                byteArrayOutputStream.reset();
                System.out.println("Sent chunk " + chunkName);
            }
        } finally {
            fileInputStream.close();
        }
    }
}
