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
    private int thisNodePort;
    private TCPServerThread clientServer;
    private TCPSender clientSender;
    private Socket controllerNodeSocket = new Socket(controllerHost, controllerPort);

    public Client() throws IOException {

    }

    private void startup() {
        clientServer = new TCPServerThread(this, 0);
        clientServer.start();
        setPort();
    }

    private void setPort() {
        while (true) {
            try {
                thisNodePort = clientServer.getPortNumber();
                if (thisNodePort != 0) {
                    break;
                }
            } catch (NullPointerException ignored) {

            }
        }
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {

    }

    @Override
    public void processText(String text) throws IOException {
        String command = text.split("\\s")[0];
        switch (command) {
            case "read":
                break;
            case "write":
                break;
        }
    }

    public static void main(String[] args) {
        try {
            controllerHost = args[0];
            controllerPort = Integer.parseInt(args[1]);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Usage: [controller host] [controller port]");
        }
    }
}
