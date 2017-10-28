package cs555.dfs.test;

import cs555.dfs.hash.ComputeHash;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

public class StringTest {


    private void test() {
        String s = "A:123,B:456,C:789,";
        String[] array = s.split(",");
        System.out.println(array.length);

        String shortString = "C789,";
        String[] shortArray = shortString.split(",");
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < shortArray.length; i++) {
            builder.append(shortArray[i]).append(",");
        }
        System.out.println(builder.toString());
    }

    private void sliceTest(String string) throws IOException {
        byte[] testBytes = string.getBytes();

        byte[] slice = new byte[5];

        int index = 0;
        long maximum = testBytes.length;
        int num = 1;

        FileWriter fileWriter = new FileWriter("test/metadata/test_integrity");

        while (index < maximum) {
            for (int i = 0; i < slice.length; i++) {
                try {
                    slice[i] = testBytes[index];
                    index++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    break;
                }
            }

            fileWriter.append(ComputeHash.SHA1FromBytes(slice)).append("\n");
            System.out.println(num);
            System.out.println(ComputeHash.SHA1FromBytes(slice));
            num++;
        }
        fileWriter.write(string);
        fileWriter.close();

    }

    private void splitTest() {
        String commas = ",,,";
        String[] split = commas.split(",");
        System.out.println(split.length);
    }

    private void  treeMapTest() {
        TreeMap<String, String> test = new TreeMap<>();
        test.put("test_5", "test");
        test.put("test_3", "test");
        test.put("test_1", "test");
        test.put("test_4", "test");
        test.put("test_2", "test");

        System.out.println(test.toString());
    }

    public static void main(String[] args) throws IOException {
        StringTest stringTest = new StringTest();
//        stringTest.test();
//        stringTest.sliceTest("abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890");
//        stringTest.sliceTest("abcdefghijklmnopqrstuvxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890");
//        stringTest.splitTest();
        stringTest.treeMapTest();
    }
}
