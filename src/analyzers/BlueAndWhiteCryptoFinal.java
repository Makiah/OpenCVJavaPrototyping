package analyzers;

import infrastructure.CVJPanel;
import infrastructure.CVManager;
import infrastructure.FrameAnalyzer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class BlueAndWhiteCryptoFinal implements FrameAnalyzer
{
    public static void main(String[] args)
    {
        new BlueAndWhiteCryptoFinal();
    }

    public BlueAndWhiteCryptoFinal()
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

        // Use adaptive threshold to artificially create luminance contours.
        Mat lContours = new Mat();
        Imgproc.adaptiveThreshold(channels.get(1), lContours, 100, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 19, 3);
        Imgproc.dilate(lContours,lContours,Mat.ones(3, 3, CvType.CV_32F));
        Imgproc.erode(lContours,lContours,Mat.ones(5, 1, CvType.CV_32F));
        new CVJPanel(lContours, "borders", 0, 0);

        Core.addWeighted(lContours, 0.4, channels.get(1), 1, 0, channels.get(1));
        new CVJPanel(channels.get(1), "Luminance with borders", 500, 0);

        // Merge artificial contours into raw, and move back to BGR color space.
        Core.merge(channels, raw);
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HLS2BGR);
        new CVJPanel(raw, "Merged", 500, 0);

        // Extract blue region.
        Mat blue = new Mat();
        Core.inRange(raw, new Scalar(60, 20, 10), new Scalar(255, 140, 75), blue);
        new CVJPanel(blue, "Blue Region", 0, 380);

        Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,12));
        Imgproc.morphologyEx(blue,blue,Imgproc.MORPH_CLOSE, structure);
        Imgproc.erode(blue, blue, Mat.ones(15, 3, CvType.CV_32F));
        new CVJPanel(blue, "Blue Region", 500, 380);
    }
}
