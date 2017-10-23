package cs555.dfs.heartbeat;

import cs555.dfs.node.ChunkServer;

public class MinorHeartbeatThread extends Thread {

    private int heartbeatInterval;
    private int majorHeartbeatInterval = 300000;
    private int heartbeatCount = 0;

    public MinorHeartbeatThread(String controllerHost, int controllerPort, int heartbeatInterval, ChunkServer owner) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public void sendMinorHeartbeat() {

    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(heartbeatInterval);
                if (heartbeatCount == majorHeartbeatInterval) {
                    //do nothing, major heartbeat is being sent
                    heartbeatCount = 0;
                } else {
                    heartbeatCount += heartbeatInterval;
                    sendMinorHeartbeat();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
