package analyzers;

import infrastructure.CVJPanel;
import infrastructure.CVManager;
import infrastructure.FrameAnalyzer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class CryptoboxMask implements FrameAnalyzer
{
    public static void main(String[] args)
    {
        new CryptoboxMask();
    }

    public CryptoboxMask()
    {
        CVManager.runOn(this);
    }

    /**
     * This is where pretty much of all of the prototyping code should go.
     */
    @Override
    public void analyze(Mat raw)
    {
        Imgproc.resize(raw, raw, new Size(700, 360));

        ///// Get rid of high luminance pixels (working in HLS space) ////
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2HLS);
        LinkedList<Mat> channels = new LinkedList<>();
        Core.split(raw, channels);
        Mat mask = new Mat();
        Imgproc.threshold(channels.get(1), mask, 0, 170, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        new CVJPanel(mask, "Mask 1", 500, 380);
        channels.get(1).setTo(new Scalar(100), mask);
        new CVJPanel(channels.get(1), "Is now", 0, 0);
        channels.get(2).setTo(new Scalar(255), mask);
//        Core.bitwise_not(mask, mask);
//        new CVJPanel(mask, "Mask 2", 500, 0);
//        channels.get(1).setTo(new Scalar(0), mask);
        Core.merge(channels, raw);

        ////// Filter based on RGB ///////
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HLS2BGR);
        Core.inRange(raw,
                new Scalar(120, 0, 0),
                new Scalar(255, 120, 52), raw);

        ////// Get rid of excessive noise ///////
        Imgproc.blur(raw, raw, new Size(2, 10));
        Mat kernel = Mat.ones(15,3, CvType.CV_32F);
        Imgproc.erode(raw,raw,kernel);
        Imgproc.dilate(raw,raw,kernel);
        Imgproc.threshold(raw, raw, 200, 255, Imgproc.THRESH_BINARY);

        new CVJPanel(raw, "Final");
    }
}
