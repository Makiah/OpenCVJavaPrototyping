package analyzers;

import infrastructure.CVJPanel;
import infrastructure.CVManager;
import infrastructure.FrameAnalyzer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class CryptoboxPixel implements FrameAnalyzer
{
    public static void main(String[] args)
    {
        new CryptoboxPixel();
    }

    public CryptoboxPixel()
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

        for (int y = 0; y < raw.rows(); y++)
        {
            for (int x = 0; x < raw.cols(); x++)
            {
                double[] pixel = raw.get(y, x);

                // Fix saturation
                pixel[2] = 255;
                pixel[1] = 100;

                raw.put(y, x, pixel);
            }
        }

        ////// Filter based on RGB ///////
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HLS2BGR);
        new CVJPanel(raw, "Recolor");
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
