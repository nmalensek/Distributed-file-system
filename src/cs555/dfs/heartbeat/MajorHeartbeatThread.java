package cs555.dfs.heartbeat;

import cs555.dfs.node.ChunkServer;

public class MajorHeartbeatThread extends Thread {

    private int heartbeatInterval;

    public MajorHeartbeatThread(String controllerHost, int controllerPort, int heartbeatInterval, ChunkServer owner) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public void sendMajorHeartbeat() {

    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(heartbeatInterval);
                sendMajorHeartbeat();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
