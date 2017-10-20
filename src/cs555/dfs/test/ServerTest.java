package cs555.dfs.test;

import cs555.dfs.messages.Event;
import cs555.dfs.messages.NodeInformation;
import cs555.dfs.node.Node;
import cs555.dfs.node.NodeRecord;
import cs555.dfs.transport.TCPServerThread;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerTest implements Node {

        private Map<String, NodeRecord> nodeMap = new HashMap<>();
        private int port;
        private TCPServerThread testServerThread;


        public ServerTest() {
            startServer();
        }
        private void startServer() {
            testServerThread = new TCPServerThread(this, 50000);
            testServerThread.start();
        }

        @Override
        public void onEvent(Event event, Socket destinationSocket) throws IOException {
            if (event instanceof NodeInformation) {
                System.out.println("got a message");
            }
        }

        @Override
        public void processText(String text) throws IOException {

        }

    @Override
    public List<String> getNewChunks() {
        return null;
    }

    @Override
    public ConcurrentHashMap<String, String> getAllChunks() {
        return null;
    }

    public static void main(String[] args) {
            ServerTest testServer = new ServerTest();
        }
    }
