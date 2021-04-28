package boba.windows;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthWindow {
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


    public void getAuthentication(ActionEvent actionEvent) {

    }

    public void getRegistration(ActionEvent actionEvent) {

    }
}
