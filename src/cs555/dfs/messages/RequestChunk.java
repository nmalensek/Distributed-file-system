package cs555.dfs.messages;

import java.io.*;

public class RequestChunk implements Protocol, Event {
    private int messageType = CHUNK_REQUEST;
    private String chunkServerAddress;
    private String chunkName;

    public RequestChunk getType() {
        return this;
    }

    @Override
    public int getMessageType() {
        return messageType;
    }

    public String getChunkServerAddress() { return chunkServerAddress; }

    public void setChunkServerAddress(String chunkServerAddress) { this.chunkServerAddress = chunkServerAddress; }

    public String getChunkName() { return chunkName; }

    public void setChunkName(String chunkName) { this.chunkName = chunkName; }

    //marshalls bytes
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream =
                new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(messageType);

        byte[] serverBytes = chunkServerAddress.getBytes();
        int serverLength = serverBytes.length;
        dataOutputStream.writeInt(serverLength);
        dataOutputStream.write(serverBytes);

        byte[] chunkNameBytes = chunkName.getBytes();
        int chunkNameLength = chunkNameBytes.length;
        dataOutputStream.writeInt(chunkNameLength);
        dataOutputStream.write(chunkNameBytes);

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

        int serverLength = dataInputStream.readInt();
        byte[] serverBytes = new byte[serverLength];
        dataInputStream.readFully(serverBytes);

        chunkServerAddress = new String(serverBytes);

        int chunkNameLength = dataInputStream.readInt();
        byte[] chunkNameBytes = new byte[chunkNameLength];
        dataInputStream.readFully(chunkNameBytes);

        chunkName = new String(chunkNameBytes);

        byteArrayInputStream.close();
        dataInputStream.close();
    }
}
