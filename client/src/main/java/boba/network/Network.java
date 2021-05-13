package boba.network;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

public class Network implements Runnable {
    private static final Logger log = Logger.getLogger(Network.class);

    private Socket socket;
    private Sender sender;
    private Received received;

    public Network(String server_address, int port,
                   BlockingQueue<ByteBuffer> outQueue, BlockingQueue<ByteBuffer> inQueue) {
        try {
            this.socket = new Socket(server_address, port);
            this.received = new Received(new DataInputStream(socket.getInputStream()), inQueue);
            this.sender = new Sender(new DataOutputStream(socket.getOutputStream()), outQueue);
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
            sender.close();
            received.close();
            socket.close();
        } catch (IOException e) {
            log.error("Fail close connection: ", e);
        }
    }
}

class Sender implements Runnable {
    private static final Logger log = Logger.getLogger(Sender.class);

    private final DataOutputStream out;
    private final BlockingQueue<ByteBuffer> outQueue;

    public Sender(final DataOutputStream out, BlockingQueue<ByteBuffer> outQueue) {
        this.out = out;
        this.outQueue = outQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                out.write(outQueue.take().array());
                log.info("Message send.");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Fail send message: ", e);
        }
    }

    public void close() throws IOException {
        out.close();
    }
}

class Received implements Runnable {
    private static final Logger log = Logger.getLogger(Received.class);
    private static final int ONE_KB = 1024;

    private final DataInputStream in;
    private final BlockingQueue<ByteBuffer> inQueue;

    public Received(final DataInputStream in, final BlockingQueue<ByteBuffer> inQueue) {
        this.in = in;
        this.inQueue = inQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                int msgLen = in.available();
                if (msgLen != 0) {
                    byte[] msg = new byte[msgLen];
                    in.read(msg);
                    inQueue.add(ByteBuffer.wrap(msg));
                    log.info("Received message.");
                }
            }
        } catch (IOException e) {
            log.error("Fail received message: ", e);
        }
    }

    public void close() throws IOException {
        in.close();
    }
}