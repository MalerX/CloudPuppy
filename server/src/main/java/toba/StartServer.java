package toba;

import toba.network.ServerNetty;
import toba.network.StringMessageHandler;

public class StartServer {
    private static final int PORT = 8189;
    public static void main(String[] args) {
        new ServerNetty(PORT, new StringMessageHandler()).start();
    }
}
