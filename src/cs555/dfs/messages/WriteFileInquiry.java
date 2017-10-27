package cs555.dfs.messages;

import java.io.*;


public class WriteFileInquiry implements Protocol, Event {
    private int messageType = WRITE_INQUIRY;
    private String clientAddress;

    public WriteFileInquiry getType() {
        return this;
    }

    @Override
    public int getMessageType() {
        return messageType;
    }

    public String getClientAddress() { return clientAddress; }

    public void setClientAddress(String clientAddress) { this.clientAddress = clientAddress; }

    //marshalls bytes
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream =
                new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(messageType);

        byte[] clientBytes = clientAddress.getBytes();
        int clientLength = clientAddress.length();
        dataOutputStream.writeInt(clientLength);
        dataOutputStream.write(clientBytes);

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

        byteArrayInputStream.close();
        dataInputStream.close();
    }
}
