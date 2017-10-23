package cs555.dfs.heartbeat;

import cs555.dfs.node.ControllerNode;
import cs555.dfs.transport.TCPSender;

public class ServerHeartbeatThread extends Thread {

    private String controllerHost;
    private int controllerPort;
    private int heartbeatInterval;
    private ControllerNode owner;
    private TCPSender controllerMessager = new TCPSender();

    public ServerHeartbeatThread(String targetHost, int targetPort, int heartbeatInterval, ControllerNode owner) {
        this.controllerHost = targetHost;
        this.controllerPort = targetPort;
        this.heartbeatInterval = heartbeatInterval;
        this.owner = owner;
    }

    public void sendServerHeartbeat() {

    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(heartbeatInterval);
                sendServerHeartbeat();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}