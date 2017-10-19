package cs555.dfs.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * code adapted from code provided by instructor at http://www.cs.colostate.edu/~cs455/lectures/CS455-HelpSession1.pdf
 */

public class TCPSender {

    public synchronized void send(Socket socket, byte[] data) throws IOException {
            int dataLength = data.length;
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(dataLength);
            outputStream.write(data, 0, dataLength);
            outputStream.flush();
    }
}
