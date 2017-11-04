package cs555.dfs.node;

import cs555.dfs.messages.*;
import cs555.dfs.processing.clientprocessing.ClientChunkProcessor;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.transport.TCPServerThread;
import cs555.dfs.util.Splitter;
import cs555.dfs.util.TextInputThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.LinkedList;
import java.util.TreeMap;

import static cs555.dfs.util.Constants.retrievalDirectory;

public class Client implements Node {

    private static int controllerPort;
    private static String controllerHost;
    private int thisNodePort;
    private static String thisNodeHost;
    private String thisNodeID;
    private TCPServerThread clientServer;
    private TCPSender clientSender = new TCPSender();
    private Socket controllerNodeSocket = new Socket(controllerHost, controllerPort);
    private File file;
    private String filenameToRead;
    private LinkedList<byte[]> chunkList = new LinkedList<>();
    private ClientChunkProcessor chunkProcessor = new ClientChunkProcessor(chunkList, controllerNodeSocket);
    private final TreeMap<String, byte[]> receivedChunks = new TreeMap<>();
    private int totalChunks;
    private int chunkNumber = 1;
    private boolean controllerDown = false;

    public Client() throws IOException { }

    private void startup() {
        clientServer = new TCPServerThread(this, 0);
        clientServer.start();
        setPort();
        TextInputThread textInputThread = new TextInputThread(this);
        textInputThread.start();
    }

    private void setPort() {
        while (true) {
            try {
                thisNodePort = clientServer.getPortNumber();
                if (thisNodePort != 0) {
                    thisNodeID = thisNodeHost + ":" + thisNodePort;
                    break;
                }
            } catch (NullPointerException ignored) {

            }
        }
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof NodeInformation) {
            if (((NodeInformation) event).getInformationType() == Protocol.CHUNK_DESTINATION) {
                sendChunk((NodeInformation) event);
            } else if (((NodeInformation) event).getInformationType() == Protocol.CHUNK_LOCATION) {
                requestChunks((NodeInformation) event);
            }
        } else if (event instanceof Chunk) {
            synchronized (receivedChunks) {
                receivedChunks.put(((Chunk) event).getFileName(), ((Chunk) event).getChunkByteArray());
                System.out.println(receivedChunks.size() + " chunks of " + ((Chunk) event).getFileName());
                if (receivedChunks.keySet().size() == totalChunks) {
                    mergeChunks();
                    receivedChunks.clear();
                }
            }
        }
    }

    private void sendChunk(NodeInformation information) throws IOException {
        chunkProcessor.sendChunk(information, chunkNumber, file.getName(), this);
        if (!chunkList.isEmpty()) {
                System.out.println("Asking for destinations for chunk " + chunkNumber);
                WriteFileInquiry writeFileInquiry = new WriteFileInquiry();
                writeFileInquiry.setClientAddress(thisNodeID);
                clientSender.send(controllerNodeSocket, writeFileInquiry.getBytes());
        } else {
            System.out.println("Done sending chunks");
            chunkNumber = 1; //sent all the file's chunks, reset chunk counter
        }
    }

    private void requestChunks(NodeInformation information) throws IOException {
        String[] numChunksPlusChunks = information.getNodeInfo().split("#!#");
        totalChunks = Integer.parseInt(numChunksPlusChunks[0]);
        System.out.println(totalChunks);
        String[] chunkNamePlusLocation = numChunksPlusChunks[1].split(",,");
        System.out.println(information.getNodeInfo());

        for (String nameAndLocation : chunkNamePlusLocation) {
            String chunkName = nameAndLocation.split(":-:")[0];
            String hostPort = nameAndLocation.split(":-:")[1];

            ReadFileInquiry requestChunk = new ReadFileInquiry();
            requestChunk.setClientAddress(thisNodeID);
            requestChunk.setFilename(chunkName);

            try {
                Socket chunkServerSocket = new Socket(Splitter.getHost(hostPort), Splitter.getPort(hostPort));
                clientSender.send(chunkServerSocket, requestChunk.getBytes());
            } catch (IOException e) {
                ChunkServerDown chunkServerDown = new ChunkServerDown();
                chunkServerDown.setNodeInfo(hostPort);
                clientSender.send(controllerNodeSocket, chunkServerDown.getBytes());
                System.out.println("Unable to request chunks from " + hostPort + ", controller has been notified.");
                System.out.println("Request aborted, please re-submit.");
                synchronized (receivedChunks) {
                    receivedChunks.clear();
                }
                break;
            }
        }
    }

    private void mergeChunks() throws IOException {
        File dir = new File(retrievalDirectory);
        dir.mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(retrievalDirectory + filenameToRead);
        for (String chunkName : receivedChunks.keySet()) {
            fileOutputStream.write(receivedChunks.get(chunkName));
        }
    }

    @Override
    public void processText(String text) {
        String command = text.split("\\s")[0];
        switch (command) {
            case "read":
                try {
                    if (controllerDown) { reconnectToController(); }
//                    filenameToRead = text.split("\\s")[1];
                    filenameToRead = "animals_of_the_past_merged.txt";
                    ReadFileInquiry readFileInquiry = new ReadFileInquiry();
                    readFileInquiry.setClientAddress(thisNodeID);
//                    readFileInquiry.setFilename(text.split("\\s")[1]);
                    readFileInquiry.setFilename("animals_of_the_past.txt");
                    clientSender.send(controllerNodeSocket, readFileInquiry.getBytes());
                } catch (StringIndexOutOfBoundsException e) {
                    System.out.println("Usage: read [file name]");
                } catch (IOException ioe) {
                    handleControllerFailure("read");
                }
                break;
            case "write":
                try {
                    if (controllerDown) { reconnectToController(); }
//                    file = new File(text.split("\\s")[1]);
                    file = new File("/s/bach/m/under/nmalensk/555/hw4/animals_of_the_past.txt");
//                    file = new File("/Users/nicholas/Documents/School/CS555/HW4/test/animals_of_the_past.txt");
                    chunkProcessor.chunkFile(file);
                    WriteFileInquiry writeFileInquiry = new WriteFileInquiry();
                    writeFileInquiry.setClientAddress(thisNodeID);
                    clientSender.send(controllerNodeSocket, writeFileInquiry.getBytes());
                    System.out.println("Asking for destination for chunk " + chunkNumber);
                } catch (StringIndexOutOfBoundsException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Usage: write [filePath]");
                } catch (IOException ioe) {
                    handleControllerFailure("write");
                }
                break;
        }
    }

    private void handleControllerFailure(String action) {
        System.out.println("Couldn't contact Controller, " + action + " aborted");
        chunkList.clear();
        try {
            controllerNodeSocket.close();
            controllerDown = true;
        } catch (IOException ignored) {
        }
    }

    private void reconnectToController() {
        if (controllerNodeSocket.isClosed()) {
            try {
                controllerNodeSocket = new Socket(controllerHost, controllerPort);
                controllerDown = false;
            } catch (IOException e) {
                System.out.println("Controller is still down on reconnect attempt.");
            }
        }
    }

    public void setChunkNumber(int chunkNumber) { this.chunkNumber = chunkNumber; }

    public static void main(String[] args) {
        try {
            controllerHost = args[0];
            controllerPort = Integer.parseInt(args[1]);
            thisNodeHost = Inet4Address.getLocalHost().getHostName();

            Client client = new Client();
            client.startup();
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Usage: [controller host] [controller port]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
