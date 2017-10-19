package cs555.dfs.node;

import cs555.dfs.messages.Event;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;

public class ControllerNode implements Node {

    private static int controllerPort;
    private static String controllerHost;

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {

    }

    @Override
    public void processText(String text) throws IOException {

    }

    public static void main(String[] args) throws UnknownHostException {
        controllerHost = Inet4Address.getLocalHost().getHostName();
        controllerPort = Integer.parseInt(args[0]);

    }
}
