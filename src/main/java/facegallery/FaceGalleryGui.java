package facegallery;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FaceGalleryGui extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("guifxml.fxml"));
        primaryStage.setTitle("Face-detection Gallery");
        primaryStage.setScene(new Scene(root, 661, 441));
        primaryStage.show();
        primaryStage.setResizable(false);
    }


    
}