package boba.network;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import lupa.Callback;

public class ClientHandler extends SimpleChannelInboundHandler<String> {
    private final Callback callback;

    public ClientHandler(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg);
    }
}
