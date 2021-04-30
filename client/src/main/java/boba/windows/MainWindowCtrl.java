package boba.windows;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainWindowCtrl {
    public Button backCloud;
    private Stage mainWindow;

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void backCloud(ActionEvent actionEvent) {
        System.out.println("Back");
    }
}
