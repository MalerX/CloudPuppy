package toba.coven;

import io.netty.buffer.ByteBuf;

import static lupa.Commands.*;

import lupa.Commands;
import org.apache.log4j.Logger;

import java.sql.*;

public class Archivist {
    private static final Logger log = Logger.getLogger(Archivist.class);
    private String login;
    private Connection connection;
    private PreparedStatement statement;

    public Archivist() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:data/CLOUDPUPPY.users.db");
            connection.prepareStatement(
                    "CREATE TABLE if NOT EXISTS 'users'" +
                    "('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'login' TEXT NOT NULL UNIQUE, 'password' TEXT NOT NULL);")
                    .execute();
        } catch (SQLException | ClassNotFoundException e) {
            log.error("Fail open DB: ", e);
        }
    }

    public String getLogin() {
        return login;
    }

    public Commands authentication(Object msg) {
        ByteBuf buf = ((ByteBuf) msg);
        byte[] msgIn = new byte[buf.readableBytes()];
        buf.readBytes(msgIn);
        String authStr = new String(msgIn).trim();

        if (authStr.startsWith(AUTH.name())) {
            return getAuth(authStr) ? AUTH_OK : AUTH_FAIL;

        }
        if (authStr.startsWith(REG.name())) {
            return getReg(authStr) ? REG_OK : REG_FAIL;
        }
        return ERR;
    }

    private boolean getReg(String authStr) {
        String[] regStr = authStr.split("\\s", 4);

        if (!regStr[2].equals(regStr[3]))
            return false;
//        try {
        return true;
//        }
//        "INSERT INTO 'users' ('login', 'password', 'path') VALUES ('malerx', 'hjrft657', 'storage/malerx');"
    }

    private boolean getAuth(String authStr) {
        String[] loginPass = authStr.split("\\s");
        String truePass = "";

        try {
            statement = connection.prepareStatement(
                    "SELECT * FROM users WHERE login = ?"
            );
            statement.setString(1, loginPass[1]);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                login = resultSet.getString("login");
                truePass = resultSet.getString("password");
            }
        } catch (SQLException throwables) {
            log.error("Error read DataBase ", throwables);
        }
        return truePass.equals(loginPass[2]);
    }
}
