package cs555.dfs.node;

import cs555.dfs.messages.*;
import cs555.dfs.processing.clientprocessing.ClientChunkProcessor;
import cs555.dfs.processing.clientprocessing.ClientChunkReceiver;
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
import java.util.*;

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
    private ClientChunkReceiver chunkReceiver;
    private TreeMap<String, List<String>> chunkLocationMap = new TreeMap<>();
    private int sentChunkNumber = 1;
    private int receivedChunkNumber = 0;
    private boolean controllerDown = false;

    public Client() throws IOException {
    }

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
                    chunkReceiver = new ClientChunkReceiver(clientSender, thisNodeID, controllerNodeSocket);
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
                chunkReceiver.processChunkLocations((NodeInformation) event, chunkLocationMap);
            }
        } else if (event instanceof Chunk) {
            chunkReceiver.handleIncomingChunk((Chunk) event, filenameToRead, chunkLocationMap);
        }
    }

    /**
     * Sends a byte array and metadata information for the next chunk in the chunkList
     * to the node specified in the information parameter. If the chunkList is not empty,
     * the destination for the next chunk in the queue is requested. If all chunks have
     * been sent, the sentChunkNumber is reset to 1 so future chunk counts are accurate.
     * @param information NodeInformation message containing the address the chunk should be sent to.
     * @throws IOException
     */
    private void sendChunk(NodeInformation information) throws IOException {
        chunkProcessor.sendChunk(information, sentChunkNumber, file.getName(), this);
        if (!chunkList.isEmpty()) {
            System.out.println("Asking for destinations for chunk " + sentChunkNumber);
            WriteFileInquiry writeFileInquiry = new WriteFileInquiry();
            writeFileInquiry.setClientAddress(thisNodeID);
            clientSender.send(controllerNodeSocket, writeFileInquiry.getBytes());
        } else {
            System.out.println("Done sending chunks");
            sentChunkNumber = 1; //sent all the file's chunks, reset chunk counter
        }
    }

    private void deleteFileIfExists() {
        File mergedFile = new File(retrievalDirectory + filenameToRead);
        if (mergedFile.exists()) { mergedFile.delete(); }
    }

    @Override
    public void processText(String text) {
        String command = text.split("\\s")[0];
        switch (command) {
            case "read":
                try {
                    if (controllerDown) {
                        reconnectToController();
                    }
//                    filenameToRead = text.split("\\s")[1] + "_merged";
                    filenameToRead = "animals_of_the_past_merged.txt";
                    deleteFileIfExists();
                    ReadFileInquiry readFileInquiry = new ReadFileInquiry();
                    readFileInquiry.setClientAddress(thisNodeID);
//                    readFileInquiry.setFilename(text.split("\\s")[1]);
                    readFileInquiry.setFilename("animals_of_the_past.txt");
                    clientSender.send(controllerNodeSocket, readFileInquiry.getBytes());
                } catch (StringIndexOutOfBoundsException e) {
                    System.out.println("Usage: read [file name]");
                } catch (FileNotFoundException fnfe) {
                    System.out.println("Could not find specified file, please re-enter.");
                } catch (IOException ioe) {
                    handleControllerFailure("read");
                }
                break;
            case "write":
                try {
                    if (controllerDown) {
                        reconnectToController();
                    }
//                    file = new File(text.split("\\s")[1]);
//                    file = new File("/s/bach/m/under/nmalensk/555/hw4/animals_of_the_past.txt");
                    file = new File("/Users/nicholas/Documents/School/CS555/HW4/test/animals_of_the_past.txt");
                    chunkProcessor.chunkFile(file);
                    WriteFileInquiry writeFileInquiry = new WriteFileInquiry();
                    writeFileInquiry.setClientAddress(thisNodeID);
                    clientSender.send(controllerNodeSocket, writeFileInquiry.getBytes());
                    System.out.println("Asking for destination for chunk " + sentChunkNumber);
                } catch (StringIndexOutOfBoundsException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Usage: write [filePath]");
                } catch (FileNotFoundException fnfe) {
                    System.out.println("Could not find specified file, please re-enter.");
                } catch (IOException ioe) {
                    handleControllerFailure("write");
                }
                break;
            default:
                System.out.println("Commands are \"write\" or \"read\" [filename]");
        }
    }

    private void handleControllerFailure(String action) {
        System.out.println("Couldn't contact Controller, " + action + " aborted");
        chunkList.clear();
        try {
            controllerNodeSocket.close();
            controllerNodeSocket = null;
            controllerDown = true;
        } catch (IOException ignored) {
        }
    }

    private void reconnectToController() {
        if (controllerNodeSocket == null) {
            try {
                controllerNodeSocket = new Socket(controllerHost, controllerPort);
                controllerDown = false;
            } catch (IOException e) {
                System.out.println("Controller is still down on reconnect attempt.");
            }
        }
    }

    public void setSentChunkNumber(int sentChunkNumber) {
        this.sentChunkNumber = sentChunkNumber;
    }

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
