package cs555.dfs.test;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class FileChunkTest {

    private LinkedList<byte[]> chunkList = new LinkedList<>();
    private int chunkSize = 65536;
    private static String fileStorageLocation = "test/";
    private HashMap<String, String> testMap = new HashMap<>();

    public FileChunkTest() {

    }

    private void chunkFiles(String filePath) throws IOException {
        File fileToChunk = new File(filePath);

        FileInputStream fileInputStream = new FileInputStream(fileToChunk);

        byte[] chunk = new byte[chunkSize];

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int read;
        try {
            while ((read = fileInputStream.read(chunk)) != -1) {
                byteArrayOutputStream.write(chunk, 0, read);
                chunkList.add(byteArrayOutputStream.toByteArray());
                byteArrayOutputStream.reset();
            }
            System.out.println(chunkList.size());
        } finally {
            fileInputStream.close();
        }
    }

    private void writeChunks(String filename) throws IOException {
        int blockNumber = 1;
        for (byte[] chunk : chunkList) {
            FileOutputStream fileOutputStream = new FileOutputStream(fileStorageLocation + filename + "_chunk" + blockNumber);
            try {
                fileOutputStream.write(chunk, 0, chunk.length);
                blockNumber++;
            } finally {
                fileOutputStream.close();
            }
        }
    }

    private void getFilesToMerge(String chunkFilename) throws IOException {
        String filename = chunkFilename.substring(0, chunkFilename.lastIndexOf('_'));
        File exampleFile = new File(chunkFilename);
        File[] files = exampleFile.getParentFile().listFiles(
                (File f, String name) -> name.matches(filename + "[_]\\d+"));
        Arrays.sort(files);

        for (File fileChunk : files) {
            chunkList.add(Files.readAllBytes(fileChunk.toPath()));
        }
    }

    private void mergeChunks(String destinationPath) throws IOException {
        File destinationFile = new File(destinationPath);
        FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);

        for (byte[] array : chunkList) {
            fileOutputStream.write(array);
        }
    }

    public static void main(String[] args) throws IOException {
        FileChunkTest fileChunkTest = new FileChunkTest();
        fileChunkTest.chunkFiles("/Users/nicholas/Documents/School/CS555/HW4/test/animals_of_the_past.txt");
        fileChunkTest.writeChunks("animals_of_the_past.txt");
        fileChunkTest.mergeChunks(fileStorageLocation + "merged.txt");
    }
}
