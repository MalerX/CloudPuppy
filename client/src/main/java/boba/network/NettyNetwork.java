package boba.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lupa.Callback;
import org.apache.log4j.Logger;

public class NettyNetwork implements Runnable {
    private static final Logger log = Logger.getLogger(NettyNetwork.class);

    private static NettyNetwork INSTANCE;
    private SocketChannel clientChannel;
    private String address;
    private int port;
    private Callback callback;

    public static NettyNetwork getInstance(final String address, final int port, final Callback callback, SocketChannel mainChannel) {
        if (INSTANCE == null) {
            INSTANCE = new NettyNetwork(address, port, callback, mainChannel);
        }
        return INSTANCE;
    }

    private NettyNetwork(final String address, final int port, Callback callback, SocketChannel mainChannel) {
        this.address = address;
        this.port = port;
        this.callback = callback;
        this.clientChannel = mainChannel;
    }

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            clientChannel = ch;
                        }
                    });
            ChannelFuture future = bootstrap.connect(address, port).sync();
            log.info("Server start.");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Connection failed: " + e);
        } finally {
            group.shutdownGracefully();
        }
    }

    public synchronized void write(String msg) {
        clientChannel.writeAndFlush(msg);
    }
}
