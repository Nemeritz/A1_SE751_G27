package facegallery.gui;

import apt.annotations.Future;
import facegallery.tasks.ImageBytesReader;
import facegallery.tasks.ParallelTasker;
import facegallery.tasks.Tasker;
import facegallery.tasks.TaskerStats;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static facegallery.FaceGallery.TEST_DATASET_DIR;

public class FaceGalleryGui extends JFrame {

    static private ScrollPane scrollPane = new ScrollPane();
    static private JPanel imageGrid = new JPanel();
    static private JButton parallel,sequential,concurrent,reset;
    static private JPanel controls = new JPanel();
    static private JPanel controlPanel = new JPanel();
    static private ImageBytesReader files = new ImageBytesReader(TEST_DATASET_DIR);
    static int currentMode = 0;

    static Dimension d;
    static int currentWidth, currentHeight;
    static JLabel labelTextImages;
    static JLabel labelTextFaces;
    static JLabel timeParallelFiles;
    static JLabel timeSequentialFiles;
    static JLabel timeConcurrentFiles;
    static JLabel timeParallelThumb;
    static JLabel timeSequentialThumb;
    static JLabel timeConcurrentThumb;
    static JLabel timeParallelDetect;
    static JLabel timeSequentialDetect;
    static JLabel timeConcurrentDetect;
    static JLabel timeParallelDistort;
    static JLabel timeSequentialDistort;
    static JLabel timeConcurrentDistort;
    static JLabel timeParallelTotal;
    static JLabel timeSequentialTotal;
    static JLabel timeConcurrentTotal;
    static JLabel labelTextThumb;
    static JLabel labelTextBlur;
    static JLabel labelTotalTime;
    static JLabel currentAction;
    static JProgressBar progressBar;
    static BufferedImage loadingImage;
    static Lock delayLock = new ReentrantLock();


    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                FaceGalleryGui app = new FaceGalleryGui();
                app.setVisible(true);
            }
        });
    }
    private class parallelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            currentMode = 3;
            ParallelTasker tasker = new ParallelTasker();

            @Future
            Void t = tasker.performParallel(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages);
        }
    }

    private class sequentialListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            currentMode = 1;
            Tasker tasker = new Tasker();
            Void t0 = tasker.performSequential(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages, false);
        }
    }

    private class concurrentListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            currentMode = 2;
            Tasker tasker = new Tasker();

            @Future
            Void t1 = tasker.performConcurrent(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages, false);
        }
    }

    private class resetListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            imageGrid.removeAll();
            imageGrid.revalidate();
            imageGrid.repaint();
            progressBar.setValue(0);
        }
    }

    public FaceGalleryGui() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Gallery app");

        try {
            loadingImage = ImageIO.read(this.getClass().getResourceAsStream("loading.png"));
        } catch (IOException ignore) {
            System.err.println("Error loading loading image");
        }

        progressBar = new JProgressBar(0, files.getFileLength() * 4);
        progressBar.setStringPainted(true);
        labelTextImages = new JLabel("File Reading :");
        labelTextFaces = new JLabel("Face Detection :");
        timeParallelFiles = new JLabel("  ");
        timeSequentialFiles = new JLabel("  ");
        timeConcurrentFiles = new JLabel("  ");
        timeParallelThumb = new JLabel("  ");
        timeSequentialThumb = new JLabel("  ");
        timeConcurrentThumb = new JLabel("  ");
        timeParallelDetect = new JLabel("  ");
        timeSequentialDetect = new JLabel("  ");
        timeConcurrentDetect = new JLabel("  ");
        timeParallelDistort = new JLabel("  ");
        timeSequentialDistort = new JLabel("  ");
        timeConcurrentDistort = new JLabel("  ");
        timeParallelTotal = new JLabel("  ");
        timeSequentialTotal = new JLabel("  ");
        timeConcurrentTotal = new JLabel("  ");
        labelTextThumb = new JLabel("Thumbnail Generation :");
        labelTextBlur = new JLabel("Thumbnail Distortion :");
        labelTotalTime = new JLabel("Total time:");
        currentAction = new JLabel("");
        imageGrid.setLayout(new GridLayout(0,4,5,5));
        sequential = new JButton("sequential");
        concurrent = new JButton("concurrent");
        parallel = new JButton("parallel");
        reset = new JButton("reset");
        parallel.addActionListener(new parallelListener());
        sequential.addActionListener(new sequentialListener());
        concurrent.addActionListener(new concurrentListener());
        reset.addActionListener(new resetListener());
        controls.setLayout(new GridLayout(0,4,5,5));
        controls.add(new JLabel("         "));
        controls.add(new JLabel("Sequential:"));
        controls.add(new JLabel("Concurrent:"));
        controls.add(new JLabel("Parallel:"));
        controls.add(labelTextImages);
        controls.add(timeSequentialFiles);
        controls.add(timeConcurrentFiles);
        controls.add(timeParallelFiles);
        controls.add(labelTextThumb);
        controls.add(timeSequentialThumb);
        controls.add(timeConcurrentThumb);
        controls.add(timeParallelThumb);
        controls.add(labelTextFaces);
        controls.add(timeSequentialDetect);
        controls.add(timeConcurrentDetect);
        controls.add(timeParallelDetect);
        controls.add(labelTextBlur);
        controls.add(timeSequentialDistort);
        controls.add(timeConcurrentDistort);
        controls.add(timeParallelDistort);
        controls.add(labelTotalTime);
        controls.add(timeSequentialTotal);
        controls.add(timeConcurrentTotal);
        controls.add(timeParallelTotal);
//        controls.add(currentAction);
        controls.add(sequential);
        controls.add(concurrent);
        controls.add(parallel);
        controls.add(reset);
        controls.add(progressBar);
        controlPanel.add(controls,BorderLayout.SOUTH);
        scrollPane.add(imageGrid);

        setLayout(new GridLayout(1,2));

        add(scrollPane);
        add(controlPanel);

        setSize(1380, 720);
        d = getSize();
        currentWidth = d.width;
        currentHeight = d.height;

        setLocationRelativeTo(null);
    }


    public static Void updateStats(TaskerStats stats) {

        switch (currentMode) {
            case 0:
                break;
            case 1:
                timeSequentialFiles.setText(Double.toString(stats.fileReadStats.runtime));
                timeSequentialThumb.setText(Double.toString(stats.thumbnailGenerateStats.runtime));
                timeSequentialDetect.setText(Double.toString(stats.faceDetectionStats.runtime));
                timeSequentialDistort.setText(Double.toString(stats.imageRescaleStats.runtime));
                timeSequentialTotal.setText(Double.toString(stats.totalRuntime));
                break;
            case 2:
                timeConcurrentFiles.setText(Double.toString(stats.fileReadStats.runtime));
                timeConcurrentThumb.setText(Double.toString(stats.thumbnailGenerateStats.runtime));
                timeConcurrentDetect.setText(Double.toString(stats.faceDetectionStats.runtime));
                timeConcurrentDistort.setText(Double.toString(stats.imageRescaleStats.runtime));
                timeConcurrentTotal.setText(Double.toString(stats.totalRuntime));
                break;
            case 3:
                timeParallelFiles.setText(Double.toString(stats.fileReadStats.runtime));
                timeParallelThumb.setText(Double.toString(stats.thumbnailGenerateStats.runtime));
                timeParallelDetect.setText(Double.toString(stats.faceDetectionStats.runtime));
                timeParallelDistort.setText(Double.toString(stats.imageRescaleStats.runtime));
                timeParallelTotal.setText(Double.toString(stats.totalRuntime));
                break;
            case 4:
                break;
        }
        progressBar.setValue(stats.faceDetectionStats.taskProgress + stats.imageRescaleStats.taskProgress + stats.thumbnailGenerateStats.taskProgress + stats.fileReadStats.taskProgress);

        return null;
    }

    public static Void updateImages(List<BufferedImage> imageList) {
        imageGrid.removeAll();
        for (BufferedImage image : imageList) {
            BufferedImage setImage = image == null ? loadingImage : image;
            ImageIcon ii = new ImageIcon(setImage);
            JLabel imageLabel = new JLabel(ii);
            imageGrid.add(imageLabel);
        }

        return null;
    }
}