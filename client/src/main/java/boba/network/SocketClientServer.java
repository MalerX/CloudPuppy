package boba.network;

import com.sun.javafx.webkit.KeyCodeMap;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SocketClientServer implements Runnable {
    private static final Logger log = Logger.getLogger(SocketClientServer.class);
    private static SocketClientServer INSTANCE;

    private final String ADDRESS;
    private final int PORT;
    private Socket socket;
    public static SocketClientServer getInstance(String ADDRESS, int PORT) {
        if (INSTANCE == null) {
            INSTANCE = new SocketClientServer(ADDRESS, PORT);
            return INSTANCE;
        }
        return INSTANCE;
    }

    private SocketClientServer(String ADDRESS, int PORT) {
        this.ADDRESS = ADDRESS;
        this.PORT = PORT;
    }

    @Override
    public void run() {
        try {
            this.socket = new Socket(ADDRESS, PORT);
            socket.getChannel().write(ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            log.error("Connect fail" + e);
        }
    }
    public void write(String msg) {
        try {
            socket.getChannel().write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
