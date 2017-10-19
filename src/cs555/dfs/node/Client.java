package cs555.dfs.node;

public class Client {

    private static int controllerPort;
    private static String controllerHost;


    public static void main(String[] args) {
        controllerHost = args[0];
        controllerPort = Integer.parseInt(args[1]);
    }
}
