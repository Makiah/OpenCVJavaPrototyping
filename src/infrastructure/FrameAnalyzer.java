package infrastructure;

import org.opencv.core.Mat;

public interface FrameAnalyzer
{
    void analyze(Mat frame);
}
