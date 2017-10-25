package cs555.dfs.test;

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

    public static void main(String[] args) {
        StringTest stringTest = new StringTest();
        stringTest.test();
    }
}
