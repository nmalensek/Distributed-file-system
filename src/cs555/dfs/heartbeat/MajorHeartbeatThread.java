package cs555.dfs.heartbeat;

public class MajorHeartbeatThread extends HeartbeatThread implements Runnable{

    private int heartbeatInterval;

    public MajorHeartbeatThread(String controllerHost, int controllerPort, int heartbeatInterval) {
        super(controllerHost, controllerPort, heartbeatInterval);
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
