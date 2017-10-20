package cs555.dfs.node;

import cs555.dfs.messages.Event;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.transport.TCPServerThread;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerNode implements Node {

    private static int controllerPort;
    private static String controllerHost;
    private TCPSender controllerSender = new TCPSender();


    private void startup() {
        TCPServerThread serverThread = new TCPServerThread(this, controllerPort);
        serverThread.start();
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {

    }

    public static void main(String[] args) throws UnknownHostException {
        try {
            controllerHost = Inet4Address.getLocalHost().getHostName();
            controllerPort = Integer.parseInt(args[0]);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Usage: [Server port]");
        }

    }

    @Override
    public void processText(String text) throws IOException { }
    @Override
    public List<String> getNewChunks() { return null; }
    @Override
    public ConcurrentHashMap<String, String> getAllChunks() { return null; }
}
