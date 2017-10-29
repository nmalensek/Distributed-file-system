package cs555.dfs.processing.chunkserverprocessing;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static cs555.dfs.util.Constants.sliceSize;
import static cs555.dfs.util.Constants.storageDirectory;

public class HandleSliceCorruption {


    public synchronized void overWriteGoodSlices(byte[] chunkBytes, int corruptedSliceNumber, String chunkName)
            throws IOException {
        int index = 0;
        int maximum = chunkBytes.length;
        int writeSize = sliceSize;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try(FileOutputStream fileOutputStream = new FileOutputStream(storageDirectory + chunkName)) {
            for (int sliceNumber = 0; sliceNumber < corruptedSliceNumber; sliceNumber++) {
                if (maximum - index < writeSize) {
                    writeSize = maximum - index;
                }
                byteArrayOutputStream.write(chunkBytes, index, writeSize);
                index += writeSize;
            }
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
        }
    }
}
