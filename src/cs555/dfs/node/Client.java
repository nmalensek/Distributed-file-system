package cs555.dfs.node;

import cs555.dfs.messages.Event;
import cs555.dfs.messages.WriteFileInquiry;
import cs555.dfs.transport.TCPReceiverThread;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.transport.TCPServerThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class Client implements Node {

    private static int controllerPort;
    private static String controllerHost;
    private static int chunkSize = 65536;
    private int thisNodePort;
    private TCPServerThread clientServer;
    private TCPSender clientSender;
    private Socket controllerNodeSocket = new Socket(controllerHost, controllerPort);
    private Path filePath;
    private File file;
    private LinkedList<byte[]> chunkList = new LinkedList<>();

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
                try {
                    filePath = Paths.get(text.split("\\s")[1]);
                    file = new File(text.split("\\s")[1]);

                    //chunk file and put into linked list, send inquiry, write to destination A, repeat for all chunks till LL empty
                    WriteFileInquiry writeFileInquiry = new WriteFileInquiry();
                } catch (StringIndexOutOfBoundsException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Usage: write [filePath]");
                }
                break;
        }
    }

    private void chunkFile(String filePath) throws IOException {

        File fileToChunk = new File(filePath);

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

    public static void main(String[] args) {
        try {
            controllerHost = args[0];
            controllerPort = Integer.parseInt(args[1]);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Usage: [controller host] [controller port]");
        }
    }
}
