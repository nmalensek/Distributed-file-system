package cs555.dfs.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

public class FileChunkTest {

    private LinkedList<byte[]> chunkList = new LinkedList<>();
    private int chunkSize = 65536;

    public FileChunkTest() {

    }

    private void chunkFiles(String filePath) throws IOException {
        File fileToChunk = new File(filePath);

        FileInputStream fileInputStream = new FileInputStream(fileToChunk);

        int bytesRemaining = chunkSize;

        try {
            while (true) {
                byte[] chunk = new byte[chunkSize];
                int read = fileInputStream.read(chunk, chunk.length - bytesRemaining, bytesRemaining);
                if (read >= 0) {
                    bytesRemaining -= read;
                    if (bytesRemaining == 0) {
                        chunkList.add(chunk);
                        bytesRemaining = chunkSize;
                    }
                } else {
                    if (bytesRemaining < chunkSize) {
                        chunkList.add(chunk);
                    }
                    break;
                }
            }
            System.out.println(chunkList.size());
        } finally {
            fileInputStream.close();
        }
    }

    private void writeChunks(String filename) throws IOException {
        int blockNumber = 1;
        for (byte[] chunk : chunkList) {
            FileOutputStream fileOutputStream = new FileOutputStream("test/" + filename + "_chunk" + blockNumber);
            try {
                fileOutputStream.write(chunk, 0, chunk.length);
                blockNumber++;
            } finally {
                fileOutputStream.close();
            }
        }
    }

    private void mergeChunks(String originPath, String destinationPath) {

    }

    public static void main(String[] args) throws IOException {
        FileChunkTest fileChunkTest = new FileChunkTest();
        fileChunkTest.chunkFiles("test.mp3");
        fileChunkTest.writeChunks("test.mp3");
    }
}
