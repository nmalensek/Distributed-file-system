package cs555.dfs.node;

import cs555.dfs.messages.Event;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface Node {

    void onEvent(Event event, Socket destinationSocket) throws IOException;
    void processText(String text) throws IOException;

}
