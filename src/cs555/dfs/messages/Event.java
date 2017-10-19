package cs555.dfs.messages;

import java.io.IOException;

public interface Event<T> {

    T getType();
    int getMessageType();
    byte[] getBytes() throws IOException;
}
