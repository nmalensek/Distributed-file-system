package cs555.dfs.messages;

import java.io.*;

public class NodeInformation implements Protocol, Event {
    private int messageType = NODE_INFORMATION;
    private String nodeInfo;
    private long usableSpace;

    public NodeInformation getType() {
        return this;
    }

    public String getNodeInfo() { return nodeInfo; }
    public void setNodeInfo(String nodeInfo) { this.nodeInfo = nodeInfo; }

    public long getUsableSpace() { return usableSpace; }
    public void setUsableSpace(long usableSpace) { this.usableSpace = usableSpace; }

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

        dataOutputStream.writeLong(usableSpace);

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

        usableSpace = dataInputStream.readLong();

        byteArrayInputStream.close();
        dataInputStream.close();
    }



}
