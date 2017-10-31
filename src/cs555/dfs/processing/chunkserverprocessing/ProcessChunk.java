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

    public synchronized void writeAndLogChunk(Chunk chunkInformation) throws IOException {
        String fullFileName = chunkInformation.getFileName();
        String originalFileName = fullFileName.substring(0, fullFileName.lastIndexOf('_'));
        long timestamp = System.currentTimeMillis();

        ChunkMetadata data = new ChunkMetadata(fullFileName, 1, originalFileName, timestamp);
        owner.getChunksResponsibleFor().put(fullFileName, data);
        owner.getNewChunksResponsibleFor().add(data.toString());
        writeMetadata(data.toString());

        FileOutputStream fileOutputStream = new FileOutputStream(storageDirectory + fullFileName);

        try {
            fileOutputStream.write(chunkInformation.getChunkByteArray());
            writeHashForSlices(chunkInformation);
            forwardToNextNode(chunkInformation);
        } finally {
            fileOutputStream.close();
        }
    }

    private void writeMetadata(String metadata) throws IOException {
        try(FileWriter metadataWriter = new FileWriter(metadataFilepath)) {
            metadataWriter.write(metadata + "\n");
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
        if (!chunk.getReplicationNodes().isEmpty()) {
            String[] nodeArray = chunk.getReplicationNodes().split(",");
            String nextNode = nodeArray[0];

            StringBuilder nodeAddress = new StringBuilder();
            for (int i = 1; i < nodeArray.length; i++) {
                nodeAddress.append(nodeArray[i]).append(",");
            }
            chunk.setReplicationNodes(nodeAddress.toString());

            Socket nextSocket = new Socket(splitter.getHost(nextNode), splitter.getPort(nextNode));
            forwardChunk.send(nextSocket, chunk.getBytes());
        }
    }

}
