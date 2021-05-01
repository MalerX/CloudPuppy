package toba.coven;

import io.netty.channel.ChannelHandlerContext;

import java.sql.*;

public class Gatekeeper {
    private final Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    public Gatekeeper() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:data/CLOUDPUPPY.users.db");
    }
    public void work(ChannelHandlerContext ctx) {
        System.out.printf(ctx.name());
    }
}
