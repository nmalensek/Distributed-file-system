package cs555.dfs.eventfactory;


import cs555.dfs.messages.*;

import java.io.IOException;

public final class EventFactory {

    /**
     * Class creates events based on the type of message received at a node so the node can respond accordingly.
     */

    private static final EventFactory instance = new EventFactory();

    private EventFactory() { }

    public static EventFactory getInstance() {
        return instance;
    }

    public static Event<NodeInformation> nodeInformationEvent(
            byte[] marshalledBytes) throws IOException {
        NodeInformation nodeInformation = new NodeInformation();
        nodeInformation.readMessage(marshalledBytes);
        return nodeInformation;
    }

    public static Event<MinorHeartbeatMessage> minorHeartbeatMessageEvent(
            byte[] marshalledByes) throws IOException {
        MinorHeartbeatMessage minorHeartbeatMessage = new MinorHeartbeatMessage();
        minorHeartbeatMessage.readMessage(marshalledByes);
        return minorHeartbeatMessage;
    }

    public static Event<MajorHeartbeatMessage> majorHeartbeatMessageEvent(
            byte[] marshalledBytes) throws IOException {
        MajorHeartbeatMessage majorHeartbeatMessage = new MajorHeartbeatMessage();
        majorHeartbeatMessage.readMessage(marshalledBytes);
        return majorHeartbeatMessage;
    }

    public static Event<RequestMajorHeartbeat> requestMajorHeartbeatEvent(
            byte[] marshalledBytes) throws IOException {
        RequestMajorHeartbeat requestMajorHeartbeat = new RequestMajorHeartbeat();
        requestMajorHeartbeat.readMessage(marshalledBytes);
        return requestMajorHeartbeat;
    }

    public static Event<Chunk> chunkEvent(
            byte[] marshalledBytes) throws IOException {
        Chunk chunk = new Chunk();
        chunk.readMessage(marshalledBytes);
        return chunk;
    }

    public static Event<WriteFileInquiry> writeFileInquiryEvent(
            byte[] marshalledBytes) throws IOException {
        WriteFileInquiry writeFileInquiry = new WriteFileInquiry();
        writeFileInquiry.readMessage(marshalledBytes);
        return writeFileInquiry;
    }

    public static Event<ReadFileInquiry> readFileInquiryEvent(
            byte[] marshalledBytes) throws IOException {
        ReadFileInquiry readFileInquiry = new ReadFileInquiry();
        readFileInquiry.readMessage(marshalledBytes);
        return readFileInquiry;
    }

    public static Event<RequestChunk> requestChunkEvent(byte[] marshalledBytes) throws IOException {
        RequestChunk requestChunk = new RequestChunk();
        requestChunk.readMessage(marshalledBytes);
        return requestChunk;
    }

}
