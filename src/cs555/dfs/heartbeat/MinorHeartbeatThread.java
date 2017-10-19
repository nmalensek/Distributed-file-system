package cs555.dfs.heartbeat;

public class MinorHeartbeatThread extends HeartbeatThread implements Runnable{

    private int heartbeatInterval;

    public MinorHeartbeatThread(String controllerHost, int controllerPort, int heartbeatInterval) {
        super(controllerHost, controllerPort, heartbeatInterval);
        this.heartbeatInterval = heartbeatInterval;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(heartbeatInterval);
                sendMinorHeartbeat();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
