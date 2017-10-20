package cs555.dfs.eventfactory;


import cs555.dfs.messages.Event;
import cs555.dfs.messages.NodeInformation;

import java.io.IOException;

public final class EventFactory {

    /**
     * Class creates events based on the type of message received at a chord.messaging.node so the chord.messaging.node can respond accordingly.
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


}
