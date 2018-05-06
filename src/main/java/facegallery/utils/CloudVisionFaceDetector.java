package facegallery.utils;

import com.google.protobuf.ByteString;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.common.primitives.Bytes;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.FaceAnnotation;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.google.api.gax.rpc.ApiException;

// https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/vision/face-detection/src/main/java/com/google/cloud/vision/samples/facedetect/FaceDetectApp.java

public class CloudVisionFaceDetector {
	public static boolean imageHasFace(BufferedImage bufferedImage) throws ApiException, IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", stream);
		stream.flush();
		Image image = Image.newBuilder().setContent(ByteString.copyFrom(stream.toByteArray())).build();
		stream.close();
        
        return CloudVisionFaceDetector.imageHasFace(image);
    }
	
	public static boolean imageHasFace(ByteArray byteArray) throws ApiException, IOException {
		return CloudVisionFaceDetector.imageHasFace(byteArray.getBytes());
	}
	
	public static boolean imageHasFace(byte[] bytes) throws ApiException, IOException {
		Image image = Image.newBuilder().setContent(ByteString.copyFrom(bytes)).build();
		return CloudVisionFaceDetector.imageHasFace(image);
	}
	
    public static boolean imageHasFace(File file) throws ApiException, IOException {
        ByteString imgBytes = ByteString.readFrom(new FileInputStream(file));
        Image image = Image.newBuilder().setContent(imgBytes).build();
        
        return CloudVisionFaceDetector.imageHasFace(image);
    }
    
    public static boolean imageHasFace(Image image) throws ApiException, IOException {  
    	List<AnnotateImageRequest> requests = new ArrayList<>();

        Feature feat = Feature.newBuilder().setType(Type.FACE_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(image).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            AnnotateImageResponse response = client.batchAnnotateImages(requests).getResponsesList().get(0);
            return response.getFaceAnnotationsCount() > 0;
        }
    }
}
