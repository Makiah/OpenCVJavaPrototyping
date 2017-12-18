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

    public Mat getMatFromCryptoColors(CryptoColor[][] cryptoColors)
    {
        // Square.
        int cols = cryptoColors.length;
        int rows = cryptoColors[0].length;

        Mat toReturn = new Mat(rows, cols, CvType.CV_8UC3);

        for (int row = 0; row < rows; row++)
        {
            for (int col = 0; col < cols; col++)
            {
                switch (cryptoColors[col][row])
                {
                    case PRIMARY:
                        toReturn.put(row, col, 100, 255, 255);
                        break;

                    case WHITE:
                        toReturn.put(row, col, 0, 0, 255);
                        break;

                    case NONE:
                        toReturn.put(row, col, 0, 0, 0);
                        break;
                }
            }
        }

        return toReturn;
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



        CryptoColor[][] pixelColumns = new CryptoColor[raw.cols()][raw.rows()];


        // Process the original image into CryptoColors.
        for (int colIndex = 0; colIndex < raw.cols(); colIndex++)
        {
            CryptoColor[] pixelColumn = new CryptoColor[raw.height()];

            // Define the pixel column
            for (int y = 0; y < raw.rows(); y++) {
                double[] pixel = raw.get(y, colIndex);

                double blueHueMin = 75 - .3 * (pixel[2] - 55);
                double blueHueMax = 135 - .3 * (pixel[2] - 55);
                double blueSatMin = 50 + .1 * (pixel[2] - 55);

                double whiteSatMax = 52;
                double whiteValMin = 69;

                if ((blueHueMin < pixel[0] && pixel[0] < blueHueMax) && blueSatMin < pixel[1]) {
                    System.out.println("Pixel 2 is " + pixel[2]);

                    pixelColumn[y] = CryptoColor.PRIMARY;
                } else if (pixel[1] < whiteSatMax && whiteValMin < pixel[2]) {
                    pixelColumn[y] = CryptoColor.WHITE;
                } else {
                    pixelColumn[y] = CryptoColor.NONE;
                }
            }

            // Short circuit to the next loop if we detect very few blue pixels.
//            if (bluePixels < camResolution.height / 5 && whitePixels < camResolution.height / 10)
//                continue;

            pixelColumns[colIndex] = pixelColumn;
        }
        new CVJPanel(getMatFromCryptoColors(pixelColumns), "CryptoColor conversion");


        // Blur the resulting CryptoColor image.
        for (int colIndex = 0; colIndex < pixelColumns.length; colIndex++)
            // Blur the column.
            pixelColumns[colIndex] = blurColumnArray(pixelColumns[colIndex], 5);
        new CVJPanel(getMatFromCryptoColors(pixelColumns), "Blurred", 600, 0);


        // Filter through the columns
        for (int colIndex = 0; colIndex < pixelColumns.length; colIndex++)
        {
            // Now start filtering out non-columns
            int[] pixelQuantities = new int[3];
            for (int i = 0; i < pixelColumns[colIndex].length; i++)
            {
                switch (pixelColumns[colIndex][i])
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
            if (!(pixelQuantities[0] > .6 * camResolution.height && pixelQuantities[1] > .1 * camResolution.height))
            {
                for (int j = 0; j < pixelColumns[colIndex].length; j++)
                {
                    pixelColumns[colIndex][j] = CryptoColor.NONE;
                }
            }
        }
        new CVJPanel(getMatFromCryptoColors(pixelColumns), "Filtered", 550, 0);


        Imgproc.cvtColor(raw, raw, Imgproc.COLOR_HSV2BGR);
    }
}
