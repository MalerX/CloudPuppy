package toba.coven;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lupa.Navigator;
import org.apache.log4j.Logger;

import static lupa.SignalBytes.AUTH_FAIL;
import static lupa.SignalBytes.AUTH_OK;

public class Gatekeeper extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(Gatekeeper.class);
    private boolean auth = false;
    private final String HOME_DIR = "storage/";
    private WorkerJack jack;


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
        if (auth) {
            jack.work(ctx, msg);
        } else {
            ctx.writeAndFlush(getAuth(msg));
        }
    }

    private ByteBuf getAuth(Object msg) {
        Archivist arch = new Archivist();
        byte resultByte = arch.authentication(msg);
        switch (resultByte) {
            case AUTH_OK -> {
                auth = true;
                jack = new WorkerJack(new Navigator(HOME_DIR + arch.getLogin()));
                log.info(String.format("Authentication successful. User: %s", arch.getLogin()));
            }
            case AUTH_FAIL -> log.info("Authentication fail.");
        }
        return Unpooled.wrappedBuffer(new byte[]{resultByte});
    }
}
