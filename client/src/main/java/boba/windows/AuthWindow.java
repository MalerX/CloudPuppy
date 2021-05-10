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
import lupa.Navigator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static lupa.SignalBytes.*;

public class AuthWindow {
    private final static Logger log = Logger.getLogger(AuthWindow.class);

    private Stage authWindow;

    @FXML
    public TextField serverAddress;
    @FXML
    public TextField rootDir;
    @FXML
    public TextField port;
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
    public PasswordField retryRegPass;

    private Network net = null;
    private final BlockingQueue<byte[]> outQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<byte[]> inQueue = new LinkedBlockingQueue<>();

    public void setAuthWindow(Stage authWindow) {
        this.authWindow = authWindow;
    }

    public void getAuthentication(ActionEvent actionEvent) throws IOException, InterruptedException {
        if (net == null)
            connect();

        ByteBuffer outMsg = buildAuthData(
                AUTH,
                loginField.getText(),
                passField.getText());

        if (outMsg == null)
            return;
        outQueue.add(outMsg.array());
        log.info("Auth data add in queue send.");

        byte[] answer = inQueue.take();

        if (answer[0] == AUTH_OK)
            authOk();

        if (answer[0] == AUTH_FAIL)
            errorAuth();
    }

    private ByteBuffer buildAuthData(byte signal, String login, String password) {
        if (login.contains(" ") || password.contains(" "))
            ContainsSpace();
        else {
            ByteBuffer result = ByteBuffer.allocate(LENGTH_SIG_BYTE
                    + LENGTH_INT
                    + login.getBytes(StandardCharsets.UTF_8).length
                    + LENGTH_INT
                    + password.getBytes(StandardCharsets.UTF_8).length
            );
            return result.put(signal)
                    .put(ByteBuffer.allocate(LENGTH_INT).putInt(login.length()).array())
                    .put(login.getBytes(StandardCharsets.UTF_8))
                    .put(ByteBuffer.allocate(LENGTH_INT).putInt(password.length()).array())
                    .put(password.getBytes(StandardCharsets.UTF_8))
                    .flip();
        }
        return null;
    }

    public void getRegistration(ActionEvent actionEvent) throws InterruptedException {
        if (net == null)
            connect();

        if (!regPass.getText().equals(retryRegPass.getText())) {
            notMatchPass();
            return;
        }
        ByteBuffer outMsg = buildAuthData(REG,
                regLogin.getText(),
                regPass.getText());

        if (outMsg == null)
            return;
        outQueue.add(outMsg.array());
        log.info("Registration data add in queue send.");
        byte[] answer = inQueue.take();
        log.info("Server response received");
        if (answer[0] == REG_OK)
            regOk();
        if (answer[0] == REG_FAIL)
            regFail();
    }

    private void regFail() {
        Alert regFail = new Alert(Alert.AlertType.ERROR);
        regFail.setTitle("Oop...");
        regFail.setHeaderText("Что-то пошло не так...");
        regFail.setContentText("Регистрация не выполнена.\n" +
                "Обратитесь в службу поддержки.");
        regFail.showAndWait();
        log.info("Registration fail.");
    }

    private void regOk() {
        Alert regOk = new Alert(Alert.AlertType.CONFIRMATION);
        regOk.setTitle("Ок!");
        regOk.setHeaderText("Поздравляем!");
        regOk.setContentText("Регистрация прошла успешно.\n" +
                "Теперь войдите под своими учётными данными.");
        regOk.showAndWait();
        log.info("Registration successfully.");
    }

    private void notMatchPass() {
        Alert wrongFormatData = new Alert(Alert.AlertType.WARNING);
        wrongFormatData.setTitle("Не верные данные.");
        wrongFormatData.setHeaderText("Проверьте введённые данные.");
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
        mainWindow.setScene(new Scene(root));
        mainWindow.setTitle("Cloud Puppy");
        mainWindow.setResizable(false);

        mainWindowCtrl.setNavigator(new Navigator(rootDir.getText()));
        mainWindowCtrl.setQueue(outQueue, inQueue);

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

    public void connect() {
        net = new Network(serverAddress.getText(), Integer.parseInt(port.getText()),
                outQueue, inQueue);
        Thread service = new Thread(net);
        service.setDaemon(true);
        service.start();
    }
}
