package facegallery.gui;

import apt.annotations.Future;
import facegallery.tasks.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static facegallery.FaceGallery.TEST_DATASET_DIR;

public class FaceGalleryGui extends JFrame {

    static private ScrollPane scrollPane = new ScrollPane();
    static private JPanel imageGrid = new JPanel();
    static private JButton parallel,sequential,concurrent,pipeline;
    static private JPanel controls = new JPanel();
    static private JPanel controlPanel = new JPanel();
    static private ImageBytesReader files = new ImageBytesReader(TEST_DATASET_DIR);
    static int currentMode = 0;
    AtomicBoolean running = new AtomicBoolean(false);

    static Dimension d;
    static int currentWidth, currentHeight;
    static JLabel labelTextImages;
    static JLabel labelTextFaces;
    static JLabel timeParallelFiles;
    static JLabel timeSequentialFiles;
    static JLabel timeConcurrentFiles;
    static JLabel timePipelineFiles;
    static JLabel timeParallelThumb;
    static JLabel timePipelineThumb;
    static JLabel timeSequentialThumb;
    static JLabel timeConcurrentThumb;
    static JLabel timeParallelDetect;
    static JLabel timeSequentialDetect;
    static JLabel timeConcurrentDetect;
    static JLabel timePipelineDetect;
    static JLabel timeParallelDistort;
    static JLabel timeSequentialDistort;
    static JLabel timeConcurrentDistort;
    static JLabel timePipelineDistort;
    static JLabel timeParallelTotal;
    static JLabel timeSequentialTotal;
    static JLabel timeConcurrentTotal;
    static JLabel timePipelineTotal;
    static JLabel labelTextThumb;
    static JLabel labelTextBlur;
    static JLabel labelTotalTime;
    static JLabel currentAction;
    static JProgressBar progressBar, filesBar, thumbBar, detectBar, distortBar;
    static JCheckBox batchFlag;
    static BufferedImage loadingImage;

    private Void setRunning(boolean val) {
        running.set(val);
        return null;
    }

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
            if (!running.get()) {
                setRunning(true);

                currentMode = 3;
                ParallelTasker tasker = new ParallelTasker();

                @Future
                Void t = tasker.performParallel(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages);

                @Future(depends="t")
                Void r = setRunning(false);
            }
        }
    }

    private class sequentialListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!running.get()) {
                setRunning(true);

                currentMode = 1;
                Tasker tasker = new Tasker();
                Void t = tasker.performSequential(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages, batchFlag.isSelected());

                setRunning(false);
            }
        }
    }

    private class concurrentListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!running.get()) {
                setRunning(true);

                currentMode = 2;
                Tasker tasker = new Tasker();

                @Future
                Void t = tasker.performConcurrent(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages, batchFlag.isSelected());

                @Future(depends="t")
                Void r = setRunning(false);
            }
        }
    }

    private class pipelineListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!running.get()) {
                setRunning(true);

                currentMode = 4;
                ParallelPipelineTasker tasker = new ParallelPipelineTasker();

                @Future
                Void t = tasker.performParallel(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages);

                @Future(depends="t")
                Void r = setRunning(false);
            }
        }
    }

//    private class resetListener implements ActionListener{
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            if (!running.get()) {
//                setRunning(true);
//                imageGrid.removeAll();
//                imageGrid.revalidate();
//                imageGrid.repaint();
//                progressBar.setValue(0);
//                filesBar.setValue(0);
//                thumbBar.setValue(0);
//                distortBar.setValue(0);
//                detectBar.setValue(0);
//                setRunning(false);
//            }
//        }
//    }

    public FaceGalleryGui() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Gallery app");

        try {
            loadingImage = ImageIO.read(this.getClass().getResourceAsStream("loading.png"));
        } catch (IOException ignore) {
            System.err.println("Error loading loading image");
        }

        batchFlag = new JCheckBox("Enable Batch");
        progressBar = new JProgressBar(0, files.getFileLength() * 4);
        progressBar.setStringPainted(true);
        filesBar = new JProgressBar(0, files.getFileLength());
        filesBar.setStringPainted(true);
        detectBar = new JProgressBar(0, files.getFileLength());
        detectBar.setStringPainted(true);
        distortBar = new JProgressBar(0, files.getFileLength());
        distortBar.setStringPainted(true);
        thumbBar = new JProgressBar(0, files.getFileLength());
        thumbBar.setStringPainted(true);
        labelTextImages = new JLabel("File Reading :");
        labelTextFaces = new JLabel("Face Detection :");
        timeParallelFiles = new JLabel("  ");
        timeSequentialFiles = new JLabel("  ");
        timeConcurrentFiles = new JLabel("  ");
        timePipelineFiles = new JLabel("  ");
        timeParallelThumb = new JLabel("  ");
        timeSequentialThumb = new JLabel("  ");
        timeConcurrentThumb = new JLabel("  ");
        timePipelineThumb = new JLabel("  ");
        timeParallelDetect = new JLabel("  ");
        timeSequentialDetect = new JLabel("  ");
        timeConcurrentDetect = new JLabel("  ");
        timePipelineDetect = new JLabel("  ");
        timeParallelDistort = new JLabel("  ");
        timeSequentialDistort = new JLabel("  ");
        timeConcurrentDistort = new JLabel("  ");
        timePipelineDistort = new JLabel("  ");
        timeParallelTotal = new JLabel("  ");
        timeSequentialTotal = new JLabel("  ");
        timeConcurrentTotal = new JLabel("  ");
        timePipelineTotal = new JLabel("  ");
        labelTextThumb = new JLabel("Thumbnail:");
        labelTextBlur = new JLabel("Blur & Darken:");
        labelTotalTime = new JLabel("Total time:");
        currentAction = new JLabel("");
        imageGrid.setLayout(new GridLayout(0,4,5,5));
        sequential = new JButton("sequential");
        concurrent = new JButton("concurrent");
        parallel = new JButton("parallel");
        pipeline = new JButton("pipeline");
        parallel.addActionListener(new parallelListener());
        sequential.addActionListener(new sequentialListener());
        concurrent.addActionListener(new concurrentListener());
        pipeline.addActionListener(new pipelineListener());
        controls.setLayout(new GridLayout(0,5,5,5));
        controls.add(new JLabel("         "));
        controls.add(new JLabel("Sequential:"));
        controls.add(new JLabel("Concurrent:"));
        controls.add(new JLabel("Parallel:"));
        controls.add(new JLabel("Pipeline:"));
        controls.add(labelTextImages);
        controls.add(timeSequentialFiles);
        controls.add(timeConcurrentFiles);
        controls.add(timeParallelFiles);
        controls.add(timePipelineFiles);
        controls.add(labelTextThumb);
        controls.add(timeSequentialThumb);
        controls.add(timeConcurrentThumb);
        controls.add(timeParallelThumb);
        controls.add(timePipelineThumb);
        controls.add(labelTextFaces);
        controls.add(timeSequentialDetect);
        controls.add(timeConcurrentDetect);
        controls.add(timeParallelDetect);
        controls.add(timePipelineDetect);
        controls.add(labelTextBlur);
        controls.add(timeSequentialDistort);
        controls.add(timeConcurrentDistort);
        controls.add(timeParallelDistort);
        controls.add(timePipelineDistort);
        controls.add(labelTotalTime);
        controls.add(timeSequentialTotal);
        controls.add(timeConcurrentTotal);
        controls.add(timeParallelTotal);
        controls.add(timePipelineTotal);
//        controls.add(currentAction);
        controls.add(new JLabel("  "));
        controls.add(sequential);
        controls.add(concurrent);
        controls.add(parallel);
        controls.add(pipeline);
        controls.add(new JLabel("File Progress: "));
        controls.add(new JLabel("Thumbnail Progress: "));
        controls.add(new JLabel("Detection Progress: "));
        controls.add(new JLabel("Blur & Darken Progress: "));
        controls.add(new JLabel("Total Progress: "));        
        controls.add(filesBar);
        controls.add(thumbBar);
        controls.add(detectBar);
        controls.add(distortBar);
        controls.add(progressBar);   
        controls.add(new JLabel("                "));
        controls.add(new JLabel("                "));
        controls.add(new JLabel("                "));
        controls.add(new JLabel("                "));
        controls.add(batchFlag);
        controlPanel.add(controls,BorderLayout.SOUTH);
        scrollPane.add(imageGrid);

        setLayout(new GridLayout(1,2));

        add(scrollPane);
        add(controlPanel);

        setSize(1880, 720);
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
                timePipelineFiles.setText(Double.toString(stats.fileReadStats.runtime));
                timePipelineThumb.setText(Double.toString(stats.thumbnailGenerateStats.runtime));
                timePipelineDetect.setText(Double.toString(stats.faceDetectionStats.runtime));
                timePipelineDistort.setText(Double.toString(stats.imageRescaleStats.runtime));
                timePipelineTotal.setText(Double.toString(stats.totalRuntime));
                break;
        }
        progressBar.setValue(stats.faceDetectionStats.taskProgress + stats.imageRescaleStats.taskProgress + stats.thumbnailGenerateStats.taskProgress + stats.fileReadStats.taskProgress);
        detectBar.setValue(stats.faceDetectionStats.taskProgress);
        filesBar.setValue(stats.fileReadStats.taskProgress);
        thumbBar.setValue(stats.thumbnailGenerateStats.taskProgress);
        distortBar.setValue(stats.imageRescaleStats.taskProgress);
        setProgressBarMax(stats.fileReadStats.taskTotal);
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

    private static void setProgressBarMax(int total) {
        progressBar.setMaximum(total * 4);
        filesBar.setMaximum(total);
        detectBar.setMaximum(total);
        distortBar.setMaximum(total);
        thumbBar.setMaximum(total);
    }
}