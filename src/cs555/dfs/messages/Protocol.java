package cs555.dfs.messages;

public interface Protocol {
    int NODE_INFORMATION = 0;
    int CHUNK_DESTINATION = 1;
    int CHUNK_LOCATION = 2;
    int CHUNK_REQUEST = 3;
    int CLEAN_SLICES = 4;
    int FILE = 6;
    int UPDATE = 9;
    int MINOR_HEARTBEAT = 10;
    int MAJOR_HEARTBEAT = 11;
    int READ_INQUIRY = 12;
    int WRITE_INQUIRY = 13;
    int CHUNK = 14;
    int REQUEST_MAJOR_HEARTBEAT = 99;
    int DISCONNECT = 100;
}
