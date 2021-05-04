package boba.windows;

import boba.network.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static lupa.SignalBytes.*;

public class AuthWindow {
    private final static Logger log = Logger.getLogger(AuthWindow.class);
    private Stage authWindow;
    private final static int LEN_INT = 4;
    private final static int LEN_SIG_BYTE = 1;

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
//        Пока что сеть крутится в JavaFX треде, потому что я ещё ума не дал, как связать работу различных потоков.
//        Работаю над этим.
        net = Network.getInstance(serverAddress.getText(), Integer.parseInt(PORT.getText()));
        net.run();

        ByteBuffer outMsg = buildAuthData(AUTH, loginField.getText(), passField.getText());
        net.send(outMsg.array());
        byte[] answer = null;
        while (answer == null) {
            answer = net.received();
        }
        if (answer[0] == AUTH_OK)
            authOk();
        if (answer[0] == AUTH_FAIL) {
            Alert wrongPass = new Alert(Alert.AlertType.ERROR);
            wrongPass.setTitle("Ошибка авторизации.");
            wrongPass.setHeaderText("Не верные логин или пароль.");
            wrongPass.setContentText("Проверьте введённые данные.");
            wrongPass.showAndWait();
        }
    }

    private ByteBuffer buildAuthData(byte signal, String login, String password) {
//        TODO: Add check login and password.
        ByteBuffer result = ByteBuffer.allocate(LEN_SIG_BYTE
                + LEN_INT
                + login.getBytes(StandardCharsets.UTF_8).length
                + LEN_INT
                + password.getBytes(StandardCharsets.UTF_8).length
        );
        return result.put(signal)
                .put(ByteBuffer.allocate(LEN_INT).putInt(login.length()).array())
                .put(login.getBytes(StandardCharsets.UTF_8))
                .put(ByteBuffer.allocate(LEN_INT).putInt(password.length()).array())
                .put(password.getBytes(StandardCharsets.UTF_8))
                .flip();
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

}
