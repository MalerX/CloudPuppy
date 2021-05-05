package boba.network;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class Network implements Runnable {
    private static final Logger log = Logger.getLogger(Network.class);

    private Socket socket;
    private Sender sender;
    private Received received;

    public Network(String server_address, int port,
                   BlockingQueue<byte[]> outQueue, BlockingQueue<byte[]> inQueue) {
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

    private final OutputStream out;
    private final BlockingQueue<byte[]> outQueue;

    public Sender(OutputStream out, BlockingQueue<byte[]> outQueue) {
        this.out = out;
        this.outQueue = outQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                out.write(outQueue.take());
                log.info("Message send.");
            } catch (IOException | InterruptedException e) {
                log.error("Fail send message: ", e);
            }
        }
    }

    public void close() throws IOException {
        out.close();
    }
}

class Received implements Runnable {
    private static final Logger log = Logger.getLogger(Received.class);

    private final InputStream in;
    private final BlockingQueue<byte[]> inQueue;

    public Received(final InputStream in, final BlockingQueue<byte[]> inQueue) {
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
                    inQueue.add(msg);
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