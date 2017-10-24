package cs555.dfs.node;

import cs555.dfs.messages.Chunk;
import cs555.dfs.messages.Event;
import cs555.dfs.messages.NodeInformation;
import cs555.dfs.messages.WriteFileInquiry;
import cs555.dfs.transport.TCPReceiverThread;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.transport.TCPServerThread;
import cs555.dfs.util.Splitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;

public class Client implements Node {

    private static int controllerPort;
    private static String controllerHost;
    private static int chunkSize = 65536;
    private int thisNodePort;
    private Splitter split = new Splitter();
    private TCPServerThread clientServer;
    private TCPSender clientSender;
    private Socket controllerNodeSocket = new Socket(controllerHost, controllerPort);
    private Path filePath;
    private File file;
    private LinkedList<byte[]> chunkList = new LinkedList<>();
    private HashMap<String, byte[]> receivedChunks = new HashMap<>();
    private int chunkNumber = 1;

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
        if (event instanceof NodeInformation) {
            sendChunk((NodeInformation) event);
            if (!chunkList.isEmpty()) {
                WriteFileInquiry writeFileInquiry = new WriteFileInquiry();
                clientSender.send(controllerNodeSocket, writeFileInquiry.getBytes());
            }
        }
    }

    @Override
    public void processText(String text) throws IOException {
        String command = text.split("\\s")[0];
        switch (command) {
            case "read":
                break;
            case "write":
                try {
                    filePath = Paths.get(text.split("\\s")[1]);
                    file = new File(text.split("\\s")[1]);

                    chunkFile(file);
                    WriteFileInquiry writeFileInquiry = new WriteFileInquiry();
                    clientSender.send(controllerNodeSocket, writeFileInquiry.getBytes());
                } catch (StringIndexOutOfBoundsException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Usage: write [filePath]");
                }
                break;
        }
    }

    private void chunkFile(File fileToChunk) throws IOException {

        FileInputStream fileInputStream = new FileInputStream(fileToChunk);

        int bytesRemaining = chunkSize;

        try {
            while (true) {
                byte[] chunk = new byte[chunkSize];
                int read = fileInputStream.read(chunk, chunk.length - bytesRemaining, bytesRemaining);
                if (read >= 0) {
                    bytesRemaining -= read;
                    if (bytesRemaining == 0) {
                        chunkList.add(chunk);
                        bytesRemaining = chunkSize;
                    }
                } else {
                    if (bytesRemaining < chunkSize) {
                        chunkList.add(chunk);
                    }
                    break;
                }
            }
        } finally {
            fileInputStream.close();
        }

    }

    private void sendChunk(NodeInformation destination) throws IOException {
        String[] destinationNodes = destination.getNodeInfo().split(",");
        Chunk chunk = new Chunk();
        chunk.setReplicationNodes(destinationNodes[1] + "," + destinationNodes[2]);
        chunk.setFileName(file.getName() + "_chunk" + chunkNumber);
        chunk.setChunkByteArray(chunkList.remove());

        Socket destinationSocket = new Socket(split.getHost(destinationNodes[0]), split.getPort(destinationNodes[0]));
        clientSender.send(destinationSocket, chunk.getBytes());
        chunkNumber++;
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
