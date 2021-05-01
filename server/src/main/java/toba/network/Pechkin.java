package toba.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;
import toba.coven.Gatekeeper;


public class Pechkin extends SimpleChannelInboundHandler<String> {
    private static Logger log = Logger.getLogger(Pechkin.class);
    private final Gatekeeper gatekeeper;

    public Pechkin(Gatekeeper gatekeeper) {
        this.gatekeeper = gatekeeper;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client connect. " + ctx.name());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client disconnected.");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        gatekeeper.work(ctx);
    }
}
