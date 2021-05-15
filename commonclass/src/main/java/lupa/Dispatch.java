package lupa;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.EnumSet;

import static lupa.SignalBytes.*;

public class Dispatch {
    private static final Logger log = Logger.getLogger(Dispatch.class);
    private static final int BUFFER_SIZE = 1024;

    private File currentDir;
    private String remoteAddress;
    private int localPort = 25000;

    public Dispatch() {
    }

    public Dispatch(String remoteAddress, File currentDir) {
        this.remoteAddress = remoteAddress;
        this.currentDir = currentDir;
    }

    public int getLocalPort() {
//        int min = 50000;
//        int max = 51000;
//        this.localPort = (int) (Math.random() * ++max) + min;
        return localPort;
    }

    public void send(int remotePort, File file) {
        new Thread(() -> {
            FileChannel fileChannel = null;
            try (SocketChannel socketChannel = SocketChannel.open(
                    new InetSocketAddress(remoteAddress, remotePort))) {
                log.info("The connection is established with remote server" + socketChannel.getRemoteAddress());
                byte signal = getSignal(socketChannel);
                if (signal == OK) {
                    log.info("The connection is established. Send the header.");
                    ByteBuffer header = sendHeader(file.getName(), file.length());
                    socketChannel.write(header);
                    socketChannel.socket().getOutputStream().flush();
                    log.info("Header successfully send.");
                } else
                    throw new RuntimeException("There is no response from the server.");
                signal = getSignal(socketChannel);
                if (signal == OK) {
                    log.info("The header is delivered. The server is ready to receive the file.");
                    fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
                    long sendByte = sendByte = fileChannel.transferTo(0, fileChannel.size(), socketChannel);
                    socketChannel.socket().getOutputStream().flush();
                    fileChannel.close();
                    log.info(String.format("The file has been poisoned. Transmitted %d bytes", sendByte));
                } else
                    throw new RuntimeException("Error send header.");
            } catch (IOException e) {
                log.error("Fail connect to remote host.", e);
            }
        }).start();
    }

    public void received() {
        new Thread(() -> {
            ServerSocketChannel serverSocket = null;
            SocketChannel socketChannel = null;
            FileChannel fileChannel = null;
            try {
                serverSocket = ServerSocketChannel.open();
                serverSocket.socket().bind(new InetSocketAddress(localPort));
                socketChannel = serverSocket.accept();
                sendSignal(socketChannel, OK);
                log.info("The client is connected. Send confirmation.");

                HeaderData<String, Long> headerData = headerParse(socketChannel);
                String nameFile = headerData.getName();
                long sizeInputFile = headerData.getSize();
                File receivedFile = new File(currentDir, nameFile);
                if (receivedFile.exists()) {
                    sendSignal(socketChannel, ERR);
                    log.error("File already exist. Abort operation.");
                    throw new RuntimeException("File already exist. Abort operation.");
                }

                fileChannel = FileChannel.open(receivedFile.toPath(),
                        EnumSet.of(StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING,
                                StandardOpenOption.WRITE));
                log.info("File create. Open channel. Ready receive content.");
                sendSignal(socketChannel, OK);

                long receivedBytes = fileChannel.transferFrom(socketChannel, 0, sizeInputFile);
                log.info(String.format("Received completed. Receive  %d bytes", receivedBytes));

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fileChannel.close();
                    socketChannel.close();
                    serverSocket.close();
                } catch (IOException e) {
                    log.info("Fail close resource.");
                }
            }
        }).start();
    }

    private void sendSignal(SocketChannel channel, byte signal) throws IOException {
        ByteBuffer confirm = ByteBuffer.allocate(LENGTH_SIG_BYTE);
        confirm.put(signal).flip();
        channel.write(confirm);
        channel.socket().getOutputStream().flush();
        log.info("Confirm successfully send.");
    }

    private ByteBuffer sendHeader(String name, long size) {
        ByteBuffer result = ByteBuffer.allocate(LENGTH_INT
                + name.getBytes(StandardCharsets.UTF_8).length
                + LENGTH_LONG);

        return result.put(ByteBuffer.allocate(LENGTH_INT).putInt(name.getBytes(StandardCharsets.UTF_8).length).array())
                .put(name.getBytes(StandardCharsets.UTF_8))
                .putLong(size)
                .flip();
    }

    private HeaderData<String, Long> headerParse(SocketChannel channel) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(BUFFER_SIZE);
        channel.read(header);
        header.flip();
        byte[] nameByte = new byte[header.getInt()];
        header.get(nameByte);
        long sizeInFile = header.getLong();
        return new HeaderData<>(new String(nameByte), sizeInFile);
    }

    private byte getSignal(SocketChannel channel) throws IOException {
        ByteBuffer signal = ByteBuffer.allocate(LENGTH_SIG_BYTE);
        channel.read(signal);
        signal.flip();
        return signal.get();
    }

    public static void main(String[] args) {
        Dispatch dispatch = new Dispatch("localhost", new File("data/malerx"));
        int inPort = dispatch.getLocalPort();
        dispatch.received();
        dispatch.send(inPort, new File("data/storage/2.jpg"));
    }
}
class HeaderData<F, S> {
    private final F fileName;
    private final S fileSize;

    protected HeaderData(F fileName, S fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public F getName() {
        return fileName;
    }

    public S getSize() {
        return fileSize;
    }
}