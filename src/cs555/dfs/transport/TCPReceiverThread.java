package cs555.dfs.transport;

import cs555.dfs.eventfactory.EventFactory;
import cs555.dfs.messages.*;
import cs555.dfs.node.Node;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * code adapted from code provided by instructor at http://www.cs.colostate.edu/~cs455/lectures/CS455-HelpSession1.pdf
 */

public class TCPReceiverThread extends Thread implements Protocol {

    private Socket communicationSocket;
    private DataInputStream dataInputStream;
    private Node node;
    private EventFactory eventFactory = EventFactory.getInstance();

    public TCPReceiverThread(Socket communicationSocket, Node node) throws IOException {
        this.communicationSocket = communicationSocket;
        this.node = node;
        dataInputStream = new DataInputStream(communicationSocket.getInputStream());
    }

    /**
     * Listens for a message coming in.
     **/
    public void run() {
        int dataLength;
        while (communicationSocket != null) {
            try {
                dataLength = dataInputStream.readInt();

                byte[] data = new byte[dataLength];
                dataInputStream.readFully(data, 0, dataLength);

                determineMessageType(data);

            } catch (IOException ioe) {
                ioe.printStackTrace();
                communicationSocket = null;
//                System.out.println("A node left the overlay");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads first line of message to determine the message type, then passes that to a switch statement to process
     * the message the rest of the way and pass it to the node.
     *
     * @param marshalledBytes packaged message
     * @throws IOException
     */
    private void determineMessageType(byte[] marshalledBytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(marshalledBytes);
        DataInputStream dataInputStream =
                new DataInputStream(new BufferedInputStream(byteArrayInputStream));

        int messageType = dataInputStream.readInt();
        dataInputStream.close();

        switch (messageType) {
            case NODE_INFORMATION:
                Event<NodeInformation> nodeInformationEvent =
                        eventFactory.nodeInformationEvent(marshalledBytes);
                node.onEvent(nodeInformationEvent, communicationSocket);
                break;
            case MINOR_HEARTBEAT:
                Event<MinorHeartbeatMessage> minorHeartbeatMessageEvent =
                        eventFactory.minorHeartbeatMessageEvent(marshalledBytes);
                node.onEvent(minorHeartbeatMessageEvent, communicationSocket);
                break;
            case MAJOR_HEARTBEAT:
                Event<MajorHeartbeatMessage> majorHeartbeatMessageEvent =
                        eventFactory.majorHeartbeatMessageEvent(marshalledBytes);
                node.onEvent(majorHeartbeatMessageEvent, communicationSocket);
                break;
            case REQUEST_MAJOR_HEARTBEAT:
                Event<RequestMajorHeartbeat> requestMajorHeartbeatEvent =
                        eventFactory.requestMajorHeartbeatEvent(marshalledBytes);
                node.onEvent(requestMajorHeartbeatEvent, communicationSocket);
                break;
            case CHUNK:
                Event<Chunk> chunkEvent =
                        eventFactory.chunkEvent(marshalledBytes);
                node.onEvent(chunkEvent, communicationSocket);
                break;
            case WRITE_INQUIRY:
                Event<WriteFileInquiry> writeFileInquiryEvent =
                        eventFactory.writeFileInquiryEvent(marshalledBytes);
                node.onEvent(writeFileInquiryEvent, communicationSocket);
                break;
            case READ_INQUIRY:
                Event<ReadFileInquiry> readFileInquiryEvent =
                        eventFactory.readFileInquiryEvent(marshalledBytes);
                node.onEvent(readFileInquiryEvent, communicationSocket);
                break;
            case CHUNK_REQUEST:
                Event<RequestChunk> requestChunkEvent =
                        eventFactory.requestChunkEvent(marshalledBytes);
                node.onEvent(requestChunkEvent, communicationSocket);
                break;
            case CLEAN_SLICES:
                Event<CleanSlices> cleanSlicesEvent =
                        eventFactory.cleanSlicesEvent(marshalledBytes);
                node.onEvent(cleanSlicesEvent, communicationSocket);
                break;
            case PING:
                break;
            case DISCONNECT:
                System.out.println("Closing socket...");
                communicationSocket.setSoLinger(true, 0);
                    communicationSocket.close();
                break;
            default:
                System.out.println("Something went horribly wrong, please restart.");
        }
    }
}
