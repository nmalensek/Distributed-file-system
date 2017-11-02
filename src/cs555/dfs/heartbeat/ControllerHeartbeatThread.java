package cs555.dfs.heartbeat;

import cs555.dfs.messages.Ping;
import cs555.dfs.node.ControllerNode;
import cs555.dfs.transport.TCPSender;

import java.io.IOException;
import java.net.Socket;

import static cs555.dfs.util.Constants.minorHeartbeatInterval;

public class ControllerHeartbeatThread extends Thread {

    private Socket targetSocket;
    private TCPSender heartbeatSender = new TCPSender();
    private String nodeID;

    public ControllerHeartbeatThread(Socket targetSocket, String nodeID) {
        this.targetSocket = targetSocket;
        this.nodeID = nodeID;
    }

    public void sendServerHeartbeat() throws IOException {
        Ping ping = new Ping();
        heartbeatSender.send(targetSocket, ping.getBytes());
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(minorHeartbeatInterval);
                sendServerHeartbeat();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Could not contact chunk server at " + targetSocket.getRemoteSocketAddress());
                //TODO initiate recovery
                e.printStackTrace();
                break;
            }
        }
    }
}