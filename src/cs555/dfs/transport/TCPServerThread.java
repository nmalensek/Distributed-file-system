package cs555.dfs.transport;


import cs555.dfs.node.Node;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * code adapted from code provided by instructor at http://www.cs.colostate.edu/~cs455/lectures/CS455-HelpSession1.pdf
 */

public class TCPServerThread extends Thread {
    private Node node;
    private int portNum;
    private ServerSocket serverSocket;

    public TCPServerThread(Node node, int portNum) {
        this.node = node;
        this.portNum = portNum;
    }

    public int getPortNumber() { return serverSocket.getLocalPort(); }

    public void run() {
        try {
            serverSocket = new ServerSocket(portNum);
            System.out.println("Server running on port " + serverSocket.getLocalPort() + "...");

            while(true) {
                new TCPReceiverThread(serverSocket.accept(), node).start();
//                System.out.println("A new client connected");
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
