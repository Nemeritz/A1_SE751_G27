package facegallery.gui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.swing.AbstractAction;

import apt.annotations.Future;
import facegallery.tasks.FaceDetector;
import facegallery.FaceGallery;
import facegallery.tasks.ImageBytesReader;
import facegallery.utils.ByteArray;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class FaceGalleryGui extends JFrame {

    private JLabel photographLabel = new JLabel();
    private JToolBar buttonBar = new JToolBar();
    private JButton parallel,sequential;

    private String imagedir = "/Users/aneesh/Images";
    File folder = new File(imagedir);
    File[] listOfFiles = folder.listFiles();
    Dimension d;
    int currentWidth, currentHeight;

/*
    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                FaceGalleryGui app = new FaceGalleryGui();
                app.setVisible(true);
            }
        });
    } */
    private class parallelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
        FaceGallery.runParallel();
        }
    }
    private class sequentialListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
        FaceGallery.runSequential();
        }
    }
    public FaceGalleryGui() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Please Select an Image");


        photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        parallel = new JButton("parallel");
        sequential = new JButton("sequential");
        parallel.addActionListener(new parallelListener());
        sequential.addActionListener(new sequentialListener());
        buttonBar.add(new JLabel("Detect Faces in:"));
        buttonBar.add(parallel);
        buttonBar.add(sequential);
        buttonBar.add(Box.createGlue());
        buttonBar.add(Box.createGlue());


        add(buttonBar, BorderLayout.SOUTH);
        add(photographLabel, BorderLayout.CENTER);

        setSize(1280, 720);
        d = getSize();
        currentWidth = d.width;
        currentHeight = d.height;



        setLocationRelativeTo(null);


        loadimages.execute();
//        setVisible(true);
    }


    private SwingWorker<Void, ThumbnailAction> loadimages = new SwingWorker<Void, ThumbnailAction>() {


        @Override
        protected Void doInBackground() throws Exception {
            for(int i=0 ; i < listOfFiles.length ; i++) {
                ImageIcon icon;
                icon = createImageIcon(listOfFiles[i].toURI().toString(), "" );

                ThumbnailAction thumbAction;


                ImageIcon thumbnailIcon = new ImageIcon(getScaledImage(icon.getImage(), 32, 32));

                thumbAction = new ThumbnailAction(icon, thumbnailIcon);




                publish(thumbAction);
            }

            return null;
        }


        @Override
        protected void process(List<ThumbnailAction> chunks) {
            for (ThumbnailAction thumbAction : chunks) {
                JButton thumbButton = new JButton(thumbAction);

                buttonBar.add(thumbButton, buttonBar.getComponentCount() - 1);
            }
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


        private ImageIcon displayPhoto;


        public ThumbnailAction(ImageIcon photo, Icon thumb){
            displayPhoto = photo;


            putValue(LARGE_ICON_KEY, thumb);
        }


        public void actionPerformed(ActionEvent e) {
            photographLabel.setIcon(new ImageIcon(getScaledImage(displayPhoto.getImage(),currentWidth,currentHeight)));

        }
    }
}