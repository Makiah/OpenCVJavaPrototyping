package prototyping;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.*;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

class JPanelOpenCV extends JPanel{

    // Compulsory
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    BufferedImage image;

    public static void main (String args[]) throws InterruptedException{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        JPanelOpenCV t = new JPanelOpenCV();
        VideoCapture camera = new VideoCapture(0);

        Mat frame = new Mat();
        camera.read(frame);

        if(!camera.isOpened()){
            System.out.println("Error");
        }
        else {
            while(true){

                if (camera.read(frame)){

                    BufferedImage image = t.MatToBufferedImage(frame);

                    t.window(image, "Original Image", 0, 0);

                    // The customly modified image.
                    t.analyzeNewMat(frame);

                    //t.window(t.loadImage("ImageName"), "Image loaded", 0, 0);

                    break;
                }
            }
        }
        camera.release();
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }

    public JPanelOpenCV() {
    }

    public JPanelOpenCV(BufferedImage img) {
        image = img;
    }

    //Show image on window
    public void window(BufferedImage img, String text, int x, int y) {
        JFrame frame0 = new JFrame();
        frame0.getContentPane().add(new JPanelOpenCV(img));
        frame0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame0.setTitle(text);
        frame0.setSize(img.getWidth(), img.getHeight() + 30);
        frame0.setLocation(x, y);
        frame0.setVisible(true);
    }

    //Load an image
    public BufferedImage loadImage(String file) {
        BufferedImage img;

        try {
            File input = new File(file);
            img = ImageIO.read(input);

            return img;
        } catch (Exception e) {
            System.out.println("erro");
        }

        return null;
    }

    //Save an image
    public void saveImage(BufferedImage img) {
        try {
            File outputfile = new File("Images/new.png");
            ImageIO.write(img, "png", outputfile);
        } catch (Exception e) {
            System.out.println("error");
        }
    }

    //Grayscale filter
    public BufferedImage grayscale(BufferedImage img) {
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                Color c = new Color(img.getRGB(j, i));

                int red = (int) (c.getRed() * 0.299);
                int green = (int) (c.getGreen() * 0.587);
                int blue = (int) (c.getBlue() * 0.114);

                Color newColor =
                        new Color(
                                red + green + blue,
                                red + green + blue,
                                red + green + blue);

                img.setRGB(j, i, newColor.getRGB());
            }
        }

        return img;
    }

    public BufferedImage MatToBufferedImage(Mat frame) {
        //Mat() to BufferedImage
        int type = 0;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0, 0, data);

        return image;
    }


    //////////// OPEN CV //////////////

    /**
     * This is where pretty much of all of the prototyping code should go.
     */
    public void analyzeNewMat(Mat raw)
    {
        /////// RESIZE IMAGE ///////
        Imgproc.resize(raw, raw, new Size(740, 480));
        this.window(MatToBufferedImage(raw), "Resized", 0, 0);


        ////// Equalize luminance ///////
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2HLS);
        List<Mat> channels = new LinkedList<Mat>();
        Core.split(raw, channels);
        channels.get(1).setTo(new Scalar(150));
        channels.get(2).setTo(new Scalar(150));
        Core.merge(channels, raw);
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HLS2BGR);
        this.window(MatToBufferedImage(raw), "Equalized", 500, 0);


        ////// Extract blue region ///////
        List<Mat> bgrChannels = new LinkedList<Mat>();
        Core.split(raw, bgrChannels);
        Mat blueGrayscale = bgrChannels.get(0);
        this.window(MatToBufferedImage(blueGrayscale), "Blue Grayscale", 0, 380);

        /////// Absolute threshold to determine which pixels are correct.   ///////
        Mat blueBinary = new Mat();
        Imgproc.threshold(blueGrayscale, blueBinary, 200, 255, Imgproc.THRESH_BINARY);
        this.window(MatToBufferedImage(blueBinary), "Blue Binary", 500, 380);


        ////// Erode and dilate the resulting image to remove noise ////////
        Imgproc.blur(blueBinary, blueBinary, new Size(3, 3));
        Mat kernel = Mat.ones(8,8,CvType.CV_32F);
        Imgproc.erode(blueBinary,blueBinary,kernel);
        Imgproc.dilate(blueBinary,blueBinary,kernel);
        Imgproc.threshold(blueBinary, blueBinary, 150, 255, Imgproc.THRESH_BINARY);
        this.window(MatToBufferedImage(blueBinary), "Dilated Blue Binary", 0, 0);


        /////// Find contours of approximately the cryptobox's shape ///////
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        List<Rect> boxes = new ArrayList<>();

        Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,30));
        Imgproc.morphologyEx(blueBinary,blueBinary,Imgproc.MORPH_CLOSE, structure);

        Imgproc.findContours(blueBinary, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        hierarchy.release();

        this.window(MatToBufferedImage(blueBinary), "Final", 0, 0);
    }

    private void reference(Mat raw) {
        //Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2HSV);
        //Core.inRange(raw,
        //        new Scalar(60, 0, 0),
        //        new Scalar(200, 255, 100), raw);
        //this.window(MatToBufferedImage(raw), "HSV filter", 0, 380);

        Imgproc.resize(raw,raw,new Size(480,360));

        Mat hsv = new Mat();
        Mat mask = new Mat();
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        List<Rect> boxes = new ArrayList<>();

        Imgproc.cvtColor(raw,hsv,Imgproc.COLOR_BGR2HSV);

        this.window(MatToBufferedImage(hsv), "CVT color image", 0, 0);

        Mat kernel = Mat.ones(5,5,CvType.CV_32F);

        Imgproc.erode(hsv,hsv,kernel);
        Imgproc.dilate(hsv,hsv,kernel);
        Imgproc.blur(hsv,hsv,new Size(6,6));

        this.window(MatToBufferedImage(hsv), "Blurred image", 500, 0);

        Scalar lower = new Scalar(90,135,25);
        Scalar upper = new Scalar(130,250,150);

        Core.inRange(hsv,lower,upper,mask);
        hsv.release();

        this.window(MatToBufferedImage(mask), "Mask Image", 0, 370);

        Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,30));
        Imgproc.morphologyEx(mask,mask,Imgproc.MORPH_CLOSE, structure);

        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        hierarchy.release();

        for(MatOfPoint c : contours) {
            if(Imgproc.contourArea(c) >= 100) { //Filter by area
                Rect column = Imgproc.boundingRect(c);
                int ratio = Math.abs(column.height / column.width);

                if(ratio > 1.5) { //Check to see if the box is tall
                    boxes.add(column); //If all true add the box to array
                }
            }
        }
        for(Rect box : boxes) {
            Imgproc.rectangle(raw,new Point(box.x,box.y),new Point(box.x+box.width,box.y+box.height),new Scalar(255,0,0),2);
        }

        Collections.sort(boxes, (rect, t1) -> {
            if(rect.x > t1.x){
                return 1;
            }else if(rect.x < t1.x){
                return -1;
            }else{
                return 0;
            }
        });


        if(boxes.size() >=4 ){
            Point left = drawSlot(0,boxes);
            Point center = drawSlot(1,boxes);
            Point right = drawSlot(2,boxes);

            Imgproc.putText(raw, "Left", new Point(left.x - 10, left.y - 20), 0,0.8, new Scalar(0,255,255),2);
            Imgproc.circle(raw,left,5,new Scalar(0,255,255), 3);

            Imgproc.putText(raw, "Center", new Point(center.x - 10, center.y - 20), 0,0.8, new Scalar(0,255,255),2);
            Imgproc.circle(raw,center, 5,new Scalar(0,255,255), 3);

            Imgproc.putText(raw, "Right", new Point(right.x - 10, right.y - 20), 0,0.8, new Scalar(0,255,255),2);
            Imgproc.circle(raw,right, 5,new Scalar(0,255,255), 3);
        }


        Imgproc.resize(raw,raw, new Size(1280,960));
        mask.release();

        this.window(MatToBufferedImage(raw), "Final", 0, 0);
    }

    // Helper methods
    public Object getKey(List item) {
        return item.get(0);
    }
    public Point drawSlot(int slot, List<Rect> boxes){
        Rect leftColumn = boxes.get(slot); //Get the pillar to the left
        Rect rightColumn = boxes.get(slot + 1); //Get the pillar to the right

        int leftX = leftColumn.x; //Get the X Coord
        int rightX = rightColumn.x; //Get the X Coord

        int drawX = ((rightX - leftX) / 2) + leftX; //Calculate the point between the two
        int drawY = leftColumn.height + leftColumn.y; //Calculate Y Coord. We wont use this in our bot's opetation, buts its nice for drawing

        return new Point(drawX, drawY);
    }

    public ArrayList ones(int width, int height) {
        ArrayList output = new ArrayList();
        for(int i = 1; i <= height; i++) {
            ArrayList row = new ArrayList();
            for(int j = 1; i <= width; i++) {
                row.add(1);
            }
            output.add(row);
        }
        return output;
    }
}