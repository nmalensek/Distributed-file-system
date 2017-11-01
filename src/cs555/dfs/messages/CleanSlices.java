package cs555.dfs.messages;

import java.io.*;

public class CleanSlices implements Protocol, Event {

    private int messageType = CLEAN_SLICES;
    private String chunkName;
    private String originalClientID;
    private byte[] slicesByteArray;

    public CleanSlices getType() {
        return this;
    }

    public String getChunkName() { return chunkName; }
    public void setChunkName(String fileName) { this.chunkName = fileName; }

    public byte[] getSlicesByteArray() { return slicesByteArray; }
    public void setSlicesByteArray(byte[] slicesByteArray) { this.slicesByteArray = slicesByteArray; }

    public String getOriginalClientID() { return originalClientID; }
    public void setOriginalClientID(String originalClientID) { this.originalClientID = originalClientID; }

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

        byte[] nameBytes = chunkName.getBytes();
        int nameLength = nameBytes.length;
        dataOutputStream.writeInt(nameLength);
        dataOutputStream.write(nameBytes);

        byte[] clientBytes = originalClientID.getBytes();
        int clientLength = clientBytes.length;
        dataOutputStream.writeInt(clientLength);
        dataOutputStream.write(clientBytes);

        int chunkBytesLength = slicesByteArray.length;
        dataOutputStream.writeInt(chunkBytesLength);
        dataOutputStream.write(slicesByteArray);

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

        int nameLength = dataInputStream.readInt();
        byte[] nameBytes = new byte[nameLength];
        dataInputStream.readFully(nameBytes);

        chunkName = new String(nameBytes);

        int clientLength = dataInputStream.readInt();
        byte[] clientBytes = new byte[clientLength];
        dataInputStream.readFully(clientBytes);

        originalClientID = new String(clientBytes);

        int chunkLength = dataInputStream.readInt();
        slicesByteArray = new byte[chunkLength];
        dataInputStream.readFully(slicesByteArray);

        byteArrayInputStream.close();
        dataInputStream.close();
    }
}
