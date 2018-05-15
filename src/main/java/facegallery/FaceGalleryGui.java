package facegallery;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import java.nio.file.Paths;

public class FaceGalleryGui extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        
        Parent root = FXMLLoader.load(Paths.get("/Users/aneesh/IdeaProjects/A1_SE751_G27/src/main/java/facegallery/guifxml.fxml").toUri().toURL());
        primaryStage.setTitle("Face-detection Gallery");
        primaryStage.setScene(new Scene(root, 661, 441));
        primaryStage.show();
        primaryStage.setResizable(false);
    }


    
}