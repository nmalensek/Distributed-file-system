package cs555.dfs.messages;

import java.io.*;

public class ReadFileInquiry implements Protocol, Event {
    private int messageType = READ_INQUIRY;
    private String clientAddress;
    private String filename;

    public ReadFileInquiry getType() {
        return this;
    }

    @Override
    public int getMessageType() {
        return messageType;
    }

    public String getClientAddress() { return clientAddress; }

    public void setClientAddress(String clientAddress) { this.clientAddress = clientAddress; }

    public String getFilename() { return filename; }

    public void setFilename(String filename) { this.filename = filename; }

    //marshalls bytes
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream =
                new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(messageType);

        byte[] clientBytes = clientAddress.getBytes();
        int clientLength = clientBytes.length;
        dataOutputStream.writeInt(clientLength);
        dataOutputStream.write(clientBytes);

        byte[] filenameBytes = filename.getBytes();
        int filenameLength = filenameBytes.length;
        dataOutputStream.writeInt(filenameLength);
        dataOutputStream.write(filenameBytes);

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

        int clientLength = dataInputStream.readInt();
        byte[] clientBytes = new byte[clientLength];
        dataInputStream.readFully(clientBytes);

        clientAddress = new String(clientBytes);

        int filenameLength = dataInputStream.readInt();
        byte[] filenameBytes = new byte[filenameLength];
        dataInputStream.readFully(filenameBytes);

        filename = new String(filenameBytes);

        byteArrayInputStream.close();
        dataInputStream.close();
    }
}
