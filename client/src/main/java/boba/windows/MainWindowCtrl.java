package boba.windows;

import boba.network.NettyNetwork;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowCtrl implements Initializable {
    private Stage mainWindow;
    private String workDir;

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}