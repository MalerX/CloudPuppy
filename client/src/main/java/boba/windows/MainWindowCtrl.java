package boba.windows;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import lupa.Navigator;
import lupa.SignalBytes;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

import static lupa.Navigator.DELIMETR;
import static lupa.SignalBytes.REFRESH;

public class MainWindowCtrl {
    private static final Logger log = Logger.getLogger(MainWindowCtrl.class);

    @FXML
    public ListView<String> localFiles;
    @FXML
    public Button refreshLocalBtn;
    @FXML
    public Button mkdirLocalBtn;
    @FXML
    public Button forwardLocalBtn;
    @FXML
    public Button upDirLocalBtn;
    @FXML
    public Button backLocalBtn;
    @FXML
    public Button rmDirLocalBtn;
    @FXML
    public Button refreshCloudBtn;
    @FXML
    public ListView<String> cloudFiles;
    @FXML
    public Button mkdirCloudBtn;

    private Stage mainWindow;

    private Navigator navigator;

    private BlockingQueue<byte[]> outQueue;
    private BlockingQueue<byte[]> inQueue;

    public void setQueue(BlockingQueue<byte[]> outQueue, BlockingQueue<byte[]> inQueue) {
        this.outQueue = outQueue;
        this.inQueue = inQueue;
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
        refreshLC();
    }

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void refreshLocal(ActionEvent actionEvent) {
        refreshLC();
    }

    private void refreshLC() {
        Platform.runLater(() -> {
            localFiles.getItems().clear();
            String[] inLocalDir = navigator.refresh().split(DELIMETR);
            for (String str :
                    inLocalDir) {
                localFiles.getItems().add(str);
            }
            log.info(String.format("Successfully refreshLC directory %s", inLocalDir[0]));
        });
    }

    public void mkDirLocal(ActionEvent actionEvent) {
        navigator.mkDir(getNameNewDir());
        refreshLC();
    }

    private String getNameNewDir() {
        TextInputDialog dialog = new TextInputDialog("Новая папка");
        dialog.setTitle("Create new directory.");
        dialog.setHeaderText("Создание новой папки.");
        dialog.setContentText("Введите имя новой папки:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public void backLocal(ActionEvent actionEvent) {
        navigator.back();
        refreshLC();
    }

    public void joinLocalDir(ActionEvent actionEvent) {
        navigator.joinDir(localFiles.getSelectionModel().getSelectedItem());
        refreshLC();
    }

    public void upDirLocal(ActionEvent actionEvent) {
        navigator.upDir();
        refreshLC();
    }

    public void rmLocalItem(ActionEvent actionEvent) {
        navigator.rmItem(localFiles.getSelectionModel().getSelectedItem());
        refreshLC();
    }

    public void refreshCloud(ActionEvent actionEvent) {
        refreshCL();
    }

    private void refreshCL() {
        Platform.runLater(() -> {
            byte[] answer = null;
            outQueue.add(new byte[]{REFRESH});
            try {
                answer = inQueue.take();
            } catch (InterruptedException e) {
                log.error("Error read inQueue.");
            }
            if (answer != null) {
                String[] inCloudDir = new String(answer).split(DELIMETR);
                cloudFiles.getItems().clear();
                for (String str :
                        inCloudDir) {
                    cloudFiles.getItems().add(str);
                }
                log.info(String.format("Successfully refreshLC remote directory %s", inCloudDir[0]));
            }
        });
    }

    public void mkDirCloud(ActionEvent actionEvent) {
        String newDirName = getNameNewDir();
        ByteBuffer buffer = ByteBuffer.allocate(SignalBytes.)
        outQueue.add();
        refreshCL();
    }
}