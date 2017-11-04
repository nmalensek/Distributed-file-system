package cs555.dfs.test;

import cs555.dfs.hash.ComputeHash;
import cs555.dfs.messages.Chunk;
import cs555.dfs.messages.ReadFileInquiry;
import cs555.dfs.node.ChunkServer;
import cs555.dfs.util.Splitter;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import static cs555.dfs.util.Constants.*;

public class SliceTest {

    private LinkedList<byte[]> chunkList = new LinkedList<>();
    private boolean corrupted = false;
    private int corruptedSlice;
    private String corruptedChunkName;

    private void writeFile() throws IOException {
        File file = new File("/Users/nicholas/Documents/School/CS555/HW4/test/animals_of_the_past.txt");
        chunkFile(file);

        File testDir = new File(testDirectory);
        testDir.mkdirs();

        int i = 1;
        for (byte[] chunk : chunkList) {
            try(FileOutputStream fileOutputStream = new FileOutputStream(testDirectory + i + ".txt")) {
                fileOutputStream.write(chunk);
            }
            writeHashForSlices(chunk, i);
            i++;
        }
    }

    private void getFileBytes() throws IOException {
        File file = new File("/Users/nicholas/Documents/School/CS555/HW4/test/animals_of_the_past.txt");
        chunkFile(file);
    }

    private void chunkFile(File fileToChunk) throws IOException {

        byte[] chunk = new byte[chunkSize];

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int read;
        try (FileInputStream fileInputStream = new FileInputStream(fileToChunk)) {
            while ((read = fileInputStream.read(chunk)) != -1) {
                byteArrayOutputStream.write(chunk, 0, read);
                chunkList.add(byteArrayOutputStream.toByteArray());
                byteArrayOutputStream.reset();
            }
        }
    }

    public synchronized void writeHashForSlices(byte[] chunkBytes, int fileName) throws IOException {

        int index = 0;
        int maximum = chunkBytes.length;
        int writeSize = sliceSize;

        FileWriter fileWriter = new FileWriter(testDirectory + fileName + integrity + ".txt");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        while (index < maximum) {
            if (maximum - index < writeSize) { writeSize = maximum - index; }
            byteArrayOutputStream.write(chunkBytes, index, writeSize);
            fileWriter.write(ComputeHash.SHA1FromBytes(byteArrayOutputStream.toByteArray()) + "\n");
            index += byteArrayOutputStream.size();
            byteArrayOutputStream.reset();
        }
        fileWriter.close();
    }

    private synchronized void checkChunkSlices(byte[] retrievedChunk, String chunkName) throws IOException {

        ArrayList<String> writtenHashes = getLoggedIntegrityData(chunkName);
        ArrayList<String> memoryHashes = getCurrentIntegrityHashes(retrievedChunk);

        for (int i = 0; i < writtenHashes.size(); i++) {
            if (!memoryHashes.contains(writtenHashes.get(i))) {
                corruptedSlice = i;
                corruptedChunkName = chunkName;
                corrupted = true;
                break;
            }
            System.out.println(corrupted);
        }
    }

    private synchronized ArrayList<String> getLoggedIntegrityData(String chunkName) throws IOException {

        ArrayList<String> loggedIntegrityData = new ArrayList<>();

        try (BufferedReader bufferedReader =
                     new BufferedReader(new FileReader(testDirectory + chunkName + integrity + ".txt"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                loggedIntegrityData.add(line);
            }
        }
        System.out.println(loggedIntegrityData.toString());
        return loggedIntegrityData;
    }

    private synchronized ArrayList<String> getCurrentIntegrityHashes(byte[] retrievedChunk) {

        ArrayList<String> currentIntegrityData = new ArrayList<>();

        int index = 0;
        int maximum = retrievedChunk.length;
        int writeSize = sliceSize;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        while (index < maximum) {
            if (maximum - index < writeSize) { writeSize = maximum - index; }
            byteArrayOutputStream.write(retrievedChunk, index, writeSize);
            currentIntegrityData.add(ComputeHash.SHA1FromBytes(byteArrayOutputStream.toByteArray()));
            index += byteArrayOutputStream.size();
            byteArrayOutputStream.reset();
        }
        System.out.println(currentIntegrityData.toString());
        return currentIntegrityData;
    }

    public synchronized void retrieveChunk(String chunkName) throws IOException {

        Chunk retrievedChunk = new Chunk();
        retrievedChunk.setFileName(chunkName);
        retrievedChunk.setReplicationNodes("N/A");

        byte[] chunkArray = new byte[chunkSize];
        byte[] corruptedBytes = new byte[chunkSize];

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int read;
        try (FileInputStream fileInputStream = new FileInputStream(testDirectory + chunkName + ".txt")) {
            while ((read = fileInputStream.read(chunkArray)) != -1) {
                byteArrayOutputStream.write(chunkArray, 0, read);
                checkChunkSlices(byteArrayOutputStream.toByteArray(), chunkName);
                if (!corrupted) {
                    retrievedChunk.setChunkByteArray(byteArrayOutputStream.toByteArray());
                    System.out.println("Sent chunk " + chunkName);
                } else {
                    System.out.println("Corruption detected in slice " + corruptedSlice +
                            " of chunk " + corruptedChunkName + ", initiating recovery.");
                    corruptedBytes = Arrays.copyOf(chunkArray, chunkSize);
                    corrupted = false;
                    break;
                }
                byteArrayOutputStream.reset();
            }
        }
    }

    private void test() {
        try {
//            writeFile();
//            getFileBytes();
            for (int i = 1; i < 6; i++) {
                retrieveChunk(String.valueOf(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SliceTest sliceTest = new SliceTest();
        sliceTest.test();
    }
}
