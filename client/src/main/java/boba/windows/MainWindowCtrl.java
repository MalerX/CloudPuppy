package boba.windows;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class MainWindowCtrl {
    private static final Logger log = Logger.getLogger(MainWindowCtrl.class);

    @FXML
    public ListView<String> localFiles;
    @FXML
    public Button refreshLocal;
    @FXML
    public Button mkdirLocal;
    @FXML
    public Button backLocal;
    @FXML
    public Button forwardLocal;

    private Stage mainWindow;
    private File lastDir;
    private File rootDir;
    private File currentDir;

    private BlockingQueue<byte[]> outQueue;
    private BlockingQueue<byte[]> inQueue;

    public void setQueue(BlockingQueue<byte[]> outQueue, BlockingQueue<byte[]> inQueue) {
        this.outQueue = outQueue;
        this.inQueue = inQueue;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = new File(rootDir);
        if (!this.rootDir.exists())
            this.rootDir.mkdir();
        currentDir = this.rootDir;
    }

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void refreshLocal(ActionEvent actionEvent) {
        refresh();
    }

    public void refresh() {
        localFiles.getItems().clear();

        List<File> filesInCurrDir = Arrays.asList(
                Objects.requireNonNull(currentDir.listFiles()));
//        Сначала папки, затем файлы.
        filesInCurrDir.sort((o1, o2) -> {
            if (o1.isDirectory() && o2.isFile())
                return -1;
            if (o1.isFile() && o2.isDirectory())
                return 1;
            return 0;
        });
        localFiles.getItems().add(currentDir.getPath());
        for (File o :
                filesInCurrDir) {
            localFiles.getItems().add(o.getName());
        }
    }

    public void mkDirLocal(ActionEvent actionEvent) {
        String nameDir = getNameNewDir();
        if (nameDir == null ||
                Files.exists(Paths.get(currentDir.getPath(), nameDir))) {
            log.info(String.format("Operation aborted or directory with name %s in %s already exists.",
                    nameDir, currentDir.getPath()));
            return;
        }
        try {
            Files.createDirectory(Paths.get(currentDir.getPath(), nameDir));
            log.info(String.format("Create directory %s in %s success.",
                    nameDir, currentDir.getPath()));
        } catch (IOException e) {
            log.error(String.format("Impossible create directory with name %s in %s",
                    nameDir, currentDir.getPath()));
        }
        refresh();
    }

    private String getNameNewDir() {
        TextInputDialog dialog = new TextInputDialog("Новая папка");
        dialog.setTitle("Create new directory.");
        dialog.setHeaderText("Создание новой папки.");
        dialog.setContentText("Введите имя новой папки:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public void backCloud(ActionEvent actionEvent) {
        if (currentDir == rootDir)
            return;
        File tmpFile = lastDir;
        lastDir = currentDir;
        currentDir = tmpFile;
        refresh();
        log.info(String.format("Transition to folder %s", currentDir.getName()));
    }

    public void joinLocalDir(ActionEvent actionEvent) {
        File tmp = new File(Paths.get(currentDir.toString(), getItem(localFiles)).toString());
        if (!tmp.isDirectory())
            return;
        lastDir = currentDir;
        currentDir = tmp;
        refresh();
        log.info(String.format("Transition to folder %s", currentDir.getName()));
    }

    private String getItem(ListView<String> listView) {
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        return listView.getSelectionModel().getSelectedItems().get(0);
    }
}