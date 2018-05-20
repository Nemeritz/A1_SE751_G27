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

    private ScrollPane scrollPane = new ScrollPane();
    private JPanel buttonBar = new JPanel();
    private JButton parallel,sequential;
    private JPanel controls = new JPanel();
    private JPanel controlPanel = new JPanel();



    ImageBytesReader imageBytesReader = new ImageBytesReader("/Users/aneesh/Images");
    FaceDetector faceDetector = new FaceDetector(imageBytesReader.getImageBytes());
    File[] listOfFiles = imageBytesReader.getFileList();
    Dimension d;
    int currentWidth, currentHeight;
    JLabel timeTakenImages;
    JLabel timeTakenFaces;
    JLabel time;
    JLabel timeFaces;
    JLabel currentAction;


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

        }
    }
    private class sequentialListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            currentAction.setText("Reading files...");

            long imageReadStartTime = System.currentTimeMillis();
            @Future
            ByteArray[] imageBytes = imageBytesReader.run();
            long imageReadEndTime = System.currentTimeMillis();

            time.setText(Double.toString((double)(imageReadEndTime - imageReadStartTime) / 1000));

            currentAction.setText("Detecting faces...");
            long faceDetectStartTime = System.currentTimeMillis();
            Boolean[] detections = faceDetector.run(true);
            long faceDetectEndTime = System.currentTimeMillis();

            timeFaces.setText(Double.toString((double)(faceDetectEndTime - faceDetectStartTime) / 1000));

            currentAction.setText("Done!");
            System.out.println("Done");
            //return true;

        }
    }
    public FaceGalleryGui() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Gallery app");
        timeTakenImages = new JLabel("time taken to load Images:");
        timeTakenFaces = new JLabel("time taken to detect Faces:");
        time = new JLabel("   ");
        timeFaces = new JLabel("  ");
        currentAction = new JLabel("");
        buttonBar.setLayout(new GridLayout(0,4,5,5));
        parallel = new JButton("parallel");
        sequential = new JButton("sequential");
        parallel.addActionListener(new parallelListener());
        sequential.addActionListener(new sequentialListener());
        controls.setLayout(new BoxLayout(controls, BoxLayout.PAGE_AXIS));
        controls.add(new JLabel("Detect Faces in:"),BorderLayout.CENTER);
        controls.add(parallel);
        controls.add(sequential);
        controls.add(timeTakenImages);
        controls.add(time);
        controls.add(timeTakenFaces);
        controls.add(timeFaces);
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

                buttonBar.add(thumbButton);
            }
            scrollPane.add(buttonBar);
        }
    };


    protected ImageIcon createImageIcon(String path,
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


    private Image getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }


    private class ThumbnailAction extends AbstractAction{


        public ThumbnailAction(Icon thumb){



            putValue(LARGE_ICON_KEY, thumb);
        }


        public void actionPerformed(ActionEvent e) {


        }
    }
}