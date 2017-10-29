package cs555.dfs.messages;

import java.io.*;

public class Chunk implements Protocol, Event {

    private int messageType = CHUNK;
    private String replicationNodes;
    private String fileName;
    private byte[] chunkByteArray;

    public Chunk getType() {
        return this;
    }

    public String getReplicationNodes() { return replicationNodes; }
    public void setReplicationNodes(String replicationNodes) { this.replicationNodes = replicationNodes; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

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

        int chunkBytesLength = chunkByteArray.length;
        dataOutputStream.writeInt(chunkBytesLength);
        dataOutputStream.write(chunkByteArray);

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

        int nodesLength = dataInputStream.readInt();
        byte[] replicationBytes = new byte[nodesLength];
        dataInputStream.readFully(replicationBytes);

        replicationNodes = new String(replicationBytes);

        int nameLength = dataInputStream.readInt();
        byte[] nameBytes = new byte[nameLength];
        dataInputStream.readFully(nameBytes);

        fileName = new String(nameBytes);

        int chunkLength = dataInputStream.readInt();
        chunkByteArray = new byte[chunkLength];
        dataInputStream.readFully(chunkByteArray);

        byteArrayInputStream.close();
        dataInputStream.close();
    }
}
