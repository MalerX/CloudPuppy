package toba.coven;

import io.netty.buffer.ByteBuf;
import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.sql.*;

import static lupa.SignalBytes.*;

public class Archivist {

    private static final Logger log = Logger.getLogger(Archivist.class);
    private String login;
    private Connection connection;
    private PreparedStatement statement;

    public Archivist() {
        try {
            if (!new File("data").exists()) {
                new File("data").mkdir();
                log.info("Create directory 'data'.");
            }
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

    public byte authentication(Object msg) {
        ByteBuf buff = (ByteBuf) msg;
        ByteBuffer inBuff = buff.nioBuffer();
        byte signal = inBuff.get();


        if (signal == AUTH)
            return getAuth(inBuff) ? AUTH_OK : AUTH_FAIL;
        if (signal == REG)
            return getReg(inBuff) ? REG_OK : REG_FAIL;
        return ERR;
    }

    private boolean getReg(ByteBuffer buff) {
        try {
            Pair<String, String> loginPass = getLoginPassword(buff);

            statement = connection.prepareStatement(
                    "INSERT INTO 'users' ('login', 'password') VALUES (?, ?);"
            );
            statement.setString(1, loginPass.getKey());
            statement.setString(2, loginPass.getValue());
            statement.execute();
            log.info(String.format("New user with login %s successfully registered.", loginPass.getKey()));
            return true;
        } catch (SQLException | IOException e) {
            log.error(String.format("Fail registration new user: %s", e));
        }
        return false;
    }

    private boolean getAuth(ByteBuffer buff) {
        String truePass = "";
        Pair<String, String> loginPass = new Pair<>(null, null);
        try {
            loginPass = getLoginPassword(buff);
            login = loginPass.getKey();
            statement = connection.prepareStatement(
                    "SELECT * FROM users WHERE login = ?"
            );
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                truePass = resultSet.getString("password");
            }
        } catch (SQLException | IOException e) {
            log.error("Error read DataBase ", e);
        } finally {
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                log.error("Fail close connection.");
            }
        }
        return truePass.equals(loginPass.getValue());
    }

    private Pair<String, String> getLoginPassword(ByteBuffer buff) throws IOException {
        byte[] tmpByteArray = new byte[LENGTH_INT];

        buff.get(tmpByteArray);
        byte[] loginInByte = new byte[ByteBuffer.wrap(tmpByteArray).getInt()];
        buff.get(loginInByte);
        String inLogin = new String(loginInByte);

        buff.get(tmpByteArray);
        byte[] passwordInByte = new byte[ByteBuffer.wrap(tmpByteArray).getInt()];
        buff.get(passwordInByte);
        String inPassword = new String(passwordInByte);

        return new Pair<>(inLogin, inPassword);
    }
}
