package toba.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;


public class StringMessageHandler extends SimpleChannelInboundHandler<String> {
    private static Logger log = Logger.getLogger(StringMessageHandler.class);

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
        log.info("received: " + s);
        ctx.writeAndFlush("echo: " + s);
    }
}
