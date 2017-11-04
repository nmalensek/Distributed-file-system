package cs555.dfs.messages;

import java.io.*;

public class ChunkServerDown implements Protocol, Event {
    private int messageType = CHUNK_SERVER_DOWN;
    private String downNode;

    public ChunkServerDown getType() {
        return this;
    }

    public String getNodeInfo() { return downNode; }
    public void setNodeInfo(String downNode) { this.downNode = downNode; }

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

        byte[] nodeBytes = downNode.getBytes();
        int nodeLength = nodeBytes.length;
        dataOutputStream.writeInt(nodeLength);
        dataOutputStream.write(nodeBytes);

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

        int nodeLength = dataInputStream.readInt();
        byte[] nodeBytes = new byte[nodeLength];
        dataInputStream.readFully(nodeBytes);

        downNode = new String(nodeBytes);

        byteArrayInputStream.close();
        dataInputStream.close();
    }
}
