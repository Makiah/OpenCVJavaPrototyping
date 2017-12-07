package infrastructure;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class CVManager
{
    // Compulsory
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void runOn (FrameAnalyzer analyzer) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        VideoCapture camera = new VideoCapture(0);

        Mat frame = new Mat();
        camera.read(frame);

        if(!camera.isOpened()){
            System.out.println("Error");
        }
        else {
            while(true){

                if (camera.read(frame)){

                    new CVJPanel(frame, "Original");

                    // The customly modified image.
                    analyzer.analyze(frame);

                    break;
                }
            }
        }
        camera.release();
    }
}
