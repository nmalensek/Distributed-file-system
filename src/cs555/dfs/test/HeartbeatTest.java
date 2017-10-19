package cs555.dfs.test;

import cs555.dfs.heartbeat.MajorHeartbeatThread;
import cs555.dfs.heartbeat.MinorHeartbeatThread;

public class HeartbeatTest {

    private static int testPort = 1000;
    private static String testHost = "testHost";


    private void testHeartbeats() {
        new Thread(new MinorHeartbeatThread(testHost, testPort, 200)).start();
        new Thread(new MajorHeartbeatThread(testHost, testPort, 350)).start();
    }

    public static void main(String[] args) {
        HeartbeatTest heartbeatTest = new HeartbeatTest();
        heartbeatTest.testHeartbeats();
    }
}
