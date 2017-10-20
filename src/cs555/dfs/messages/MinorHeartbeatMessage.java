package cs555.dfs.messages;

import java.io.*;

public class MinorHeartbeatMessage implements Protocol, Event {
    private int messageType = MINOR_HEARTBEAT;
    private String nodeInfo;
    private String newChunkData;

    public MinorHeartbeatMessage getType() {
        return this;
    }

    public String getNodeInfo() { return nodeInfo; }
    public void setNodeInfo(String nodeInfo) { this.nodeInfo = nodeInfo; }

    public String getNewChunkData() { return newChunkData; }
    public void setNewChunkData(String newChunkData) { this.newChunkData = newChunkData; }

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

        byte[] infoBytes = nodeInfo.getBytes();
        int infoLength = infoBytes.length;
        dataOutputStream.writeInt(infoLength);
        dataOutputStream.write(infoBytes);

        byte[] chunkDataBytes = newChunkData.getBytes();
        int dataLength = chunkDataBytes.length;
        dataOutputStream.writeInt(dataLength);
        dataOutputStream.write(chunkDataBytes);

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

        int infoLength = dataInputStream.readInt();
        byte[] infoBytes = new byte[infoLength];
        dataInputStream.readFully(infoBytes);

        nodeInfo = new String(infoBytes);

        int dataLength = dataInputStream.readInt();
        byte[] dataBytes = new byte[dataLength];
        dataInputStream.readFully(dataBytes);

        newChunkData = new String(dataBytes);

        byteArrayInputStream.close();
        dataInputStream.close();
    }
}
