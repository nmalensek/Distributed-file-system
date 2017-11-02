package cs555.dfs.util;

import cs555.dfs.node.NodeRecord;

import java.util.concurrent.ConcurrentHashMap;

public class DetermineTopThree {

    public static String determineTopThreeChunkServers(ConcurrentHashMap<String, NodeRecord> nodes) {
        String first = "";
        String second = "";
        String third = "";

        long firstSpace = 0;
        long secondSpace = 0;
        long thirdSpace = 0;

        for (NodeRecord node : nodes.values()) {
            if (node.getUsableSpace() > firstSpace) {

                third = second;
                thirdSpace = secondSpace;

                second = first;
                secondSpace = firstSpace;

                first = node.toString();
                firstSpace = node.getUsableSpace();

            } else if (node.getUsableSpace() > secondSpace && node.getUsableSpace() < firstSpace) {

                third = second;
                thirdSpace = secondSpace;

                second = node.toString();
                secondSpace = node.getUsableSpace();

            } else if (node.getUsableSpace() > thirdSpace && node.getUsableSpace() < secondSpace) {

                third = node.toString();
                thirdSpace = node.getUsableSpace();
            }
        }
        return first + "," + second + "," + third;
    }
}
