package analyzers;

import infrastructure.CVJPanel;
import infrastructure.CVManager;
import infrastructure.FrameAnalyzer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class AdaptiveGaussian implements FrameAnalyzer
{
    public static void main(String[] args)
    {
        new AdaptiveGaussian();
    }

    public AdaptiveGaussian()
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

        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2GRAY);

        new CVJPanel(raw, "Before", 0, 0);

        Imgproc.adaptiveThreshold(raw, raw, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);

        new CVJPanel(raw, "After", 500, 0);
    }
}
