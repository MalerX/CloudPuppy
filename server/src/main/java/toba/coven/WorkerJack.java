package toba.coven;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lupa.INavigate;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static lupa.SignalBytes.*;

public class WorkerJack {
    private static final Logger log = Logger.getLogger(WorkerJack.class);
    private final INavigate navigator;

    public WorkerJack(INavigate navigator) {
        this.navigator = navigator;
    }

    public void work(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buff = (ByteBuf) msg;
        ByteBuffer inBuff = buff.nioBuffer();
        byte signal = inBuff.get();

        switch (signal) {
            case REFRESH -> {
                log.info("Request received refresh.");
                String tmpStr = navigator.refresh();
                ctx.writeAndFlush(Unpooled.wrappedBuffer(
                        tmpStr.getBytes(StandardCharsets.UTF_8)));
            }
            case MKDIR -> {
                log.info("Request to create folder received.");
                navigator.mkDir(getName(inBuff));
            }
            case BACK -> {
                log.info("Request received to return to the previous directory.");
                navigator.back();
            }
            case JOIN -> {
                log.info("A request was received to move to a directory.");
                navigator.joinDir(getName(inBuff));
            }
            case UP -> {
                log.info("A request was received to navigate to the parent directory.");
                navigator.upDir();
            }
            case RM -> {
                log.info("A request was received to delete an item.");
                navigator.rmItem(getName(inBuff));
            }
            case UPLOAD -> {
                log.info("Uploading a file to the server. Create the receiving object. Open the socket.");
                int receivedPort = navigator.download();

                ctx.writeAndFlush(Unpooled.wrappedBuffer(
                        ByteBuffer.allocate(LENGTH_INT)
                                .putInt(receivedPort).array()));
            }
            case DOWNLOAD -> {
                log.info("Uploading a file to the client. Create an object. Open the socket.");
                byte[] nameByte = new byte[inBuff.getInt()];
                inBuff.get(nameByte);
                int clientPort = inBuff.getInt();
                String fileName = new String(nameByte);
                navigator.upload(fileName, clientPort);
            }
        }
    }

    private String getName(ByteBuffer inBuff) {
        byte[] tmpByteArray = new byte[LENGTH_INT];
        inBuff.get(tmpByteArray);
        byte[] nameDirInByte = new byte[ByteBuffer.wrap(tmpByteArray).getInt()];
        inBuff.get(nameDirInByte);
        return new String(nameDirInByte);
    }
}
