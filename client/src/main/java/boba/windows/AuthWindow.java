package boba.windows;

import boba.network.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static lupa.SignalBytes.*;

public class AuthWindow implements Initializable {
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
    private final BlockingQueue<byte[]> sendQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<byte[]> answerQueue = new LinkedBlockingQueue<>();

    public void setAuthWindow(Stage authWindow) {
        this.authWindow = authWindow;
    }

    public void getAuthentication(ActionEvent actionEvent) throws IOException, InterruptedException {
        ByteBuffer outMsg = buildAuthData(
                AUTH,
                loginField.getText(),
                passField.getText());

        if (outMsg == null)
            return;
        sendQueue.add(outMsg.array());
        log.info("Auth data add in queue send.");
        byte[] answer;

        answer = answerQueue.take();

        if (answer[0] == AUTH_OK)
            authOk();

        if (answer[0] == AUTH_FAIL)
            errorAuth();
    }

    private ByteBuffer buildAuthData(byte signal, String login, String password) {
        if (login.contains(" ") || password.contains(" "))
            ContainsSpace();
        else {
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
        return null;
    }

    public void getRegistration(ActionEvent actionEvent) {
        ByteBuffer outMsg = null;
        if (!regPass.getText().equals(repeatRegPass.getText()))
            notMatchPass();
        else {
            outMsg = buildAuthData(REG, regLogin.getText(), regPass.getText());
        }

        if (outMsg != null) {

        }
    }

    private void notMatchPass() {
        Alert wrongFormatData = new Alert(Alert.AlertType.WARNING);
        wrongFormatData.setTitle("Не верные данные.");
        wrongFormatData.setHeaderText("Не допустимый пароль.");
        wrongFormatData.setContentText("Введённые пароли должны совпадать. Повторите ввод.");
        wrongFormatData.showAndWait();
        log.info("The entered passwords do not match");
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
        log.info("Success authorization.");
    }

    private void ContainsSpace() {
        Alert wrongFormatData = new Alert(Alert.AlertType.WARNING);
        wrongFormatData.setTitle("Не верные данные.");
        wrongFormatData.setHeaderText("Не допустимый формат данных.");
        wrongFormatData.setContentText("Логин и пароль не должны содержать пробельных символов.");
        wrongFormatData.showAndWait();
        log.info("Wrong format authentication data. Login or password contains space");
    }

    private void errorAuth() {
        Alert wrongPass = new Alert(Alert.AlertType.ERROR);
        wrongPass.setTitle("Ошибка авторизации.");
        wrongPass.setHeaderText("Не верные логин или пароль.");
        wrongPass.setContentText("Проверьте введённые данные.");
        wrongPass.showAndWait();
        log.info("Fail authorization.");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        net = Network.getInstance(serverAddress.getText(), Integer.parseInt(PORT.getText()),
                sendQueue, answerQueue);
        Thread service = new Thread(net);
        service.setDaemon(true);
        service.start();
    }
}
