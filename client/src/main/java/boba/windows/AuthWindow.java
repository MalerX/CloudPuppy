package boba.windows;

import boba.network.Network;
import boba.network.SocketClientServer;
import javafx.application.Platform;
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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    private Network net;

    public void setAuthWindow(Stage authWindow) {
        this.authWindow = authWindow;
    }

    public void getAuthentication(ActionEvent actionEvent) throws IOException {
        byte[] msg = "AUTH malerx hjrft657".getBytes(StandardCharsets.UTF_8);
        net.send(msg);
        System.out.println(net.received().toString());
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        net = new Network(serverAddress.getText(), Integer.parseInt(PORT.getText()));
        net.start();
    }
}
