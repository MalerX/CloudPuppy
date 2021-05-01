package toba.coven;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lupa.Commands;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;

import static lupa.Commands.*;

public class Gatekeeper extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(Gatekeeper.class);
    private boolean auth_ok = false;
    private String homeDir = "storage/";
    private String login;


    public Gatekeeper() {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug(String.format("Client %s disconnect.", ctx.channel().remoteAddress().toString()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error in runtime. ", cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug(String.format("Client %s connect.", ctx.channel().remoteAddress().toString()));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!auth_ok)
            ctx.writeAndFlush(getAuth(msg));
        else {
            ByteBuf buf = ((ByteBuf) msg);
            byte[] msgIn = new byte[buf.readableBytes()];
            buf.readBytes(msgIn);
            byte[] out = new byte[msgIn.length];
            int k = msgIn.length - 1;
            for (int i = 0; i < msgIn.length; i++) {
                out[k--] = msgIn[i];
            }
            ctx.writeAndFlush(Unpooled.wrappedBuffer(out));
        }
    }

    private ByteBuf getAuth(Object msg) {
        Archivist arch = new Archivist();
        Commands commands = arch.authentication(msg);
        switch (commands) {
            case AUTH_OK -> {
                auth_ok = true;
                login = arch.getLogin();
                homeDir += login;
                log.info(String.format("Authentication successful. User: %s", login));
            }
            case AUTH_FAIL ->
                    log.info("Authentication fail.");
            case REG_OK ->
                    log.info("New user registered.");
            case REG_FAIL ->
                    log.info("New user register fail.");
        }
        return Unpooled.wrappedBuffer(commands.name().getBytes(StandardCharsets.UTF_8));
    }
}
