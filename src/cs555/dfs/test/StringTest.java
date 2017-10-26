package cs555.dfs.test;

import cs555.dfs.hash.ComputeHash;

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

    private void sliceTest() {
        String string = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890";
        byte[] testBytes = string.getBytes();

        byte[] slice = new byte[5];

        int index = 0;
        long maximum = testBytes.length;
        int num = 1;

        while (index < maximum) {
            for (int i = 0; i < slice.length; i++) {
                try {
                    slice[i] = testBytes[index];
                    index++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    break;
                }
            }
            System.out.println(num);
            System.out.println(ComputeHash.SHA1FromBytes(slice));
            num++;
        }

    }

    public static void main(String[] args) {
        StringTest stringTest = new StringTest();
//        stringTest.test();
        stringTest.sliceTest();
    }
}
