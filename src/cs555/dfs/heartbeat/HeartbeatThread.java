package cs555.dfs.heartbeat;

import cs555.dfs.transport.TCPSender;

public class HeartbeatThread {

    private String controllerHost;
    private int controllerPort;
    private int heartbeatInterval;
    private TCPSender controllerMessager = new TCPSender();

    public HeartbeatThread(String controllerHost, int controllerPort, int heartbeatInterval) {
        this.controllerHost = controllerHost;
        this.controllerPort = controllerPort;
        this.heartbeatInterval = heartbeatInterval;
    }

    public void sendMinorHeartbeat() {
        System.out.println("sending minor heartbeat to " + controllerHost + ":" + controllerPort);
    }
    public void sendMajorHeartbeat() {
        System.out.println("sending MAJOR heartbeat to " + controllerHost + ":" + controllerPort);
    }
}
