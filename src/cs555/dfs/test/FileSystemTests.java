package cs555.dfs.test;

import java.io.File;

public class FileSystemTests {

    public FileSystemTests() {

    }

    private void testUsableSpace() {
        File testDir = new File ("/Users/nicholas/Desktop");
        System.out.println(testDir.getUsableSpace());
        System.out.println(testDir.getAbsolutePath());
    }


    public static void main(String[] args) {
        FileSystemTests fileSystemTests = new FileSystemTests();
        fileSystemTests.testUsableSpace();
    }
}
