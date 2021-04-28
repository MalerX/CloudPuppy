import boba.windows.AuthWindow;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader primaryFXMLLoader = new FXMLLoader(getClass().getResource("authWindow.fxml"));
        Parent root = primaryFXMLLoader.load();
        primaryStage.setTitle("Вход в Cloud Puppy");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
