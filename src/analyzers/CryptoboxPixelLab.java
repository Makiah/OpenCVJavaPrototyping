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
        Imgproc.resize(raw, raw, new Size(700, 360));

        ///// Get rid of high luminance pixels (working in HLS space) ////
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2HLS);

        Mat before = raw.clone();
        Mat after = raw.clone();

        // Before
        LinkedList<Mat> channelsBefore = new LinkedList<>();
        Core.split(before, channelsBefore);
//        CLAHE claheBefore = Imgproc.createCLAHE(4, new Size(50,200));
//        claheBefore.apply(channelsBefore.get(1), channelsBefore.get(1));
        new CVJPanel(channelsBefore.get(1), "Before 1", 0, 0);
        Imgproc.adaptiveThreshold(channelsBefore.get(1), channelsBefore.get(1), 150, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        //Imgproc.adaptiveThreshold();
        new CVJPanel(channelsBefore.get(1), "Before 1", 0, 0);

        Core.merge(channelsBefore, before);
        Imgproc.cvtColor(before, before, Imgproc.COLOR_HLS2BGR);
//        Imgproc.cvtColor(before, before, Imgproc.COLOR_HLS2BGR);
        new CVJPanel(before, "Before 1", 0, 0);

////        LinkedList<Mat> channelsBefore2 = new LinkedList<>();
////        Imgproc.cvtColor(before, before, Imgproc.COLOR_BGR2HLS);
//        Core.split(before, channelsBefore);
//        channelsBefore.get(1).setTo(new Scalar(100));
//        channelsBefore.get(2).setTo(new Scalar(255));
//        Core.merge(channelsBefore, before);
//        Imgproc.cvtColor(before, before, Imgproc.COLOR_HLS2BGR);
//        new CVJPanel(before, "Before 2", 500, 0);
//
//        //After
//        LinkedList<Mat> channelsAfter = new LinkedList<>();
//        Core.split(after, channelsAfter);
//        channelsAfter.get(1).setTo(new Scalar(100));
//        channelsAfter.get(2).setTo(new Scalar(255));
//        Core.merge(channelsAfter, after);
//        Imgproc.cvtColor(after, after, Imgproc.COLOR_HLS2BGR);
//        new CVJPanel(after, "After", 0, 380);


        if (true)
            return;

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

        //new CVJPanel(raw, "Final");
    }
}
