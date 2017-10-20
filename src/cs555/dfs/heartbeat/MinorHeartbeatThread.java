package cs555.dfs.heartbeat;

import cs555.dfs.node.Node;

public class MinorHeartbeatThread extends HeartbeatThread implements Runnable{

    private int heartbeatInterval;
    private int majorHeartbeatInterval = 300000;
    private int heartbeatCount = 0;

    public MinorHeartbeatThread(String controllerHost, int controllerPort, int heartbeatInterval, Node owner) {
        super(controllerHost, controllerPort, heartbeatInterval, owner);
        this.heartbeatInterval = heartbeatInterval;
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
