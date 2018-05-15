package facegallery.utils;
import javafx.scene.image.*;


public class MyImageView extends ImageView {
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    private String imagePath;

    public MyImageView(Image image)
    {
        setImage(image);

    }
}