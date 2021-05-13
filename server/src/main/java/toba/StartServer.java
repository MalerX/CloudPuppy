package toba;

import toba.network.ServerNetty;

public class StartServer {
    private static final int PORT = 8189;
    public static void main(String[] args) {
        new ServerNetty(PORT, args[0]).start();
    }
}
