package facegallery.gui;

import apt.annotations.Future;
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

public class FaceGalleryGui extends JFrame {

    static private ScrollPane scrollPane = new ScrollPane();
    static private JPanel imageGrid = new JPanel();
    static private JButton parallel,sequential,concurrent,pipeline,reset;
    static private JPanel controls = new JPanel();
    static private JPanel controlPanel = new JPanel();

    static Dimension d;
    static int currentWidth, currentHeight;
    static JLabel labelTextImages;
    static JLabel labelTextFaces;
    static JLabel time;
    static JLabel timeFaces;
    static JLabel labelTextThumb;
    static JLabel labelTextBlur;
    static JLabel timeThumb;
    static JLabel timeBlur;
    static JLabel labelTotalTime;
    static JLabel totalTime;
    static JLabel currentAction;
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
            ParallelTasker tasker = new ParallelTasker();

            @Future
            Void t = tasker.performParallel(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages);
        }
    }

    private class sequentialListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            Tasker tasker = new Tasker();
            Void t0 = tasker.performSequential(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages, true);
        }
    }

    private class concurrentListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            Tasker tasker = new Tasker();

            @Future
            Void t1 = tasker.performConcurrent(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages, true);
        }
    }

    private class pipelineListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            Tasker tasker = new Tasker();
            //tasker.performParallelPipeline(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages, false);
        }
    }


    private class resetListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            time.setText("");
            timeFaces.setText("");
            timeThumb.setText("");
            timeBlur.setText("");
            totalTime.setText("");
            imageGrid.removeAll();
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

        labelTextImages = new JLabel("time taken to load Images:");
        labelTextFaces = new JLabel("time taken to detect Faces:");
        time = new JLabel("   ");
        timeFaces = new JLabel("  ");
        timeThumb = new JLabel("  ");
        timeBlur = new JLabel("  ");
        totalTime = new JLabel("  ");
        labelTextThumb = new JLabel("time taken to generate Thumbnails:");
        labelTextBlur = new JLabel("time taken to distort Images:");
        labelTotalTime = new JLabel("Total time:");
        currentAction = new JLabel("");
        imageGrid.setLayout(new GridLayout(0,4,5,5));
        sequential = new JButton("sequential");
        concurrent = new JButton("concurrent");
        parallel = new JButton("parallel");
        pipeline = new JButton("pipeline");
        reset = new JButton("reset");
        parallel.addActionListener(new parallelListener());
        sequential.addActionListener(new sequentialListener());
        concurrent.addActionListener(new concurrentListener());
        pipeline.addActionListener(new pipelineListener());
        reset.addActionListener(new resetListener());
        controls.setLayout(new BoxLayout(controls, BoxLayout.PAGE_AXIS));
        controls.add(new JLabel("Detect Faces in:"),BorderLayout.CENTER);
        controls.add(parallel);
        controls.add(sequential);
        controls.add(concurrent);
        controls.add(pipeline);
        controls.add(reset);
        controls.add(labelTextImages);
        controls.add(time);
        controls.add(labelTextThumb);
        controls.add(timeThumb);
        controls.add(labelTextFaces);
        controls.add(timeFaces);
        controls.add(labelTextBlur);
        controls.add(timeBlur);
        controls.add(labelTotalTime);
        controls.add(totalTime);
        controls.add(currentAction);
        controlPanel.add(controls,BorderLayout.SOUTH);
        scrollPane.add(imageGrid);

        setLayout(new GridLayout(1,2));

        add(scrollPane);
        add(controlPanel);

        setSize(1280, 720);
        d = getSize();
        currentWidth = d.width;
        currentHeight = d.height;

        setLocationRelativeTo(null);
    }


    public static Void updateStats(TaskerStats stats) {
        time.setText(Double.toString(stats.fileReadStats.runtime));
        timeFaces.setText(Double.toString(stats.faceDetectionStats.runtime));
        timeThumb.setText(Double.toString(stats.thumbnailGenerateStats.runtime));
        timeBlur.setText(Double.toString(stats.imageRescaleStats.runtime));
        totalTime.setText(Double.toString(stats.totalRuntime));

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