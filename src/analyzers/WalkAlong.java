package analyzers;

import infrastructure.CVJPanel;
import infrastructure.CVManager;
import infrastructure.FrameAnalyzer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class WalkAlong implements FrameAnalyzer
{
    public static void main(String[] args)
    {
        new WalkAlong();
    }

    public WalkAlong()
    {
        CVManager.runOn(this);
    }

    private int estimatedDistCM = 100;

    /**
     * This is where pretty much of all of the prototyping code should go.
     */
    @Override
    public void analyze(Mat raw)
    {
        // Set resolution
        Size camResolution = new Size(500, 300);
        Imgproc.resize(raw, raw, camResolution);

        LinkedList<Mat> channels = new LinkedList<>();

        // Fix the lighting contrast that results from using different fields.
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2YCrCb);
        Core.split(raw, channels);
        Imgproc.equalizeHist(channels.get(0), channels.get(0));
        Core.merge(channels, raw);
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_YCrCb2BGR);
        new CVJPanel(raw, "Lighting fix");

        Imgproc.blur(raw, raw, new Size(3, 3));

        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR2HSV);
        for (int x = 0; x < raw.cols(); x++)
        {
            int[] pixelColumn = new int[raw.height()];

            int bluePixels = 0, whitePixels = 0, otherPixels = 0;

            // Define the pixel column
            for (int y = 0; y < raw.rows(); y++)
            {
                double[] pixel = raw.get(y, x);

                if (pixel[0] > 30 && pixel[0] > 150)
                {
                    pixelColumn[y] = 1;
                    bluePixels++;
                    raw.put(y, x, 255, 0, 0); // blue
                }
                else if (pixel[1] < 50)
                {
                    pixelColumn[y] = 2;
                    whitePixels++;
                    raw.put(y, x, 255, 255, 255); // white
                }
                else
                {
                    pixelColumn[y] = 0;
                    raw.put(y, x, 0, 0, 0);
                }
            }

            // Short circuit to the next loop if we detect very few blue pixels.
            if (bluePixels < camResolution.height / 5)
                continue;

            // Sort of "blur" the barcode type array with a radius of 5.
            int radius = 5;
            for (int i = 0; i < pixelColumn.length; i += radius)
            {
                int[] mostPopular = new int[3];

                for (int j = 0; j < radius; j++)
                {
                    // add one to mostPopular[0, 1, or 2]
                    mostPopular[pixelColumn[i + j]]++;
                }

                int prominentColor = 0;
                for (int j = 0; j < mostPopular.length; j++) {
                    prominentColor = mostPopular[j] > mostPopular[prominentColor] ? j : prominentColor;
                }

                for (int j = 0; j < radius; j++)
                {
                    pixelColumn[i + j] = prominentColor;
                }
            }

            // Now figure out the distance from the column using this information.

        }
        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HSV2BGR);
        new CVJPanel(raw, "Blue/White", 550, 0);
    }
}
