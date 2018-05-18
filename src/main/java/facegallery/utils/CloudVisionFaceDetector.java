package facegallery.utils;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/vision/face-detection/src/main/java/com/google/cloud/vision/samples/facedetect/FaceDetectApp.java

public class CloudVisionFaceDetector {
	public static boolean imageHasFace(ByteArray byteArray) throws ApiException, IOException {
		return CloudVisionFaceDetector.imageHasFace(Image.newBuilder().setContent(ByteString.copyFrom(byteArray.getBytes())).build());
	}

    public static Boolean[] imageHasFace(ByteArray[] byteArray) throws ApiException, IOException {
        Image[] images = new Image[byteArray.length];

	    for (int i = 0; i < byteArray.length; i++) {
            images[i] = Image.newBuilder().setContent(ByteString.copyFrom(byteArray[i].getBytes())).build();
        }

        return CloudVisionFaceDetector.imageHasFace(images);
    }

    public static Boolean imageHasFace(Image image) throws ApiException, IOException {
    	List<AnnotateImageRequest> requests = new ArrayList<>();

        Feature feat = Feature.newBuilder().setType(Type.FACE_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(image).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            AnnotateImageResponse response = client.batchAnnotateImages(requests).getResponsesList().get(0);
            return response.getFaceAnnotationsCount() > 0;
        }
    }

    public static Boolean[] imageHasFace(Image[] images) throws ApiException, IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        for (int i = 0; i < images.length; i++) {
            Feature feat = Feature.newBuilder().setType(Type.FACE_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(images[i]).build();
            requests.add(request);
        }

        Boolean detections[] = new Boolean[images.length];
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            List<AnnotateImageResponse> responses = client.batchAnnotateImages(requests).getResponsesList();
            for (int i = 0; i < images.length; i++) {
                detections[i] = responses.get(i).getFaceAnnotationsCount() > 0;
            }
        }
        return detections;
    }
}
