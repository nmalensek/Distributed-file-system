package cs555.dfs.test;

import java.util.ArrayList;
import java.util.Iterator;

public class IteratorTest {

    private ArrayList<String> nodesWithChunks;
    private ArrayList<String> locationList = new ArrayList<>();

    private String destinations = "santa-fe:36547,dover:36303,columbia:36443";

    private void setup() {
        locationList.add("santa-fe:36547");
        locationList.add("dover:36303");
        locationList.add("phoenix:37405");
    }

    private synchronized void removeFromChunksMap() {
        String nodeID = "phoenix:37405";
        nodesWithChunks = new ArrayList<>();
        for (Iterator<String> iterator = locationList.iterator(); iterator.hasNext();) {
            String nodeAddress = iterator.next();
            if (nodeAddress.equals(nodeID)) {
                iterator.remove();
            } else {
                nodesWithChunks.add(nodeAddress);
            }
            System.out.println("node " + nodeAddress);
        }
        System.out.println(locationList.toString());
    }

    private void test() {
        System.out.println(destinations);
        System.out.println(nodesWithChunks.toString());
        String[] destinationAddresses = destinations.split(",");
        String replicationAddress = "";
        for (String address : destinationAddresses) {
            if (!nodesWithChunks.contains(address)) {
                replicationAddress = address;
                break;
            }
        }
        System.out.println("-------------------");
        System.out.println(replicationAddress);
    }

    public static void main(String[] args) {
        IteratorTest test = new IteratorTest();
        test.setup();
        test.removeFromChunksMap();
        test.test();
    }
}
