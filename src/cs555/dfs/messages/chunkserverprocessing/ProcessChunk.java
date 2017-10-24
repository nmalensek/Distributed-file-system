package cs555.dfs.messages.chunkserverprocessing;

import cs555.dfs.messages.Chunk;
import cs555.dfs.node.ChunkServer;
import cs555.dfs.util.ChunkMetadata;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ProcessChunk {

    private ChunkServer owner;
    private String metadataFilepath;
    private String storageDirectory;

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
        } finally {
            fileOutputStream.close();
        }
    }

    private void writeHashForSlices() {
        
    }

    private void forwardToNextNode() {

    }

    //get replication nodes, if it's yourself, remove and put the other(s) in new message
    //get filename, which will include chunk#, so split out for the metadata and also write full name
    //write file to disk
    //forward to B/C/do nothing

}
