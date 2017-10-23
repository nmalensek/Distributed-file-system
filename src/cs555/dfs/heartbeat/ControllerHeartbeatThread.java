package cs555.dfs.heartbeat;

import cs555.dfs.node.ControllerNode;
import cs555.dfs.transport.TCPSender;

import java.net.Socket;

public class ControllerHeartbeatThread extends Thread {

    private int heartbeatInterval;
    private Socket targetSocket;
    private ControllerNode owner;
    private TCPSender controllerMessager = new TCPSender();

    public ControllerHeartbeatThread(Socket targetSocket, int heartbeatInterval, ControllerNode owner) {
        this.heartbeatInterval = heartbeatInterval;
        this.owner = owner;
        this.targetSocket = targetSocket;
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