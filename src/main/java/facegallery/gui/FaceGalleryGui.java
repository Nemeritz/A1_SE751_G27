package facegallery.gui;
import facegallery.FaceGallery;

import apt.annotations.Future;
import facegallery.tasks.FaceDetector;
import facegallery.FaceGallery;
import facegallery.tasks.ImageBytesReader;
import facegallery.utils.ByteArray;
import facegallery.utils.MyImageView;
import facegallery.tasks.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class FaceGalleryGui extends JFrame {

    static private ScrollPane scrollPane = new ScrollPane();
    static private JPanel buttonBar = new JPanel();
    static private JButton parallel,sequential,concurrent,pipeline,reset;
    static private JPanel controls = new JPanel();
    static private JPanel controlPanel = new JPanel();



    ImageBytesReader imageBytesReader = new ImageBytesReader(FaceGallery.TEST_DATASET_DIR);
    FaceDetector faceDetector = new FaceDetector(imageBytesReader.getImageBytes());
    File[] listOfFiles = imageBytesReader.getFileList();
    Dimension d;
    int currentWidth, currentHeight;
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
            Tasker tasker = new Tasker();
            //tasker.performParallel(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages, false);
        }
    }

    private class sequentialListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("ENTERED");
            Tasker tasker = new Tasker();
            tasker.performSequential(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages, false);
        }
    }

    private class concurrentListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("ENTERED");
            Tasker tasker = new Tasker();
            tasker.performConcurrent(FaceGalleryGui::updateStats, FaceGalleryGui::updateImages, false);
        }
    }

    private class pipelineListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("ENTERED");
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
            buttonBar.removeAll();
        }
    }

    public FaceGalleryGui() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Gallery app");
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
        buttonBar.setLayout(new GridLayout(0,4,5,5));
        parallel = new JButton("parallel");
        sequential = new JButton("sequential");
        concurrent = new JButton("concurrent");
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
        controls.add(labelTextFaces);
        controls.add(timeFaces);
        controls.add(labelTextThumb);
        controls.add(timeThumb);
        controls.add(labelTextBlur);
        controls.add(timeBlur);
        controls.add(labelTotalTime);
        controls.add(totalTime);
        controls.add(currentAction);
        controlPanel.add(controls,BorderLayout.SOUTH);


        setLayout(new GridLayout(1,2));

        add(scrollPane);
        //add(photographLabel, BorderLayout.CENTER);
        add(controlPanel);

        setSize(1280, 720);
        d = getSize();
        currentWidth = d.width;
        currentHeight = d.height;



        setLocationRelativeTo(null);


        loadimages.execute();
    }


    private SwingWorker<Void, ThumbnailAction> loadimages = new SwingWorker<Void, ThumbnailAction>() {


        @Override
        protected Void doInBackground() throws Exception {
            for(int i=0 ; i < listOfFiles.length ; i++) {
                ImageIcon icon;
                icon = createImageIcon(listOfFiles[i].toURI().toString(), "" );

                ThumbnailAction thumbAction;


                ImageIcon thumbnailIcon = new ImageIcon(getScaledImage(icon.getImage(), 150, 150));

                thumbAction = new ThumbnailAction(thumbnailIcon);




                publish(thumbAction);
            }

            return null;
        }


        @Override
        protected void process(List<ThumbnailAction> chunks) {
            for (ThumbnailAction thumbAction : chunks) {
                JButton thumbButton = new JButton(thumbAction);

                //buttonBar.add(thumbButton);
            }
            scrollPane.add(buttonBar);
        }
    };


    protected static ImageIcon createImageIcon(String path,
                                        String description) {
        java.net.URL imgURL;
        try { imgURL = new java.net.URL(path);
            if (imgURL != null) {
                return new ImageIcon(imgURL, description);
            } else {
                System.err.println("Couldn't find file: " + path);
                return null;
            }}
        catch(Exception e){System.out.println(e);
            return  null;}

    }


    private static Image getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }


    private static class ThumbnailAction extends AbstractAction{


        public ThumbnailAction(Icon thumb){



            putValue(LARGE_ICON_KEY, thumb);
        }


        public void actionPerformed(ActionEvent e) {


        }
    }

    public static Void updateStats(TaskerStats stats) {
//        System.out.printf("=====================================%n" +
//                        "|  Last Action : %17s  |%n" +
//                        "|  Total Time  : %17.2f  |%n" +
//                        "=====================================%n" +
//                        "|  Task  |  Time  |  Done  |  Tota  |%n" +
//                        "|  LOAD  |  %3.2f  |  %4d  |  %4d  |%n" +
//                        "|  THUM  |  %3.2f  |  %4d  |  %4d  |%n" +
//                        "|  FACE  |  %3.2f  |  %4d  |  %4d  |%n" +
//                        "|  RESC  |  %3.2f  |  %4d  |  %4d  |%n" +
//                        "=====================================%n",
//                stats.lastAction.toDisplayString(),
//                stats.totalRuntime, now
//                stats.fileReadStats.runtime, stats.fileReadStats.taskProgress, stats.fileReadStats.taskTotal,
//                stats.thumbnailGenerateStats.runtime, stats.thumbnailGenerateStats.taskProgress, stats.thumbnailGenerateStats.taskTotal,
//                stats.faceDetectionStats.runtime, stats.faceDetectionStats.taskProgress, stats.faceDetectionStats.taskTotal,
//                stats.imageRescaleStats.runtime, stats.imageRescaleStats.taskProgress, stats.imageRescaleStats.taskProgress
//        );
        time.setText(Double.toString(stats.fileReadStats.runtime));
        timeFaces.setText(Double.toString(stats.faceDetectionStats.runtime));
        timeThumb.setText(Double.toString(stats.thumbnailGenerateStats.runtime));
        timeBlur.setText(Double.toString(stats.imageRescaleStats.runtime));
        totalTime.setText(Double.toString(stats.totalRuntime));

        return null;
    }

    public static Void updateImages(List<BufferedImage> imageList) {
        buttonBar.removeAll();
        System.out.println("Images:" + imageList.size());
        for (BufferedImage image : imageList) {
            if(image != null) {
                ImageIcon ii = new ImageIcon(image);
                JButton newImage = new JButton(ii);
                buttonBar.add(newImage);
            }
            else {
                System.out.println("NULL");
            }
        }
        return null;
    }
}