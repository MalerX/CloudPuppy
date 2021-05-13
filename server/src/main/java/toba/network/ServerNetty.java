package toba.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import toba.coven.Gatekeeper;

public class ServerNetty {
    private static Logger log = Logger.getLogger(ServerNetty.class);
    private final int PORT;
    private final String STORAGE_DIR;

    public ServerNetty(final int PORT, final String dir) {
        this.PORT = PORT;
        this.STORAGE_DIR = dir;
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
                            socketChannel.pipeline()
                                    .addLast(new Gatekeeper(STORAGE_DIR));
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
