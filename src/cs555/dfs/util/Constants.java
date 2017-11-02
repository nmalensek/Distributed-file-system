package cs555.dfs.util;

public class Constants {

    public static final int sliceSize = 8192;
    public static final int minorHeartbeatInterval = 30000;
    public static final int majorHeartbeatInterval = 300000;
    public static final int chunkSize = 65536;
    public static final String integrity = "_integrity";
    public static final String storageSpaceDirectory = "/tmp";
//    public static final String storageSpaceDirectory = "/Users/nicholas/Desktop/tmp";
    public static final String storageDirectory = "/tmp/nmalensk_chunks/";
//    public static final String storageDirectory = "/Users/nicholas/Desktop/tmp/nmalensk_chunks/";
    public static final String metadataFilepath = storageDirectory + "/metadata";
    public static final String metadata = "/metadata";
    public static final String testDirectory = storageDirectory + "/test/";
    public static final String retrievalDirectory = "/tmp/nmalensk_retrieved_files/";
//    public static final String retrievalDirectory = "/Users/nicholas/Desktop/tmp/nmalensk_retrieved_files/";
}
