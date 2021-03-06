package cs555.dfs.processing.chunkserverprocessing;

import cs555.dfs.hash.ComputeHash;
import cs555.dfs.messages.Chunk;
import cs555.dfs.node.ChunkServer;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.ChunkMetadata;
import cs555.dfs.util.Splitter;

import java.io.*;
import java.net.Socket;

import static cs555.dfs.util.Constants.integrity;
import static cs555.dfs.util.Constants.metadataFilepath;
import static cs555.dfs.util.Constants.sliceSize;

public class ProcessChunk {

    private ChunkServer owner;
    private String storageDirectory;
    private Splitter splitter = new Splitter();
    private TCPSender forwardChunk = new TCPSender();

    public ProcessChunk(ChunkServer owner, String storageDirectory) throws IOException {
        this.owner = owner;
        this.storageDirectory = storageDirectory;
    }

    /**
     * For a given chunk, the method writes bytes to local storage, calls writeMetadata method,
     * and forwards the chunk information to the next destination node (if applicable).
     * @param chunkInformation information, including bytes, of a new chunk.
     * @throws IOException
     */
    public synchronized void writeAndLogChunk(Chunk chunkInformation) throws IOException {
        String fullFileName = chunkInformation.getFileName();
        String originalFileName = fullFileName.substring(0, fullFileName.lastIndexOf('_'));
        long timestamp = System.currentTimeMillis();

        ChunkMetadata data = new ChunkMetadata(fullFileName, 1, originalFileName, timestamp);
        owner.getChunksResponsibleFor().put(fullFileName, data);
        owner.getNewChunksResponsibleFor().add(data.toString());
        writeMetadata(data.toString());

        try (FileOutputStream fileOutputStream = new FileOutputStream(storageDirectory + fullFileName)) {
            fileOutputStream.write(chunkInformation.getChunkByteArray());
            writeHashForSlices(chunkInformation);
            forwardToNextNode(chunkInformation);
        }
        System.out.println("Got chunk " + fullFileName);
    }

    /**
     * Writes out metadata to metadata file, adding a newline at the end of the metadata string.
     * @param metadata a pre-formed metadata string.
     * @throws IOException
     */
    private synchronized void writeMetadata(String metadata) throws IOException {
        File metadataFolder = new File(storageDirectory);
        metadataFolder.mkdirs();
        metadataFolder.deleteOnExit();
        File metadataFile = new File(metadataFilepath);
        metadataFile.createNewFile();
        metadataFile.deleteOnExit();

        try(FileWriter metadataWriter = new FileWriter(metadataFilepath, true)) {
            metadataWriter.write(metadata + "\n");
            metadataWriter.flush();
        }
    }

    /**
     * Given a byte array, get the SHA-1 hash for each 8kb slice and write it to a file.
     * @param fileChunk Chunk of a split file.
     * @throws IOException
     */
    public synchronized void writeHashForSlices(Chunk fileChunk) throws IOException {
        byte[] chunkBytes = fileChunk.getChunkByteArray();

        int index = 0;
        int maximum = chunkBytes.length;
        int writeSize = sliceSize;

        FileWriter fileWriter = new FileWriter(storageDirectory + fileChunk.getFileName() + integrity);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        while (index < maximum) {
            if (maximum - index < writeSize) { writeSize = maximum - index; }
            byteArrayOutputStream.write(chunkBytes, index, writeSize);
            fileWriter.write(ComputeHash.SHA1FromBytes(byteArrayOutputStream.toByteArray()) + "\n");
            index += byteArrayOutputStream.size();
            byteArrayOutputStream.reset();
        }
        fileWriter.close();
    }

    /**
     * Get next node to forward chunk to, then add other nodes back as replication nodes.
     * If the next node is the last node, nothing's added and this method's skipped at the final node.
     * @param chunk Chunk message that originated from client.
     */
    private synchronized void forwardToNextNode(Chunk chunk) throws IOException {
        if (!chunk.getReplicationNodes().isEmpty() && !chunk.getReplicationNodes().equals("N/A")) {
            String[] nodeArray = chunk.getReplicationNodes().split(",");
            String nextNode = nodeArray[0];

            StringBuilder nodeAddress = new StringBuilder();
            for (int i = 1; i < nodeArray.length; i++) {
                nodeAddress.append(nodeArray[i]).append(",");
            }
            chunk.setReplicationNodes(nodeAddress.toString());

            Socket nextSocket = new Socket(splitter.getHost(nextNode), splitter.getPort(nextNode));
            forwardChunk.send(nextSocket, chunk.getBytes());
            System.out.println("Forwarding chunk to " + nextNode);
        }
    }

}
