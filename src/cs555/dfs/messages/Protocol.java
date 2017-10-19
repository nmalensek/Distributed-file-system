package cs555.dfs.messages;

public interface Protocol {
    int NODE_INFORMATION = 0;
    int FILE = 6;
    int UPDATE = 9;
    int MINOR_HEARTBEAT = 10;
    int MAJOR_HEARTBEAT = 11;
    int READ = 12;
    int WRITE = 13;
}
