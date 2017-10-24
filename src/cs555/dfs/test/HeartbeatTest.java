package cs555.dfs.test;

import cs555.dfs.messages.Event;
import cs555.dfs.node.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;

public class HeartbeatTest implements Node{

    private static int testPort = 1000;
    private static String testHost = "testHost";
    private String metadataFilepath = "test/metadata/metadata";


    private void testHeartbeats() {
    }

    public static void main(String[] args) throws IOException {
        HeartbeatTest heartbeatTest = new HeartbeatTest();
        heartbeatTest.getAllChunks();
    }

    private void getAllChunks() throws IOException {
        StringBuilder allChunksBuilder = new StringBuilder();
        File metadataFile = new File(metadataFilepath);

        BufferedReader reader = new BufferedReader(new FileReader(metadataFile));

        String line;
        while ((line = reader.readLine()) != null) {
            allChunksBuilder.append(line).append(",");
        }

        System.out.println(allChunksBuilder.toString());
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {

    }

    @Override
    public void processText(String text) throws IOException {

    }

}
