package cs555.dfs.node;

import cs555.dfs.heartbeat.ChunkServerHeartbeatThread;
import cs555.dfs.messages.Chunk;
import cs555.dfs.messages.Event;
import cs555.dfs.messages.NodeInformation;
import cs555.dfs.messages.RequestMajorHeartbeat;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.transport.TCPServerThread;
import cs555.dfs.util.ChunkMetadata;
import cs555.dfs.processing.chunkserverprocessing.ProcessChunk;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static cs555.dfs.util.Constants.metadataFilepath;
import static cs555.dfs.util.Constants.storageDirectory;

public class ChunkServer implements Node {

    private static int controllerPort;
    private static String controllerHost;
    private int thisNodePort;
    private static String thisNodeHost;
    private long freeSpace;
    private TCPServerThread serverThread;
    private TCPSender sender = new TCPSender();
    private ConcurrentHashMap<String, ChunkMetadata> chunksResponsibleFor = new ConcurrentHashMap<>(); //chunkname, metadata
    private List<String> newChunksResponsibleFor = new ArrayList<>(); //metadata.toString()
    private Socket controllerNodeSocket = new Socket(controllerHost, controllerPort);
    private ProcessChunk chunkProcessor = new ProcessChunk(this, storageDirectory);
    private ChunkServerHeartbeatThread chunkServerHeartbeatThread =
            new ChunkServerHeartbeatThread(controllerNodeSocket, this, metadataFilepath);

    public ChunkServer() throws IOException {

    }

    private void startup() {
        updateUsableSpace();
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

    private void updateUsableSpace() {
        File tmpDirectory = new File(storageDirectory);
        freeSpace = tmpDirectory.getUsableSpace();
    }

    private void startHeartbeats() throws IOException {
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
        } else if (event instanceof Chunk) {
            chunkProcessor.writeAndLogChunk((Chunk) event);
            updateUsableSpace();
        }

    }

    public String getNodeID() { return thisNodeHost + ":" + thisNodePort; }

    public ConcurrentHashMap<String, ChunkMetadata> getChunksResponsibleFor() { return chunksResponsibleFor; }

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
