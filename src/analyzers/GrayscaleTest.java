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

public class GrayscaleTest implements FrameAnalyzer
{
    public static void main(String[] args)
    {
        new GrayscaleTest();
    }

    public GrayscaleTest()
    {
        CVManager.runOn(this);
    }

    /**
     * This is where pretty much of all of the prototyping code should go.
     */
    @Override
    public void analyze(Mat raw)
    {
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2GRAY);
        new CVJPanel(raw, "Grayscale");
    }
}
