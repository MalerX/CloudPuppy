package boba.windows;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthWindow {
    private Stage authWindow;

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

    public void setAuthWindow(Stage authWindow) {
        this.authWindow = authWindow;
    }

    public void getAuthentication(ActionEvent actionEvent) throws IOException {
        authOk();
    }

    public void getRegistration(ActionEvent actionEvent) {

    }

    public void authOk() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        Parent root = fxmlLoader.load();
        Stage mainWindow = new Stage();
        MainWindowCtrl mainWindowCtrl = fxmlLoader.getController();
        mainWindowCtrl.setMainWindow(mainWindow);
        mainWindow.setScene(new Scene(root));
        mainWindow.setResizable(false);
        authWindow.close();
        mainWindow.show();

    }
}
