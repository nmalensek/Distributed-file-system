package cs555.dfs.node;

import cs555.dfs.heartbeat.ChunkServerHeartbeatThread;
import cs555.dfs.messages.Event;
import cs555.dfs.messages.NodeInformation;
import cs555.dfs.messages.RequestMajorHeartbeat;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.transport.TCPServerThread;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkServer implements Node {

    private static int controllerPort;
    private static String controllerHost;
    private int thisNodePort;
    private static String thisNodeHost;
    private static int sliceSize = 8192; //8kb slices
    private long freeSpace;
    public static String storageDirectory = "/tmp/";
    private TCPServerThread serverThread;
    private TCPSender sender = new TCPSender();
    private ConcurrentHashMap<String, String> chunksResponsibleFor = new ConcurrentHashMap<>(); //chunkname, chunkname
    private List<String> newChunksResponsibleFor = new ArrayList<>();
    private Socket controllerNodeSocket = new Socket(controllerHost, controllerPort);
    ChunkServerHeartbeatThread chunkServerHeartbeatThread;

    public ChunkServer() throws IOException {

    }

    private void startup() {
        getUsableSpace();
        serverThread = new TCPServerThread(this, 0);
        serverThread.start();
        setPort();
        try {
            startHeartbeats();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void getUsableSpace() { //TODO: call this after every chunk storage
        File tmpDirectory = new File(storageDirectory);
        freeSpace = tmpDirectory.getUsableSpace();
    }

    private void startHeartbeats() throws IOException {
        chunkServerHeartbeatThread = new ChunkServerHeartbeatThread(controllerNodeSocket, this);
        chunkServerHeartbeatThread.start();
        //register with controller
        NodeInformation nodeInformation = new NodeInformation();
        nodeInformation.setNodeInfo(thisNodeHost + ":" + thisNodePort);
        nodeInformation.setUsableSpace(freeSpace);
        sender.send(controllerNodeSocket, nodeInformation.getBytes());
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof NodeInformation) {
            System.out.println("got a registration reply");
        } else if (event instanceof RequestMajorHeartbeat) {
            chunkServerHeartbeatThread.sendMajorHeartbeat();
        }

    }

    public String getNodeID() { return thisNodeHost + ":" + thisNodePort; }

    public ConcurrentHashMap<String, String> getChunksResponsibleFor() { return chunksResponsibleFor; }

    public List<String> getNewChunksResponsibleFor() { return newChunksResponsibleFor; }

    public long getFreeSpace() { return freeSpace; }

    public static void main(String[] args) {
        try {
            controllerHost = args[0];
            controllerPort = Integer.parseInt(args[1]);
            thisNodeHost = Inet4Address.getLocalHost().getHostName();

            ChunkServer chunkServer = new ChunkServer();
            chunkServer.startup();

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Usage [controller host] [controller port]");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void processText(String text) throws IOException { }
}
