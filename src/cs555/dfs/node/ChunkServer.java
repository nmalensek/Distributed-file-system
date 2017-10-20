package cs555.dfs.node;

import cs555.dfs.messages.Event;
import cs555.dfs.transport.TCPReceiverThread;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.transport.TCPServerThread;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkServer implements Node {

    private static int controllerPort;
    private static String controllerHost;
    private int thisNodePort;
    private static int sliceSize = 8192; //8kb slices
    private static long freeSpace;
    public static String storageDirectory = "/tmp/";
    private TCPServerThread serverThread;
    private TCPSender sender = new TCPSender();
    private ConcurrentHashMap<String, String> chunksResponsibleFor = new ConcurrentHashMap<>();
    private List<String> newChunksResponsibleFor = new ArrayList<>();
    private Socket controllerNodeSocket = new Socket(controllerHost, controllerPort);

    public ChunkServer() throws IOException {

    }

    private void startup() {
        getUsableSpace();
        serverThread = new TCPServerThread(this, 0);
        serverThread.start();
        setPort();
    }

    private void setPort() {
        while (true) {
            try {
                thisNodePort = serverThread.getPortNumber();
                if (thisNodePort != 0) {
                    break;
                }
            } catch (NullPointerException npe) {

            }
        }
    }

    private void getUsableSpace() {
        File tmpDirectory = new File(storageDirectory);
        freeSpace = tmpDirectory.getUsableSpace();
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {

    }

    @Override
    public List<String> getNewChunks() {
        return newChunksResponsibleFor;
    }

    @Override
    public ConcurrentHashMap<String, String> getAllChunks() {
        return chunksResponsibleFor;
    }

    public static void main(String[] args) {
        try {
            controllerHost = args[0];
            controllerPort = Integer.parseInt(args[1]);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Usage [controller host] [controller port]");
        }

    }

    @Override
    public void processText(String text) throws IOException { }
}
