package cs555.dfs.heartbeat;

import cs555.dfs.node.Node;

public class MajorHeartbeatThread extends HeartbeatThread implements Runnable{

    private int heartbeatInterval;

    public MajorHeartbeatThread(String controllerHost, int controllerPort, int heartbeatInterval, Node owner) {
        super(controllerHost, controllerPort, heartbeatInterval, owner);
        this.heartbeatInterval = heartbeatInterval;
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
