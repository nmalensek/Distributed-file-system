package cs555.dfs.node;

import cs555.dfs.messages.Event;

import java.io.IOException;
import java.net.Socket;

public interface Node {

    void onEvent(Event event, Socket destinationSocket) throws IOException;
    void processText(String text) throws IOException;
}
