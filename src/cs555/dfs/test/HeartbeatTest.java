package cs555.dfs.test;

import cs555.dfs.heartbeat.MajorHeartbeatThread;
import cs555.dfs.heartbeat.MinorHeartbeatThread;
import cs555.dfs.messages.Event;
import cs555.dfs.node.Node;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HeartbeatTest implements Node{

    private static int testPort = 1000;
    private static String testHost = "testHost";


    private void testHeartbeats() {
    }

    public static void main(String[] args) {
        HeartbeatTest heartbeatTest = new HeartbeatTest();
        heartbeatTest.testHeartbeats();
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {

    }

    @Override
    public void processText(String text) throws IOException {

    }

}
