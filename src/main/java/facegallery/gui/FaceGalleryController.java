package facegallery.gui;

import apt.annotations.Future;
import facegallery.FaceDetector;
import facegallery.FaceGallery;
import facegallery.ImageBytesReader;
import facegallery.utils.ByteArray;
import facegallery.utils.MyImageView;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Text;

import java.io.File;

public class FaceGalleryController {
    @FXML
    private ScrollPane imageSlider;
    @FXML
    private StackPane imageStackPane;
    @FXML
    private ToggleGroup runMode;
    @FXML
    private Button run;
    @FXML
    private Text currentAction;
    @FXML
    private Text imagesLoadedTime;
    @FXML
    private Text faceDetectionTime;
    @FXML
    private void runClicked(Event event) {
        switch(runMode.getSelectedToggle().getUserData().toString()) {
            case "sequential":
                @Future
                Boolean sync = runSequential();
                break;
            case "parallel":
                currentAction.setText("Idle");
                break;
            default:
                break;
        }
    }

    private ImageBytesReader imageBytesReader;
    private FaceDetector faceDetector;

    public FaceGalleryController() {
        imageBytesReader = new ImageBytesReader(FaceGallery.TEST_DATASET_DIR);
        faceDetector = new FaceDetector(imageBytesReader.getImageBytes());
    }

    public Boolean runSequential() {
        currentAction.setText("Reading files...");

        long imageReadStartTime = System.currentTimeMillis();
        @Future
        ByteArray[] imageBytes = imageBytesReader.run();
        long imageReadEndTime = System.currentTimeMillis();

        imagesLoadedTime.setText(Double.toString((double)(imageReadEndTime - imageReadStartTime) / 1000));

        currentAction.setText("Detecting faces...");
        long faceDetectStartTime = System.currentTimeMillis();
        Boolean[] detections = faceDetector.run(true);
        long faceDetectEndTime = System.currentTimeMillis();

        faceDetectionTime.setText(Double.toString((double)(faceDetectEndTime - faceDetectStartTime) / 1000));

        currentAction.setText("Done!");
        System.out.println("Done");
        return true;
    }

    public void loadFiles() {
        File[] fileList = imageBytesReader.getFileList();
        TilePane tilePane = new TilePane();
        tilePane.setStyle("-fx-background-color : black");
        tilePane.setHgap(5);
        tilePane.setVgap(5);
        tilePane.setPrefTileHeight(100);

        tilePane.setPrefHeight(100);
        tilePane.setMinSize(661,100);
        for(int i=0 ; i < fileList.length ; i++){
            Image img = new Image(fileList[i].toURI().toString());
            MyImageView imageView = new MyImageView(img);
            imageView.setImagePath(fileList[i].toURI().toString());
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
