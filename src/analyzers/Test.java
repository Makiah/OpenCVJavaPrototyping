package analyzers;

import infrastructure.CVManager;
import infrastructure.FrameAnalyzer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class Test implements FrameAnalyzer
{
    public static void main(String[] args)
    {
        new Test();
    }

    public Test()
    {
        CVManager.runOn(this);
    }

    /**
     * This is where pretty much of all of the prototyping code should go.
     */
    @Override
    public void analyze(Mat raw)
    {
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_RGBA2RGB);

        // Extract blue region.
        Core.inRange(raw, new Scalar(0, 0, 60), new Scalar(75, 160, 255), raw);
    }
}
