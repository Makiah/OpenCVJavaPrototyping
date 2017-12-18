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

    enum CryptoColor
    {
        PRIMARY, WHITE, NONE
    }

    private CryptoColor getProminentColorFrom(CryptoColor[] colors)
    {
        int primaries = 0, whites = 0, nones = 0;

        for (CryptoColor color : colors)
        {
            switch (color)
            {
                case PRIMARY:
                    primaries++;
                    break;

                case WHITE:
                    whites++;
                    break;

                case NONE:
                    nones++;
                    break;
            }
        }

        // Return prominent color.
        if (primaries > whites && primaries > nones)
            return CryptoColor.PRIMARY;
        else if (whites > primaries && whites > nones)
            return CryptoColor.WHITE;
        else
            return CryptoColor.NONE;
    }

    private CryptoColor[] blurColumnArray(CryptoColor[] pixelColumn, int radius)
    {
        // Sort of "blur" the barcode type array with a radius of 5.
        for (int i = 0; i < pixelColumn.length - radius; i += radius)
        {
            CryptoColor[] radiusArray = new CryptoColor[radius];
            for (int j = 0; j < radius; j++)
                radiusArray[j] = pixelColumn[i + j];

            CryptoColor prominentColor = getProminentColorFrom(radiusArray);

            for (int j = 0; j < radius; j++)
                pixelColumn[i + j] = prominentColor;
        }

        return pixelColumn;
    }

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
            CryptoColor[] pixelColumn = new CryptoColor[raw.height()];

            int bluePixels = 0, whitePixels = 0, otherPixels = 0;

            // Define the pixel column
            for (int y = 0; y < raw.rows(); y++)
            {
                double[] pixel = raw.get(y, x);

                if (pixel[0] > 100 - .1 * (pixel[2] - 90) && pixel[0] < 190 - .1 * (pixel[2] - 90) && pixel[1] > 50 - .2 * (pixel[2] - 90))
                {
                    pixelColumn[y] = CryptoColor.PRIMARY;
                    bluePixels++;
                }
                else if (pixel[1] < 56 && pixel[2] > 69)
                {
                    pixelColumn[y] = CryptoColor.WHITE;
                    whitePixels++;
                }
                else
                {
                    pixelColumn[y] = CryptoColor.NONE;
                }
            }

            // Short circuit to the next loop if we detect very few blue pixels.
//            if (bluePixels < camResolution.height / 5 && whitePixels < camResolution.height / 10)
//                continue;

            // Blur the column.
            pixelColumn = blurColumnArray(pixelColumn, 5);

            // Apply this back to the original image and just see what's up with it now.
            for (int j = 0; j < pixelColumn.length; j++)
            {
                switch(pixelColumn[j])
                {
                    case NONE:
                        raw.put(j, x, 0, 0, 0);
                        break;

                    case PRIMARY:
                        raw.put(j, x, 70, 255, 120);
                        break;

                    case WHITE:
                        raw.put(j, x, 0, 0, 255);
                        break;
                }
            }

            // Now start filtering out non-columns
            int[] pixelQuantities = new int[3];
            for (int j = 0; j < pixelColumn.length; j++)
            {
                switch (pixelColumn[j])
                {
                    case PRIMARY:
                        pixelQuantities[0]++;
                        break;

                    case WHITE:
                        pixelQuantities[1]++;
                        break;

                    case NONE:
                        pixelQuantities[2]++;
                        break;
                }
            }

            // If this isn't a column, then make it appear as empty.
//            if (!(pixelQuantities[0] > .1 * camResolution.height && pixelQuantities[1] > .1 * camResolution.height))
//            {
//                for (int j = 0; j < pixelColumn.length; j++)
//                {
//                    raw.put(j, x, 0, 0, 0);
//                }
//            }
        }

        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HSV2BGR);
        new CVJPanel(raw, "Processed", 550, 0);
    }
}
