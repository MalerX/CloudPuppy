package toba.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.log4j.Logger;

public class ServerNetty {
    private static Logger log = Logger.getLogger(ServerNetty.class);
    private final int PORT;
    private final StringMessageHandler messageHandler;

    public ServerNetty(final int PORT, final StringMessageHandler handler) {
        this.PORT = PORT;
        this.messageHandler = handler;
    }

    public void start() {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    messageHandler
                            );
                        }
                    });
            ChannelFuture future = bootstrap.bind(PORT).sync();
            log.debug("Server started");
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("Fail start server: ", e);
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}