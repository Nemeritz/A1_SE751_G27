package facegallery;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import facegallery.utils.MyImageView;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class FaceGalleryController implements Initializable {
    @FXML
    private ScrollPane imageSlider;
    @FXML
    private StackPane imageStackPane;
    @FXML
    private Button parallel;
    @FXML
    private Button sequential;
    @FXML
    private Button detectButton;
    @FXML
    private void handleButtonAction(Event event) {
            if(event.getSource()==parallel)
                System.out.println("Detect");
            if(event.getSource()==sequential)
                System.out.println("sequential");
            if(event.getSource()==detectButton)
                System.out.println("detect faces");
    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File folder = new File("/Users/aneesh/Images");
        File[] listOfFiles = folder.listFiles();
        TilePane tilePane = new TilePane();
        tilePane.setStyle("-fx-background-color : black");
        tilePane.setHgap(5);
        tilePane.setVgap(5);
        tilePane.setPrefTileHeight(100);

        tilePane.setPrefHeight(100);
        tilePane.setMinSize(661,100);
        for(int i=0 ; i < listOfFiles.length ; i++){
            Image img = new Image(listOfFiles[i].toURI().toString());
            MyImageView imageView = new MyImageView(img);
            imageView.setImagePath(listOfFiles[i].toURI().toString());
            imageView.setFitHeight(100);
            imageView.setFitWidth(100);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-opacity : 0.5");
            tilePane.getChildren().add(imageView);
            addEventToImageView(imageView);


        }
        imageSlider.setContent(tilePane);


    }



    public void addEventToImageView(ImageView img){
        img.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MyImageView imageView = (MyImageView) event.getSource();
                Image img = new Image(imageView.getImagePath());
                ImageView mainImageView = new ImageView(img);
                mainImageView.setPreserveRatio(true);
                mainImageView.setFitWidth(imageStackPane.getWidth());
                mainImageView.setFitHeight(imageStackPane.getHeight());
                imageStackPane.getChildren().clear();
                imageStackPane.getChildren().add(mainImageView);
                event.consume();
            }
        });
    }

}
