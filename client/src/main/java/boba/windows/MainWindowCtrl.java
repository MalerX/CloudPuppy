package boba.windows;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;

public class MainWindowCtrl implements Initializable {
    private Stage mainWindow;
    private String workDir;

    private BlockingQueue<byte[]> outQueue;
    private BlockingQueue<byte[]> inQueue;

    public void setQueue(BlockingQueue<byte[]> outQueue, BlockingQueue<byte[]> inQueue) {
        this.outQueue = outQueue;
        this.inQueue = inQueue;
    }

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void backCloud(ActionEvent actionEvent) {

    }
}