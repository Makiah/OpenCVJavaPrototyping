package analyzers;

import infrastructure.CVJPanel;
import infrastructure.CVManager;
import infrastructure.FrameAnalyzer;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class NewColorDetection implements FrameAnalyzer
{
    public static void main(String[] args)
    {
        new NewColorDetection();
    }

    public NewColorDetection()
    {
        CVManager.runOn(this);
    }

    /**
     * This is where pretty much of all of the prototyping code should go.
     */
    @Override
    public void analyze(Mat bgr)
    {
        LinkedList<Mat> channels = new LinkedList<>();

        Core.split(bgr, channels);

        // Subtract red from blue.
        Mat subtraction = new Mat();
        Core.addWeighted(channels.get(0), 1, channels.get(2), -0.5, 1, subtraction);
        Core.multiply(subtraction, new Scalar(5), subtraction);
        Core.addWeighted(subtraction, 1, channels.get(1), -0.3, 1, subtraction);
        new CVJPanel(subtraction, "Blue - red");


    }
}
