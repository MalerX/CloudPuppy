package boba.network;

import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network implements Runnable {
    private static final Logger log = Logger.getLogger(Network.class);

    private final String server_address;
    private final int port;
    private static Network instance = null;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public static Network getInstance(String server_address, int port) {
        if (instance == null) {
            instance = new Network(server_address, port);
            return instance;
        }
        return instance;
    }

    private Network(String server_address, int port) {
        this.server_address = server_address;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(server_address, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            log.error("Fail connect: ", e);
        }
    }

    public void stop() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            log.error("Fail close connection: ", e);
        }
    }

    public void send(byte[] msg) {
        try {
            out.write(msg);
        } catch (IOException e) {
            log.error("Fail sending message: ", e);
        }
    }

    public byte[] received() {
        try {
            int msgLen = in.available();
            byte[] msg = new byte[msgLen];
            in.read(msg);
            return msg;
        } catch (IOException e) {
            log.error("Fail read message: ", e);
        }
        return null;
    }
}
