package cs555.dfs.util;

public class Splitter {

    public static String getHost(String hostPort) {
        return hostPort.split(":")[0];
    }

    public static int getPort(String hostPort) {
        return Integer.parseInt(hostPort.split(":")[1]);
    }

    public static int getID(String hostPortID) {
        return Integer.parseInt(hostPortID.split(":")[2]);
    }

    public static String getHostPort(String hostPortID) { return hostPortID.split(":")[0] + ":" + hostPortID.split(":")[1]; }
}
