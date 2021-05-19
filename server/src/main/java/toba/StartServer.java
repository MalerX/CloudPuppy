package toba;

import toba.network.ServerNetty;

public class StartServer {
    private static final int PORT = 8189;
    public static void main(String[] args) {
        String homeDir = null;
        if (args.length != 1) {
            System.err.println("Wrong work directory!");
            System.exit(1);
        } else
            homeDir = args[0];
        new ServerNetty(PORT, homeDir).start();
    }
}
