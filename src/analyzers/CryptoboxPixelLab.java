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

        // Blur image to reduce noise slightly.
        Imgproc.blur(raw, raw, new Size(3, 3));

        // Work in Hue Luminance Saturation Space.
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2HLS);
        LinkedList<Mat> channels = new LinkedList<>();
        Core.split(raw, channels);

        // Use adaptive threshold to artificially create luminance contours.
        Imgproc.adaptiveThreshold(channels.get(1), channels.get(1), 150, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 9, 2);
        //Imgproc.blur(channels.get(1), channels.get(1), new Size(1, 10));
        Imgproc.erode(channels.get(1),channels.get(1),kernel);
        Imgproc.dilate(channels.get(1),channels.get(1),kernel);
        //Imgproc.threshold(channels.get(1), channels.get(1), 0, 50, Imgproc.THRESH_BINARY);
        //Imgproc.cvtColor(channels.get(1), channels.get(1), GRAY);
        //new CVJPanel(channels.get(1), "Luminance Thresholds", 0, 0);

        // Merge artificial contours into raw, and move back to BGR color space.
        Core.merge(channels, raw);
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HLS2BGR);
        //new CVJPanel(raw, "Raw", 500, 0);

        // Extract blue channel
        LinkedList<Mat> bgrChannels = new LinkedList<>();
        Core.split(raw, bgrChannels);
        //new CVJPanel(bgrChannels.get(0), "Blue channel", 0, 380);

        // Extract blue regions using ANOTHER adaptive threshold.
        Mat blueRegion = bgrChannels.get(0).clone();
//        Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10,2));
//        Imgproc.morphologyEx(blueRegion,blueRegion,Imgproc.MORPH_CLOSE, structure);

        new CVJPanel(blueRegion, "Blue region", 0, 380);

        Imgproc.equalizeHist(blueRegion, blueRegion);

        Imgproc.threshold(blueRegion, blueRegion, 150, 255, Imgproc.THRESH_BINARY);

        //Mat kernelRectangle = Mat.ones(60, 1, CvType.CV_32F);
        //Imgproc.dilate(blueRegion, blueRegion, kernelRectangle);

        //Imgproc.blur(blueRegion, blueRegion, new Size(6, 6));

        Mat widthErode = Mat.ones(1, 5, CvType.CV_32F);
        Imgproc.erode(blueRegion, blueRegion, widthErode);

        new CVJPanel(blueRegion, "Width erode", 500, 0);

        Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,10));
        Imgproc.morphologyEx(blueRegion,blueRegion,Imgproc.MORPH_CLOSE, structure);

        new CVJPanel(blueRegion, "Blue channel", 0, 0);

        Mat finalErode = Mat.ones(1, 5, CvType.CV_32F);
        Imgproc.erode(blueRegion, blueRegion, finalErode);

        Mat finalDilate = Mat.ones(6, 3, CvType.CV_32F);
        Imgproc.dilate(blueRegion, blueRegion, finalDilate);

        new CVJPanel(blueRegion, "Finalizations", 500, 380);
    }
}
