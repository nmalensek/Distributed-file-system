package cs555.dfs.messages;

import java.io.*;
import java.nio.file.Files;

public class Chunk implements Protocol, Event {

    private int messageType = CHUNK;
    private String replicationNodes;
    private String fileName;
    private File fileChunk;
    private byte[] chunkByteArray;

    public Chunk getType() {
        return this;
    }

    public String getReplicationNodes() { return replicationNodes; }
    public void setReplicationNodes(String replicationNodes) { this.replicationNodes = replicationNodes; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public File getFileChunk() { return fileChunk; }
    public void setFileChunk(File fileChunk) { this.fileChunk = fileChunk; }

    public byte[] getChunkByteArray() { return chunkByteArray; }
    public void setChunkByteArray(byte[] chunkByteArray) { this.chunkByteArray = chunkByteArray; }

    @Override
    public int getMessageType() {
        return messageType;
    }

    //marshalls bytes
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream =
                new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(messageType);

        byte[] nodeBytes = replicationNodes.getBytes();
        int nodeLength = nodeBytes.length;
        dataOutputStream.writeInt(nodeLength);
        dataOutputStream.write(nodeBytes);

        byte[] nameBytes = fileName.getBytes();
        int nameLength = nameBytes.length;
        dataOutputStream.writeInt(nameLength);
        dataOutputStream.write(nameBytes);

        byte[] fileChunkBytes = Files.readAllBytes(fileChunk.toPath());
        int fileBytesLength = fileChunkBytes.length;
        dataOutputStream.writeInt(fileBytesLength);
        dataOutputStream.write(fileChunkBytes);

        dataOutputStream.flush();
        marshalledBytes = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return marshalledBytes;
    }

    public void readMessage(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(marshalledBytes);
        DataInputStream dataInputStream =
                new DataInputStream(new BufferedInputStream(byteArrayInputStream));

        messageType = dataInputStream.readInt();



        byteArrayInputStream.close();
        dataInputStream.close();
    }
}
