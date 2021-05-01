package boba.windows;

import boba.network.NettyNetwork;
import boba.network.SocketClientServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.SocketChannel;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lupa.Callback;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthWindow implements Initializable {
    private Stage authWindow;

    @FXML
    public TextField serverAddress;
    @FXML
    public TextField workDir;
    @FXML
    public TextField PORT;
    @FXML
    public Button join;
    @FXML
    public Button reg;

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passField;

    @FXML
    public TextField regLogin;
    @FXML
    public PasswordField regPass;
    @FXML
    public PasswordField repeatRegPass;

    private DataInputStream in;
    private DataOutputStream out;

    private Callback callback;
    private SocketClientServer client;

    private Service<Void> transmitter;

    public void setAuthWindow(Stage authWindow) {
        this.authWindow = authWindow;
    }

    public void getAuthentication(ActionEvent actionEvent) throws IOException {
        connect();
        client.write("hello");
    }

    public void getRegistration(ActionEvent actionEvent) {

    }

    public void authOk() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        Parent root = fxmlLoader.load();
        Stage mainWindow = new Stage();
        MainWindowCtrl mainWindowCtrl = fxmlLoader.getController();
        mainWindowCtrl.setMainWindow(mainWindow);
        mainWindowCtrl.setWorkDir(workDir.getText());
        mainWindow.setScene(new Scene(root));
        mainWindow.setResizable(false);
        authWindow.close();
        mainWindow.show();

    }

    private void connect() {
        client = SocketClientServer.getInstance(serverAddress.getText(),
                Integer.parseInt(PORT.getText()));
        Platform.runLater(client);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket(serverAddress.getText(), Integer.parseInt(PORT.getText()));
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            byte[] buff = new byte[256];
            new Thread(() -> {
                try {
                    while (true) {
                        int size = in.read(buff);
                        while (size != 0) {
                            System.out.print(buff.toString());
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
