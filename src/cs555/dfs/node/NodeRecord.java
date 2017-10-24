package cs555.dfs.node;

import cs555.dfs.util.ChunkMetadata;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class NodeRecord {
    private String host;
    private String identifier;
    private int port;
    private long usableSpace;
    private Socket nodeSocket;
    private int numChunks;
    private HashMap<String, ChunkMetadata> chunkInfo = new HashMap<>(); //chunkname, data

    public NodeRecord(String hostPort, Socket nodeSocket, long usableSpace) throws IOException {
        this.host = hostPort.split(":")[0];
        this.port = Integer.parseInt(hostPort.split(":")[1]);
        this.identifier = hostPort;
        this.nodeSocket = nodeSocket;
        this.usableSpace = usableSpace;
    }

    public String getHost() {
        return host;
    }

    public int getPort() { return port; }

    public Socket getNodeSocket() { return nodeSocket; }

    public void setNodeSocket(Socket nodeSocket) {
        this.nodeSocket = nodeSocket;
    }

    public long getUsableSpace() { return usableSpace; }
    public void setUsableSpace(long usableSpace) { this.usableSpace = usableSpace; }

    public int getNumChunks() { return numChunks; }
    public void setNumChunks(int numChunks) { this.numChunks = numChunks; }

    public HashMap<String, ChunkMetadata> getChunkInfo() { return chunkInfo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeRecord that = (NodeRecord) o;

        return identifier == that.identifier;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() { return this.getHost() + ":" + this.getPort(); }
}
