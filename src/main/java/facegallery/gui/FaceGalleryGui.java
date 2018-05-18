package facegallery.gui;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class FaceGalleryGui extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("FaceGalleryGui.fxml"));
            primaryStage.setTitle("Face-detection Gallery");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.show();
            primaryStage.setResizable(true);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}