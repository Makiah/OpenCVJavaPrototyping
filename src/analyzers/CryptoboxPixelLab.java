package analyzers;

import infrastructure.CVJPanel;
import infrastructure.CVManager;
import infrastructure.FrameAnalyzer;
import org.opencv.core.*;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class CryptoboxPixelLab implements FrameAnalyzer
{
    public static void main(String[] args)
    {
        new CryptoboxPixelLab();
    }

    public CryptoboxPixelLab()
    {
        CVManager.runOn(this);
    }

    /**
     * This is where pretty much of all of the prototyping code should go.
     */
    @Override
    public void analyze(Mat raw)
    {
        Mat kernel = Mat.ones(2, 2, CvType.CV_32F);

        // Resize picture to phone space.
        Imgproc.resize(raw, raw, new Size(700, 360));

        Imgproc.blur(raw, raw, new Size(3, 3));

        // Work in Hue Luminance Saturation Space.
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2HLS);
        LinkedList<Mat> channels = new LinkedList<>();
        Core.split(raw, channels);

        // Use adaptive threshold to artificially create luminance contours.
        Imgproc.adaptiveThreshold(channels.get(1), channels.get(1), 150, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        //Imgproc.blur(channels.get(1), channels.get(1), new Size(1, 10));
        Imgproc.erode(channels.get(1),channels.get(1),kernel);
        Imgproc.dilate(channels.get(1),channels.get(1),kernel);
        new CVJPanel(channels.get(1), "Luminance Thresholds", 0, 0);

        // Merge artificial contours into raw, and move back to normal color space.
        Core.merge(channels, raw);
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HLS2BGR);
        new CVJPanel(raw, "Raw", 500, 0);

        // Extract blue channel
        LinkedList<Mat> bgrChannels = new LinkedList<>();
        Core.split(raw, bgrChannels);
        new CVJPanel(bgrChannels.get(0), "Blue channel", 0, 380);

        // Extract blue regions using ANOTHER adaptive threshold.
        Mat blueRegion = bgrChannels.get(0).clone();
//        Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10,2));
//        Imgproc.morphologyEx(blueRegion,blueRegion,Imgproc.MORPH_CLOSE, structure);

        Mat kernelRectangle = Mat.ones(15, 1, CvType.CV_32F);
        //Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,15));
        //Imgproc.morphologyEx(blueRegion,blueRegion,Imgproc.MORPH_CLOSE, structure);
        Imgproc.dilate(blueRegion, blueRegion, kernel);

        Core.subtract(blueRegion, new Scalar(100), blueRegion);

        new CVJPanel(blueRegion, "Blue channel", 500, 380);


        if (true)
            return;

        ////// Filter based on RGB ///////
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HLS2BGR);
        new CVJPanel(raw, "Recolor");
        Core.inRange(raw,
                new Scalar(120, 0, 0),
                new Scalar(255, 120, 52), raw);

        ////// Get rid of excessive noise ///////
        Imgproc.blur(raw, raw, new Size(1, 5));
        //Mat kernel = Mat.ones(15,3, CvType.CV_32F);
        Imgproc.erode(raw,raw,kernel);
        Imgproc.dilate(raw,raw,kernel);
        Imgproc.threshold(raw, raw, 200, 255, Imgproc.THRESH_BINARY);

        //new CVJPanel(raw, "Final");
    }
}
