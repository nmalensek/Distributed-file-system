package cs555.dfs.processing.clientprocessing;

import cs555.dfs.messages.Chunk;
import cs555.dfs.messages.ChunkServerDown;
import cs555.dfs.messages.Disconnect;
import cs555.dfs.messages.NodeInformation;
import cs555.dfs.node.Client;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.util.Splitter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

import static cs555.dfs.util.Constants.chunkSize;

public class ClientChunkProcessor {

    private LinkedList<byte[]> chunkList;
    private TCPSender chunkSender = new TCPSender();
    private Socket controllerSocket;

    public ClientChunkProcessor(LinkedList<byte[]> chunkList, Socket controllerSocket) {
        this.chunkList = chunkList;
        this.controllerSocket = controllerSocket;
    }

    /**
     * Breaks file into chunks equal to the chunkSize constant and adds the chunks to
     * the client's list of chunks that are pending transmission for easy access by the
     * sendChunk method.
     * @param fileToChunk File that should be chunked.
     * @throws IOException
     */
    public void chunkFile(File fileToChunk) throws IOException {

        byte[] chunk = new byte[chunkSize];

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int read;
        try (FileInputStream fileInputStream = new FileInputStream(fileToChunk)) {
            while ((read = fileInputStream.read(chunk)) != -1) {
                byteArrayOutputStream.write(chunk, 0, read);
                chunkList.add(byteArrayOutputStream.toByteArray());
                byteArrayOutputStream.reset();
            }
            System.out.println(chunkList.size());
        }
    }

    /**
     * Sends file chunk to each of the Chunk Servers specified by the Controller Node.
     * @param destination The addresses of all Chunk Servers the chunk should be stored on.
     * @throws IOException
     */
    public void sendChunk(NodeInformation destination, int chunkNumber, String filename, Client client) throws IOException {
        String[] destinationNodes = destination.getNodeInfo().split(",");
        Chunk chunk = new Chunk();
        StringBuilder nextNodes = new StringBuilder();
        for (int i = 1; i < destinationNodes.length; i++) {
            nextNodes.append(destinationNodes[i]).append(",");
        }
        chunk.setReplicationNodes(nextNodes.toString());
        chunk.setFileName(filename + "_chunk-" + chunkNumber);
        chunk.setChunkByteArray(chunkList.remove());

        try {
            Socket destinationSocket = new Socket(Splitter.getHost(destinationNodes[0]), Splitter.getPort(destinationNodes[0]));
            chunkSender.send(destinationSocket, chunk.getBytes());
            System.out.println("Sent " + chunk.getFileName() + " to " + destinationNodes[0]);
            client.setSentChunkNumber(chunkNumber + 1);
            Disconnect dc = new Disconnect();
            chunkSender.send(destinationSocket, dc.getBytes());
        } catch (IOException ioe) {
            ChunkServerDown serverDown = new ChunkServerDown();
            serverDown.setNodeInfo(destinationNodes[0]);
            chunkSender.send(controllerSocket, serverDown.getBytes());
            System.out.println("Write to " + destinationNodes[0] + " failed, Controller Node has been informed.");
        }
    }
}
