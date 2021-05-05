package boba.network;

import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class Network implements Runnable {
    private static final Logger log = Logger.getLogger(Network.class);

    private final String server_address;
    private final int port;
    private static Network instance = null;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Sender sender;
    private Received received;

    public static Network getInstance(String server_address, int port,
                                      BlockingQueue<byte[]> sendQueue, BlockingQueue<byte[]> answerQueue) {
        if (instance == null) {
            instance = new Network(server_address, port, sendQueue, answerQueue);
            return instance;
        }
        return instance;
    }

    private Network(String server_address, int port,
                    BlockingQueue<byte[]> sendQueue, BlockingQueue<byte[]> answerQueue) {
        this.server_address = server_address;
        this.port = port;
        try {
            socket = new Socket(server_address, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            this.received = new Received(in, answerQueue);
            this.sender = new Sender(out, sendQueue);
        } catch (IOException e) {
            log.error("Fail connect: ", e);
        }
    }

    @Override
    public void run() {
        new Thread(sender).start();
        new Thread(received).start();
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
}

class Sender implements Runnable {
    private static final Logger log = Logger.getLogger(Sender.class);

    private final DataOutputStream out;
    private final BlockingQueue<byte[]> sendQueue;

    public Sender(DataOutputStream out, BlockingQueue<byte[]> sendQueue) {
        this.out = out;
        this.sendQueue = sendQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                out.write(sendQueue.take());
                log.info("Message send.");
            } catch (IOException | InterruptedException e) {
                log.error("Fail send message: ", e);
            }
        }
    }
}

class Received implements Runnable {
    private static final Logger log = Logger.getLogger(Received.class);

    private final DataInputStream in;
    private final BlockingQueue<byte[]> answerQueue;

    public Received(final DataInputStream in, final BlockingQueue<byte[]> answerQueue) {
        this.in = in;
        this.answerQueue = answerQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                int msgLen = in.available();
                if (msgLen != 0) {
                    byte[] msg = new byte[msgLen];
                    in.read(msg);
                    answerQueue.add(msg);
                    log.info("Received message.");
                }
            }
        } catch (IOException e) {
            log.error("Fail received message: ", e);
        }
    }
}