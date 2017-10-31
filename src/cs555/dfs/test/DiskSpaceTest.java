package cs555.dfs.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;

import static cs555.dfs.util.Constants.storageSpaceDirectory;

public class DiskSpaceTest {


    private void getDiskSpace() {
        ArrayList<Long> availableSpaceList = new ArrayList<>();
        File tmp = new File(storageSpaceDirectory);
        File tmpUname = new File("/tmp/nmalensk_chunks/");
        availableSpaceList.add(tmp.getUsableSpace());
        availableSpaceList.add(tmpUname.getUsableSpace());
        System.out.println(availableSpaceList.toString());
    }

    private void getRootSpace() {
        NumberFormat nf = NumberFormat.getNumberInstance();
        for (Path root : FileSystems.getDefault().getRootDirectories()) {

            System.out.print(root + ": ");
            try {
                FileStore store = Files.getFileStore(root);
                System.out.println("available=" + nf.format(store.getUsableSpace())
                        + ", total=" + nf.format(store.getTotalSpace()));
            } catch (IOException e) {
                System.out.println("error querying space: " + e.toString());
            }
        }
    }

    private void testFiles() throws IOException {
        File metaDataFile = new File("/tmp/nmalensk/test");
        System.out.println(metaDataFile.canRead());
        System.out.println(metaDataFile.canWrite());
        System.out.println(metaDataFile.canExecute());
        System.out.println(metaDataFile.createNewFile());
    }

    public static void main(String[] args) throws IOException {
        DiskSpaceTest diskSpaceTest = new DiskSpaceTest();
        diskSpaceTest.getDiskSpace();
//        diskSpaceTest.getRootSpace();
        diskSpaceTest.testFiles();
    }
}
