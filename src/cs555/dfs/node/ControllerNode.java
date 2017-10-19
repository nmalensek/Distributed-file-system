package cs555.dfs.node;

import cs555.dfs.messages.Event;
import cs555.dfs.transport.TCPServerThread;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;

public class ControllerNode implements Node {

    private static int controllerPort;
    private static String controllerHost;

    private void startup() {
        TCPServerThread serverThread = new TCPServerThread(this, controllerPort);
        serverThread.start();
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {

    }

    public static void main(String[] args) throws UnknownHostException {
        controllerHost = Inet4Address.getLocalHost().getHostName();
        controllerPort = Integer.parseInt(args[0]);

    }

    @Override
    public void processText(String text) throws IOException { }
}
