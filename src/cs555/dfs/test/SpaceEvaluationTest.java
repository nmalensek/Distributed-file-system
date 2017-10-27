package cs555.dfs.test;

import cs555.dfs.node.NodeRecord;

import java.io.IOException;

public class SpaceEvaluationTest {

    public SpaceEvaluationTest() throws IOException {
    }

    NodeRecord[] testNodes =
            {
                    new NodeRecord("test:1111", null, 300000L),
                    new NodeRecord("test:5678", null, 400000L),
                    new NodeRecord("test:23525", null, 200000L),
                    new NodeRecord("test:1234", null, 500000L),
                    new NodeRecord("test:23425", null, 100000L)
            };

    private void testSpace() {
        String first = "";
        String second = "";
        String third = "";

        long firstSpace = 0;
        long secondSpace = 0;
        long thirdSpace = 0;

        for (NodeRecord node : testNodes) {
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
        System.out.println(first + " -- " + firstSpace);
        System.out.println(second + " -- " + secondSpace);
        System.out.println(third + " -- " + thirdSpace);
    }

    private void setNameAndSpace(String previous, long previousSpace, String newName, long newSpace) {
        previous = newName;
        previousSpace = newSpace;
    }

    public static void main(String[] args) throws IOException {
        SpaceEvaluationTest test = new SpaceEvaluationTest();
        test.testSpace();
    }
}
