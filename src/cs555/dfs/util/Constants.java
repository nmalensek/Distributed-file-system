package cs555.dfs.util;

public class Constants {

    public static final int sliceSize = 8192;
    public static final int heartbeatInterval = 30000;
    public static final int majorHeartbeatInterval = 300000;
    public static final int chunkSize = 65536;
    public static final String integrity = "_integrity";
    public static final String storageSpaceDirectory = "/tmp";
    public static final String storageDirectory = "/tmp/nmalensk_chunks/";
    public static final String metadataFilepath = storageDirectory + "/metadata";
}
