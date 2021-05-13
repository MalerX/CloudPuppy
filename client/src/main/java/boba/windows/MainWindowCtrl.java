package boba.windows;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import lupa.Navigator;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

import static lupa.Navigator.DELIMITER;
import static lupa.SignalBytes.*;

public class MainWindowCtrl {
    private static final Logger log = Logger.getLogger(MainWindowCtrl.class);

    @FXML
    public ListView<String> localFiles;
    @FXML
    public Button refreshLocalBtn;
    @FXML
    public Button mkdirLocalBtn;
    @FXML
    public Button joinLocalBtn;
    @FXML
    public Button upDirLocalBtn;
    @FXML
    public Button backLocalBtn;
    @FXML
    public Button rmItemLocalBtn;
    @FXML
    public Button refreshCloudBtn;
    @FXML
    public ListView<String> cloudFiles;
    @FXML
    public Button mkdirCloudBtn;
    @FXML
    public Button backCloudBtn;
    @FXML
    public Button joinCloudBtn;
    @FXML
    public Button upDirCloudBtn;
    @FXML
    public Button rmItemCloudBtn;

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
        refreshCL();
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
            String[] inLocalDir = navigator.refresh().split(DELIMITER);
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
                String[] inCloudDir = new String(answer).split(DELIMITER);
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
        String nameDir = getNameNewDir();
        byte[] requestMkdir = buildName(MKDIR, nameDir);
        outQueue.add(requestMkdir);
        log.info("A request has been sent to create a directory.");
        refreshCL();
    }

    private byte[] buildName(byte signal, String name) {
        byte[] nameByte = name.getBytes(StandardCharsets.UTF_8);
        ByteBuffer request = ByteBuffer.allocate(LENGTH_SIG_BYTE
                + LENGTH_INT
                + nameByte.length);
        request.put(signal)
                .put(ByteBuffer.allocate(LENGTH_INT).putInt(nameByte.length).array())
                .put(nameByte)
                .flip();
        return request.array();
    }

    public void backCloud(ActionEvent actionEvent) {
        outQueue.add(new byte[]{BACK});
        log.info("A request to return to the previous directory has been sent.");
        refreshCL();
    }

    public void joinCloudDir(ActionEvent actionEvent) {
        String nameDir = cloudFiles.getSelectionModel().getSelectedItem();
        byte[] requestJoinDir = buildName(JOIN, nameDir);
        outQueue.add(requestJoinDir);
        log.info("The request to switch to the directory has been sent.");
        refreshCL();
    }

    public void udDirCloud(ActionEvent actionEvent) {
        outQueue.add(new byte[]{UP});
        log.info("A request was sent to switch to the parent directory.");
        refreshCL();
    }

    public void rmCloudItem(ActionEvent actionEvent) {
        String nameItem = cloudFiles.getSelectionModel().getSelectedItem();
        byte[] requestRmItem = buildName(RM, nameItem);
        outQueue.add(requestRmItem);
        log.info("A request was sent to delete an item.");
        refreshCL();
    }
}