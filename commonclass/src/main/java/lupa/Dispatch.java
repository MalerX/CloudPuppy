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
import java.util.EnumSet;

import static lupa.SignalBytes.*;

public class Dispatch {
    private static final Logger log = Logger.getLogger(Dispatch.class);
    private static final int BUFFER_SIZE = 1024;

    private final File currentDir;
    private final String remoteAddress;
    private final int localPort;

    public Dispatch(String remoteAddress, File currentDir) {
        this.remoteAddress = remoteAddress;
        this.currentDir = currentDir;
        this.localPort = generatePort();
    }

    private int generatePort() {
        int min = 50000;
        int max = 51000;
        return (int) (Math.random() * (max - min) + min);
    }

    public int getLocalPort() {
        return localPort;
    }

    public void send(int remotePort, String fileName) {
        new Thread(() -> {
            FileChannel fileChannel = null;
            try (SocketChannel socketChannel = SocketChannel.open(
                    new InetSocketAddress(remoteAddress, remotePort))) {

                File file = new File(currentDir, fileName);
                if (file.isDirectory() && !file.exists()) {
                    sendSignal(socketChannel, ERR);
                    throw new RuntimeException(
                            String.format("The object %s was not found or is a directory.", fileName));
                } else {
                    sendSignal(socketChannel, OK);
                    log.info(String.format("File %s checked. Continue transmission.", fileName));
                }

                fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ);

                log.info("The connection is established. Send the header.");
                sendHeader(socketChannel, file.getName(), fileChannel.size());

                byte signal = getSignal(socketChannel);
                if (signal == OK) {
                    log.info("The header is delivered. The server is ready to receive the file.");
                    long sendBytes;
                    do {
                        sendBytes = fileChannel.transferTo(fileChannel.position(), BUFFER_SIZE, socketChannel);
                        socketChannel.socket().getOutputStream().flush();
                        fileChannel.position(fileChannel.position() + sendBytes);
                    } while (sendBytes > 0);
                    log.info(String.format("The file has been sent. Transmitted %d bytes", fileChannel.size()));
                } else
                    throw new RuntimeException("Error send header or file already exists on the remote host.");
            } catch (IOException e) {
                log.error("Fail connect to remote host.", e);
            } finally {
                try {
                    assert fileChannel != null;
                    fileChannel.close();
                } catch (IOException e) {
                    log.error("Fail close file channel.");
                }
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
                log.info("Wait connect client.");
                socketChannel = serverSocket.accept();
                log.info("The client is connected.");

                if (getSignal(socketChannel) == ERR)
                    throw new RuntimeException("Aborted operation.");

                Header header = new Header().getHeader(socketChannel);

                File receivedFile = new File(currentDir, header.getName());
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
                long receivedBytes;
                do {
                    receivedBytes = fileChannel.transferFrom(socketChannel, fileChannel.position(), BUFFER_SIZE);
                    fileChannel.position(fileChannel.position() + receivedBytes);
                } while (receivedBytes > 0);
                log.info(String.format("Received completed. Receive  %d bytes", fileChannel.size()));

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    assert fileChannel != null;
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

    private void sendHeader(SocketChannel socketChannel, String name, long size) throws IOException {
        ByteBuffer result = ByteBuffer.allocate(LENGTH_INT
                + name.getBytes(StandardCharsets.UTF_8).length
                + LENGTH_LONG);

        result.put(ByteBuffer.allocate(LENGTH_INT).putInt(name.getBytes(StandardCharsets.UTF_8).length).array())
                .put(name.getBytes(StandardCharsets.UTF_8))
                .putLong(size)
                .flip();

        socketChannel.write(result);
        socketChannel.socket().getOutputStream().flush();

        log.info("Header successfully send.");
    }

    private byte getSignal(SocketChannel channel) throws IOException {
        ByteBuffer signal = ByteBuffer.allocate(LENGTH_SIG_BYTE);
        channel.read(signal);
        signal.flip();
        return signal.get();
    }

    /* Инкапсулировал работу с заголовком во вложенном классе. Так как возвращаемых значений >1,
то использовать внутренний класс предпочтительнее, чем метод. */
    private static class Header {
        private String fileName;
        private long fileSize;

        public Header getHeader(SocketChannel channel) throws IOException {
            ByteBuffer header = ByteBuffer.allocate(BUFFER_SIZE);
            channel.read(header);
            header.flip();
            byte[] nameByte = new byte[header.getInt()];
            header.get(nameByte);
            long sizeIncomingFile = header.getLong();

            this.fileName = new String(nameByte);
            this.fileSize = sizeIncomingFile;

            return this;
        }

        public String getName() {
            return fileName;
        }

        public long getSize() {
            return fileSize;
        }
    }

    public static void main(String[] args) {
        Dispatch dispatch = new Dispatch("localhost", new File("data/malerx"));
        int port = dispatch.getLocalPort();
        dispatch.received();
//        dispatch.send(port, new File("data/storage/2.jpg"));
    }
}