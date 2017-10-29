package cs555.dfs.processing.chunkserverprocessing;

import cs555.dfs.hash.ComputeHash;
import cs555.dfs.messages.Chunk;
import cs555.dfs.messages.ReadFileInquiry;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static cs555.dfs.util.Constants.*;

public class RetrieveChunk {

    private TCPSender sender = new TCPSender();
    private Splitter splitter = new Splitter();
    private ArrayList<String> checkedIntegrityData = new ArrayList<>();
    private boolean okToSend = false;

    public synchronized void retrieveChunk(ReadFileInquiry chunkRequest) throws IOException {
        String chunkName = chunkRequest.getFilename();
        String requestor = chunkRequest.getClientAddress();
        Socket clientSocket = new Socket(splitter.getHost(requestor), splitter.getPort(requestor));

        Chunk retrievedChunk = new Chunk();
        retrievedChunk.setFileName(chunkName);
        retrievedChunk.setReplicationNodes("N/A");

        byte[] chunkArray = new byte[chunkSize];

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int read;
        try (FileInputStream fileInputStream = new FileInputStream(storageDirectory + chunkName)) {
            while ((read = fileInputStream.read(chunkArray)) != -1) {
                byteArrayOutputStream.write(chunkArray, 0, read);
                retrievedChunk.setChunkByteArray(byteArrayOutputStream.toByteArray());
                checkChunkSlices(byteArrayOutputStream.toByteArray(), chunkName);
//                sender.send(clientSocket, retrievedChunk.getBytes());
                byteArrayOutputStream.reset();
//                System.out.println("Sent chunk " + chunkName);
            }
        }
    }

    private synchronized void checkChunkSlices(byte[] retrievedChunk, String chunkName) throws IOException {

        ArrayList<String> writtenHashes = getLoggedIntegrityData(chunkName);
        ArrayList<String> memoryHashes = getCurrentIntegrityHashes(retrievedChunk);

        System.out.println(writtenHashes.equals(memoryHashes));
        if (!writtenHashes.equals(memoryHashes)) {
            System.out.println("Corruption detected in chunk " + chunkName);
        } else {
            System.out.println("Sent chunk " + chunkName);
        }
    }

    private synchronized ArrayList<String> getLoggedIntegrityData(String chunkName) throws IOException {

        ArrayList<String> loggedIntegrityData = new ArrayList<>();

        try (BufferedReader bufferedReader =
                     new BufferedReader(new FileReader(storageDirectory + chunkName + integrity))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                loggedIntegrityData.add(line);
            }
        }
        return loggedIntegrityData;
    }

    private synchronized ArrayList<String> getCurrentIntegrityHashes(byte[] retrievedChunk) {

        ArrayList<String> currentIntegrityData = new ArrayList<>();

        int index = 0;
        int maximum = retrievedChunk.length;
        int writeSize = sliceSize;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        while (index < maximum) {
            if (maximum - index < writeSize) { writeSize = maximum - index; }
            byteArrayOutputStream.write(retrievedChunk, index, writeSize);
            currentIntegrityData.add(ComputeHash.SHA1FromBytes(byteArrayOutputStream.toByteArray()));
            index += byteArrayOutputStream.size();
            byteArrayOutputStream.reset();
        }
        return currentIntegrityData;
    }
}
