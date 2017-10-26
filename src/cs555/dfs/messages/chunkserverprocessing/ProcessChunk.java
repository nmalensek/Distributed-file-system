package cs555.dfs.messages.chunkserverprocessing;

import cs555.dfs.messages.Chunk;
import cs555.dfs.node.ChunkServer;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.ChunkMetadata;
import cs555.dfs.util.Splitter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ProcessChunk {

    private ChunkServer owner;
    private String metadataFilepath;
    private String storageDirectory;
    private Splitter splitter = new Splitter();
    private TCPSender forwardChunk = new TCPSender();
    private String integrity = "_integrity";

    public ProcessChunk(ChunkServer owner, String metadataFilepath, String storageDirectory) {
        this.owner = owner;
        this.metadataFilepath = metadataFilepath;
        this.storageDirectory = storageDirectory;
    }

    private void writeMetadata(String metadata) throws IOException {
        List<String> line = Arrays.asList(metadata);
        Path path = Paths.get(metadataFilepath);
        Files.write(path, line);
    }

    public void writeAndLogChunk(Chunk chunkInformation) throws IOException {
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
            forwardToNextNode(chunkInformation);
        } finally {
            fileOutputStream.close();
        }
    }

    private void writeHashForSlices(Chunk fileChunk) throws IOException {
        byte[] chunkBytes = fileChunk.getChunkByteArray();

        byte[] slice = new byte[8192];

        try (FileOutputStream fileOutputStream = new FileOutputStream(storageDirectory + fileChunk.getFileName() + integrity)) {

        }
    }

    /**
     * Get next node to forward chunk to, then add other nodes back as replication nodes.
     * If the next node is the last node, nothing's added and this method's skipped at the final node.
     * @param chunk Chunk message that originated from client.
     */
    private void forwardToNextNode(Chunk chunk) throws IOException {
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
