package analyzers;

import infrastructure.CVJPanel;
import infrastructure.CVManager;
import infrastructure.FrameAnalyzer;
import org.opencv.core.*;
import org.opencv.imgproc.CLAHE;
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
     * Fixes the lighting of the original image by converting to a different color space and equalizing the histogram.
     * @return the fixed image
     */
    private void preprocessLighting(Mat toFix)
    {
        Imgproc.cvtColor(toFix, toFix, Imgproc.COLOR_BGR2YCrCb);
        LinkedList<Mat> channels = new LinkedList<>();
        Core.split(toFix, channels);
        Imgproc.equalizeHist(channels.get(0), channels.get(0));
        Core.merge(channels, toFix);
        Imgproc.cvtColor(toFix, toFix, Imgproc.COLOR_YCrCb2BGR);
    }

    /**
     * This is where pretty much of all of the prototyping code should go.
     */
    @Override
    public void analyze(Mat raw)
    {
        Imgproc.resize(raw, raw, new Size(500, 300));

        Mat blueMask = new Mat();
        LinkedList<Mat> channels = new LinkedList<>();

        // Fix the lighting contrast that results from using different fields.
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2YCrCb);
        Core.split(raw, channels);
        Imgproc.equalizeHist(channels.get(0), channels.get(0));
        Core.merge(channels, raw);
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_YCrCb2BGR);

        // Get blue mask.
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2HSV);
        Core.inRange(raw, new Scalar(40, 0, 0), new Scalar(150, 180, 255), blueMask);

        // Dilate the blue mask so we can find contours within it.
        Imgproc.dilate(blueMask, blueMask, Mat.ones(50, 1, CvType.CV_32F));

        // Invert the mask and neutralize non-included pixels in raw after making it grayscale.
        Core.bitwise_not(blueMask, blueMask);
        raw.setTo(new Scalar(0, 0, 0), blueMask);

        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HSV2BGR);

        new CVJPanel(raw, "Final");

        // TODO contours
    }
}
