package cs555.dfs.node;

import cs555.dfs.heartbeat.ChunkServerHeartbeatThread;
import cs555.dfs.messages.*;
import cs555.dfs.processing.chunkserverprocessing.GetCleanSlice;
import cs555.dfs.processing.chunkserverprocessing.RetrieveChunk;
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
import static cs555.dfs.util.Constants.storageSpaceDirectory;

public class ChunkServer implements Node {

    private static int controllerPort;
    private static String controllerHost;
    private int thisNodePort;
    private static String thisNodeHost;
    private String thisNodeID;
    private long freeSpace;
    private TCPServerThread serverThread;
    private TCPSender sender = new TCPSender();
    private ConcurrentHashMap<String, ChunkMetadata> chunksResponsibleFor = new ConcurrentHashMap<>(); //chunkname, metadata
    private List<String> newChunksResponsibleFor = new ArrayList<>(); //metadata.toString()
    private Socket controllerNodeSocket = new Socket(controllerHost, controllerPort);
    private ProcessChunk chunkProcessor = new ProcessChunk(this, storageDirectory);
    private ChunkServerHeartbeatThread chunkServerHeartbeatThread =
            new ChunkServerHeartbeatThread(controllerNodeSocket, this, metadataFilepath);
    private RetrieveChunk retrieveChunk = new RetrieveChunk();

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
                    thisNodeID = thisNodeHost + ":" + thisNodePort;
                    break;
                }
            } catch (NullPointerException ignored) {

            }
        }
    }

    private void updateUsableSpace() {
        File tmpDirectory = new File(storageSpaceDirectory);
        freeSpace = tmpDirectory.getUsableSpace();
    }

    private void startHeartbeats() throws IOException {
        chunkServerHeartbeatThread.start();
        //register with controller
        NodeInformation nodeInformation = new NodeInformation();
        nodeInformation.setNodeInfo(thisNodeID);
        nodeInformation.setUsableSpace(freeSpace);
        sender.send(controllerNodeSocket, nodeInformation.getBytes());
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof NodeInformation) {
            if (((NodeInformation) event).getInformationType() == Protocol.CHUNK_LOCATION) {
                retrieveChunk.askForCleanChunk((NodeInformation) event, thisNodeID);
            } else {
                System.out.println("got a registration reply");
            }
        } else if (event instanceof RequestMajorHeartbeat) {
            chunkServerHeartbeatThread.sendMajorHeartbeat();
        } else if (event instanceof Chunk) {
            chunkProcessor.writeAndLogChunk((Chunk) event);
            updateUsableSpace();
        } else if (event instanceof ReadFileInquiry) {
            System.out.println("got a request for chunk: " + ((ReadFileInquiry) event).getFilename());
            retrieveChunk.retrieveChunk(((ReadFileInquiry) event), this);
        } else if (event instanceof RequestChunk) {
            GetCleanSlice getCleanSlice = new GetCleanSlice((RequestChunk) event);
            getCleanSlice.retrieveCleanSlice();
        } else if (event instanceof CleanSlices) {
            retrieveChunk.writeSlices((CleanSlices) event);
        }

    }

    public String getNodeID() { return thisNodeID; }

    public ConcurrentHashMap<String, ChunkMetadata> getChunksResponsibleFor() { return chunksResponsibleFor; }

    public List<String> getNewChunksResponsibleFor() { return newChunksResponsibleFor; }

    public long getFreeSpace() { return freeSpace; }

    public Socket getControllerNodeSocket() { return controllerNodeSocket; }

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
