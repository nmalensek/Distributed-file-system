package cs555.dfs.test;

import cs555.dfs.messages.Event;
import cs555.dfs.messages.NodeInformation;
import cs555.dfs.node.Node;
import cs555.dfs.transport.TCPSender;
import cs555.dfs.transport.TCPServerThread;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionTest implements Node {

    private Socket testSocket = new Socket("127.0.0.1", 50000);
    private TCPSender sender = new TCPSender();

    public ConnectionTest() throws IOException {
        TCPServerThread serverThread = new TCPServerThread(this, 51000);
    }

    private void test() throws InterruptedException, IOException {
        while (true) {
            try{
                Thread.sleep(500);
                NodeInformation nodeInformation = new NodeInformation();
                nodeInformation.setNodeInfo("127.0.0.1:51000");
                nodeInformation.setUsableSpace(243234234234L);
                System.out.println("sent a message");
                sender.send(testSocket, nodeInformation.getBytes());
            } catch (IOException | InterruptedException e) {
                System.out.println("Could not connect..");
                testSocket.close();
                tryToReconnect();
            }
        }
    }

    private void tryToReconnect() {
        try {
            System.out.println("Trying to reconnect...");
            testSocket = new Socket("127.0.0.1", 50000);
//            test();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Reconnect failed, server's not back up. Retrying...");
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        ConnectionTest connectionTest = new ConnectionTest();
        connectionTest.test();
    }


    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {

    }

    @Override
    public void processText(String text) throws IOException {

    }

}
