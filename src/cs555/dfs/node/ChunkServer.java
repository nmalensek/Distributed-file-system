package cs555.dfs.node;

import cs555.dfs.messages.Event;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class ChunkServer implements Node {

    private static int controllerPort;
    private static String controllerHost;
    private static int sliceSize = 8192; //8kb slices
    private static long freeSpace;
    public static String storageDirectory = "/tmp/";

    public ChunkServer() {

        getUsableSpace();
    }

    private void getUsableSpace() {
        File tmpDirectory = new File(storageDirectory);
        freeSpace = tmpDirectory.getUsableSpace();
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {

    }

    @Override
    public void processText(String text) throws IOException {

    }

    public static void main(String[] args) {
        controllerHost = args[0];
        controllerPort = Integer.parseInt(args[1]);

    }
}
