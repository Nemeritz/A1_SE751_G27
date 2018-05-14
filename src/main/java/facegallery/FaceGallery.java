package facegallery;

import apt.annotations.Future;
import facegallery.utils.ByteArray;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

public class FaceGallery {
	public static void main(String[] args) {
		ArrayList<ByteArray> fileBytes = new ArrayList<ByteArray>();		
		
		try {
			fileBytes.add(
						new ByteArray(
								Files.readAllBytes(Paths.get("C:\\Users\\lichk\\OneDrive\\Pictures\\Games\\Assassins Creed\\uPlay_PC_Wallpaper2_1680x1050.jpg"))
						)
					);
			fileBytes.add(
						new ByteArray(
								Files.readAllBytes(Paths.get("C:\\Users\\lichk\\Documents\\Git\\facedataset\\People\\10comm-decarlo.jpg"))
						)
					);
			fileBytes.add(
						new ByteArray(
								Files.readAllBytes(Paths.get("C:\\Users\\lichk\\Documents\\Git\\facedataset\\People\\1198_0_861.jpg"))
						)
					);
        } catch (IOException e) {
            // TODO: handle exception
        }

        List<AtomicBoolean> parallelDetections = FaceDetector.createParallelDetectionContainer(fileBytes.size());

		List<Boolean> sequentialDetections = FaceDetector.detectSequential(fileBytes);

		@Future
        Void v = FaceDetector.detectParallel(fileBytes, parallelDetections);

        System.out.println("Sequential as follows:");
        for (Boolean detection : sequentialDetections) {
            System.out.println(detection.toString());
        }

        System.out.println("Parallel as follows:");
        for (AtomicBoolean detection : parallelDetections) {
            System.out.println(detection.toString());
        }
	}
}
