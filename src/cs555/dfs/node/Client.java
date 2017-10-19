package cs555.dfs.node;

import cs555.dfs.messages.Event;
import cs555.dfs.transport.TCPReceiverThread;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.transport.TCPServerThread;

import java.io.IOException;
import java.net.Socket;

public class Client implements Node {

    private static int controllerPort;
    private static String controllerHost;
    private TCPServerThread clientServer;
    private TCPReceiverThread clientReceiver;
    private TCPSender clientSender;

    public Client() {

    }

    private void startup() {

    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {

    }

    @Override
    public void processText(String text) throws IOException {

    }

    public static void main(String[] args) {
        controllerHost = args[0];
        controllerPort = Integer.parseInt(args[1]);
    }
}
