package analyzers;

import infrastructure.CVJPanel;
import infrastructure.CVManager;
import infrastructure.FrameAnalyzer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CryptoboxDetector2 implements FrameAnalyzer
{
    public enum CryptoboxDetectionMode {
        HSV_RED, HSV_BLUE
    }

    public enum CryptoboxSpeed {
        VERY_FAST, FAST, BALANCED, SLOW, VERY_SLOW
    }


    public CryptoboxDetectionMode detectionMode      = CryptoboxDetectionMode.HSV_BLUE;
    public double                 downScaleFactor    = 0.6;
    public boolean                rotateMat          = false;
    public CryptoboxSpeed         speed              = CryptoboxSpeed.BALANCED;
    public boolean debugShowMask = true;


    private boolean CryptoBoxDetected = false;
    private boolean ColumnDetected = false;
    private int[] CryptoBoxPositions = new int[3];


    Scalar lower = new Scalar(90, 135, 25);
    Scalar upper = new Scalar(130, 250, 150);

    public static void main(String[] args)
    {
        new CryptoboxDetector2();
    }

    public CryptoboxDetector2()
    {
        CVManager.runOn(this);
    }

    @Override
    public void analyze(Mat rgba)
    {
        Mat workingMat = new Mat();
        Mat mask1  = new Mat();
        Mat mask2  = new Mat();
        Mat mask  = new Mat();
        Mat hsv  = new Mat();
        Mat structure  = new Mat();
        Mat hierarchy = new Mat();
        Mat kernel = Mat.ones(5,5,CvType.CV_32F);

        Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_BGRA2RGBA);

        Size initSize= rgba.size();
        Size newSize  = new Size(initSize.width * downScaleFactor, initSize.height * downScaleFactor);
        rgba.copyTo(workingMat);


        Imgproc.resize(workingMat, workingMat,newSize);
        Imgproc.putText(workingMat,newSize.toString() + speed.toString(),new Point(5,15),0,0.6,new Scalar(0,255,255),2);
        if(rotateMat){
            Mat tempBefore = workingMat.t();

            Core.flip(tempBefore, workingMat, 1); //mRgba.t() is the transpose

            tempBefore.release();
        }



        List<MatOfPoint> contours = new ArrayList<>();
        List<Rect> boxes = new ArrayList<>();

        Imgproc.erode(workingMat, workingMat,kernel);
        Imgproc.dilate(workingMat, workingMat,kernel);
        Imgproc.cvtColor(workingMat,hsv,Imgproc.COLOR_RGB2HSV);


        switch(detectionMode){
            case HSV_RED:

                Scalar lower1 = new Scalar(0,150,100);
                Scalar upper1 = new Scalar(20,255,255);

                Scalar lower2 = new Scalar(140,100,100);
                Scalar upper2 = new Scalar(179,255,255);


                Core.inRange(hsv,lower1,upper1,mask1);


                Core.inRange(hsv,lower2,upper2,mask2);

                Core.addWeighted(mask1,1.0, mask2,1.0, 0.0, mask);

                break;
            case HSV_BLUE:


                Scalar lower = new Scalar(90, 135, 25);
                Scalar upper = new Scalar(130, 250, 150);


                Core.inRange(hsv,lower,upper,mask);
                break;
        }



        switch (speed){
            case VERY_FAST:
                Imgproc.blur(hsv,hsv,new Size(3,3));
                structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,30));
                Imgproc.morphologyEx(mask,mask,Imgproc.MORPH_CLOSE, structure);
                break;
            case FAST:
                Imgproc.blur(hsv,hsv,new Size(4,4));
                structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,30));
                Imgproc.morphologyEx(mask,mask,Imgproc.MORPH_CLOSE, structure);
                break;

            case BALANCED:
                Imgproc.blur(hsv,hsv,new Size(5,5));
                structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,40));
                Imgproc.morphologyEx(mask,mask,Imgproc.MORPH_CLOSE, structure);
                break;


            case SLOW:
                Imgproc.blur(hsv,hsv,new Size(7,7));
                structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,55));
                Imgproc.morphologyEx(mask,mask,Imgproc.MORPH_CLOSE, structure);
                break;

            case VERY_SLOW:
                Imgproc.blur(hsv,hsv,new Size(8,8));
                structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,60));
                Imgproc.morphologyEx(mask,mask,Imgproc.MORPH_CLOSE, structure);
                break;
        }




        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        for(MatOfPoint c : contours) {
            if(Imgproc.contourArea(c) >= newSize.height / 4 * 30) { //Filter by area
                Rect column = Imgproc.boundingRect(c);
                int ratio = Math.abs(column.height / column.width);

                if(ratio > 1.5) { //Check to see if the box is tall
                    boxes.add(column); //If all true add the box to array
                }
            }
        }
        for(Rect box : boxes) {
            Imgproc.rectangle(workingMat,new Point(box.x,box.y),new Point(box.x+box.width,box.y+box.height),new Scalar(255,0,0),2);
        }

        Collections.sort(boxes, new Comparator<Rect>() {
            @Override
            public int compare(Rect rect, Rect t1) {
                if(rect.x > t1.x){
                    return 1;
                }else if(rect.x < t1.x){
                    return -1;
                }else{
                    return 0;
                }
            }
        });

        CryptoBoxDetected = boxes.size() >=4;
        if(CryptoBoxDetected){
            Point left = drawSlot(0,boxes);
            Point center = drawSlot(1,boxes);
            Point right = drawSlot(2,boxes);

            CryptoBoxPositions[0] = (int)left.x;
            CryptoBoxPositions[1] = (int)center.x;
            CryptoBoxPositions[2] = (int)right.x;

            Imgproc.putText(workingMat, "Left", new Point(left.x - 10, left.y - 20), 0,0.8, new Scalar(0,255,255),2);
            Imgproc.circle(workingMat,left,5,new Scalar(0,255,255), 3);

            Imgproc.putText(workingMat, "Center", new Point(center.x - 10, center.y - 20), 0,0.8, new Scalar(0,255,255),2);
            Imgproc.circle(workingMat,center, 5,new Scalar(0,255,255), 3);

            Imgproc.putText(workingMat, "Right", new Point(right.x - 10, right.y - 20), 0,0.8, new Scalar(0,255,255),2);
            Imgproc.circle(workingMat,right, 5,new Scalar(0,255,255), 3);
        }else{
            for(int i=0;i<boxes.size() - 1;i++){
                Point collumn = drawSlot(i,boxes);
                Imgproc.circle(workingMat,collumn,5,new Scalar(0,255,255), 3);
                if(i<3){
                    CryptoBoxPositions[i] = (int)collumn.x;
                }
            }

            ColumnDetected = boxes.size() > 1;
        }

        if(rotateMat){

            Mat tempAfter = workingMat.t();

            Core.flip(tempAfter, workingMat, 0); //mRgba.t() is the transpose

            tempAfter.release();
        }

        Imgproc.resize(workingMat, workingMat, initSize);


        new CVJPanel(workingMat, "Working", 0, 0);


    }


    public Point drawSlot(int slot, List<Rect> boxes){
        Rect leftColumn = boxes.get(slot); //Get the pillar to the left
        Rect rightColumn = boxes.get(slot + 1); //Get the pillar to the right

        int leftX = leftColumn.x; //Get the X Coord
        int rightX = rightColumn.x; //Get the X Coord

        int drawX = ((rightX - leftX) / 2) + leftX; //Calculate the point between the two
        int drawY = leftColumn.height + leftColumn.y; //Calculate Y Coord. We wont use this in our bot's opetation, buts its nice for drawing

        return new Point(drawX, drawY);
    }

    public ArrayList ones(int width, int height) {
        ArrayList output = new ArrayList();
        for(int i = 1; i <= height; i++) {
            ArrayList row = new ArrayList();
            for(int j = 1; i <= width; i++) {
                row.add(1);
            }
            output.add(row);
        }
        return output;
    }

    public int[] getCryptoBoxPositions() {
        return CryptoBoxPositions;
    }

    public int getCryptoBoxLeftPosition() {
        return CryptoBoxPositions[0];
    }

    public int getCryptoBoxCenterPosition() {
        return CryptoBoxPositions[1];
    }

    public int getCryptoBoxRightPosition() {
        return CryptoBoxPositions[2];
    }

    public boolean isCryptoBoxDetected() {
        return CryptoBoxDetected;
    }

    public boolean isColumnDetected() {
        return ColumnDetected;
    }

}