package cs555.dfs.messages;

public interface Protocol {
    int NODE_INFORMATION = 0;
    int FILE = 6;
    int UPDATE = 9;
    int MINOR_HEARTBEAT = 10;
    int MAJOR_HEARTBEAT = 11;
    int READ = 12;
    int WRITE = 13;
    int CHUNK = 14;
    int FILE_INQUIRY = 15;
    int REQUEST_MAJOR_HEARTBEAT = 99;
}
