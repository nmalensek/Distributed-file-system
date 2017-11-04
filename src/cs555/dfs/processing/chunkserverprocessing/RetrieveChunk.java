package cs555.dfs.processing.chunkserverprocessing;

import cs555.dfs.hash.ComputeHash;
import cs555.dfs.messages.Chunk;
import cs555.dfs.messages.CleanSlices;
import cs555.dfs.messages.NodeInformation;
import cs555.dfs.messages.ReadFileInquiry;
import cs555.dfs.node.ChunkServer;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import static cs555.dfs.util.Constants.*;

public class RetrieveChunk {

    private TCPSender sender = new TCPSender();
    private ChunkServer parent;
    private int corruptedSlice;
    private String corruptedChunkName;
    private boolean corrupted = false;
    private HandleSliceCorruption handleCorruption = new HandleSliceCorruption();

    public synchronized void retrieveChunk(ReadFileInquiry chunkRequest, ChunkServer parent) throws IOException {
        this.parent = parent;
        String chunkName = chunkRequest.getFilename();
        String requester = chunkRequest.getClientAddress();
        Socket clientSocket = new Socket(Splitter.getHost(requester), Splitter.getPort(requester));

        Chunk retrievedChunk = new Chunk();
        retrievedChunk.setFileName(chunkName);
        retrievedChunk.setReplicationNodes("N/A");

        byte[] chunkArray = new byte[chunkSize];
        byte[] corruptedBytes = new byte[chunkSize];

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int read;
        try (FileInputStream fileInputStream = new FileInputStream(storageDirectory + chunkName)) {
            while ((read = fileInputStream.read(chunkArray)) != -1) {
                byteArrayOutputStream.write(chunkArray, 0, read);
                checkChunkSlices(byteArrayOutputStream.toByteArray(), chunkName);
                if (!corrupted) {
                    retrievedChunk.setChunkByteArray(byteArrayOutputStream.toByteArray());
                sender.send(clientSocket, retrievedChunk.getBytes());
                System.out.println("Sent chunk " + chunkName);
                } else {
                    System.out.println("Corruption detected in slice " + corruptedSlice +
                            " of chunk " + corruptedChunkName + ", initiating recovery.");
                    corruptedBytes = Arrays.copyOf(chunkArray, chunkSize);
                    break;
                }
                byteArrayOutputStream.reset();
            }
        }
        if (corrupted) {
            corrupted = false;
            handleCorruption.overWriteGoodSlices(corruptedBytes, corruptedSlice, chunkName);
            handleCorruption.requestChunkLocation(sender, chunkName, parent.getNodeID(),
                    parent.getControllerNodeSocket(), requester);
        }
    }

    private synchronized void checkChunkSlices(byte[] retrievedChunk, String chunkName) throws IOException {

        ArrayList<String> writtenHashes = getLoggedIntegrityData(chunkName);
        ArrayList<String> memoryHashes = getCurrentIntegrityHashes(retrievedChunk);

        for (int i = 0; i < writtenHashes.size(); i++) {
            if (!memoryHashes.contains(writtenHashes.get(i))) {
                corruptedSlice = i;
                corruptedChunkName = chunkName;
                corrupted = true;
                break;
            }
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

    public synchronized void askForCleanChunk(NodeInformation message, String nodeID) throws IOException {
        handleCorruption.requestChunkFromServer(message, nodeID, corruptedChunkName, corruptedSlice);
    }

    public synchronized void writeSlices(CleanSlices message) throws IOException {
        handleCorruption.writeCleanSlices(message);
        ReadFileInquiry inquiry = new ReadFileInquiry();
        inquiry.setFilename(message.getChunkName());
        inquiry.setClientAddress(message.getOriginalClientID());
        retrieveChunk(inquiry, parent);
    }
}
