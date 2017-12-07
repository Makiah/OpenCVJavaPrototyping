package analyzers;

import infrastructure.CVJPanel;
import infrastructure.CVManager;
import infrastructure.FrameAnalyzer;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
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
        new CVJPanel(raw, "Resized", 0, 0);

        ///// Get rid of high luminance pixels (working in HLS space) ////
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2HLS);
        LinkedList<Mat> channels = new LinkedList<>();
        Core.split(raw, channels);
        Mat mask = new Mat();
        Imgproc.threshold(channels.get(1), mask, 0, 170, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        channels.get(1).setTo(new Scalar(100), mask);
        channels.get(2).setTo(new Scalar(255), mask);
        Core.merge(channels, raw);

        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HLS2BGR);
        new CVJPanel(raw, "Preprocessed", 0, 0);
    }
}
