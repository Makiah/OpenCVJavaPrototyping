package analyzers;

import infrastructure.CVJPanel;
import infrastructure.CVManager;
import infrastructure.FrameAnalyzer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class BlueAndWhiteCrypto implements FrameAnalyzer
{
    public static void main(String[] args)
    {
        new BlueAndWhiteCrypto();
    }

    public BlueAndWhiteCrypto()
    {
        CVManager.runOn(this);
    }

    /**
     * This is where pretty much of all of the prototyping code should go.
     */
    @Override
    public void analyze(Mat raw)
    {
        // Resize picture to phone space.
        Imgproc.resize(raw, raw, new Size(700, 360));

        // Blur image to reduce noise slightly.
        Imgproc.blur(raw, raw, new Size(3, 3));

        // Work in Hue Luminance Saturation Space.
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2HLS);
        LinkedList<Mat> channels = new LinkedList<>();
        Core.split(raw, channels);

//        new CVJPanel(channels.get(2), "Sat b4");

//        Imgproc.blur(channels.get(2), channels.get(2), new Size(5, 5));
//        Imgproc.blur(channels.get(0), channels.get(0), new Size(5, 5));
//        new CVJPanel(channels.get(2), "Sat after", 600, 0);

//        for (int i = 0; i < 3; i++)
//            new CVJPanel(channels.get(i), "Channel before", 500 * i, 0);
//
//        if (true)
//            return;

        // Use adaptive threshold to artificially create luminance contours.
        Mat lContours = new Mat();
        Imgproc.adaptiveThreshold(channels.get(1), lContours, 100, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 19, 3);
        Imgproc.dilate(lContours,lContours,Mat.ones(3, 3, CvType.CV_32F));
        Imgproc.erode(lContours,lContours,Mat.ones(5, 1, CvType.CV_32F));
        new CVJPanel(lContours, "borders", 0, 0);

        Core.addWeighted(lContours, 0.5, channels.get(1), 1, 0, channels.get(1));
        new CVJPanel(channels.get(1), "Luminance with borders", 500, 0);

//        if (true)
//            return;

        //Mat kernel = Mat.ones(2, 2, CvType.CV_32F);

        // Merge artificial contours into raw, and move back to BGR color space.
        Core.merge(channels, raw);
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HLS2BGR);
        new CVJPanel(raw, "Merged", 500, 0);

        // Extract blue region.
        Mat blue = new Mat();
        Core.inRange(raw, new Scalar(80, 20, 10), new Scalar(255, 125, 75), blue);
        new CVJPanel(blue, "Blue Region", 0, 380);

        Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,5));
        Imgproc.morphologyEx(blue,blue,Imgproc.MORPH_CLOSE, structure);
        //Imgproc.erode(blue,blue,Mat.ones(5, 3, CvType.CV_32F));
        //Imgproc.morphologyEx(blue,blue,Imgproc.MORPH_CLOSE, structure);
        //Imgproc.blur(blue,blue,new Size(3, 3));
        new CVJPanel(blue, "Blue Region", 500, 380);
//        Core.split(raw, channels);
//        Mat blue = channels.get(0);
//        Imgproc.equalizeHist(blue, blue);
//        new CVJPanel(blue, "Blue Region", 0, 380);

        // Extract white region
//        Mat white = new Mat();
//        Core.inRange(raw, new Scalar(200, 200, 200), new Scalar(255, 255, 255), white);
//        new CVJPanel(white, "White Region", 500, 380);
    }
}
