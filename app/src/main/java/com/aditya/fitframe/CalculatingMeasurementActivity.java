package com.aditya.fitframe;


import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Matrix;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aditya.fitframe.ml.LiteModelMovenetSingleposeLightningTfliteFloat164;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CalculatingMeasurementActivity extends AppCompatActivity {

    private LiteModelMovenetSingleposeLightningTfliteFloat164 model;
    private TensorImage tensorImage;
    private ImageProcessor imageProcessor , imageProcessor1;
    private Paint paint;
    int maskWidth, maskHeight, maxHeight, maxWidth;
    ByteBuffer maskBuffer = ByteBuffer.allocate(0);
    String midEndPoints;
    PoseDetector poseDetector;

    int x = 0;
    int y= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculating_measurement);

        Intent intent = getIntent();
        String imageURI = intent.getStringExtra("ImageURI");
        SelfieSegmenterOptions options =
                new SelfieSegmenterOptions.Builder()
                        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                        .enableRawSizeMask()
                        .build();
        Segmenter segmenter = Segmentation.getClient(options);

        AccuratePoseDetectorOptions options1 =
                new AccuratePoseDetectorOptions.Builder()
                        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
                        .build();

        poseDetector = PoseDetection.getClient(options1);

        tensorImage = new TensorImage(DataType.UINT8);
        imageProcessor = new ImageProcessor.Builder().add(new ResizeOp(192, 192, ResizeOp.ResizeMethod.BILINEAR)).build();
        imageProcessor1 = new ImageProcessor.Builder().add(new ResizeOp(8064, 8064, ResizeOp.ResizeMethod.BILINEAR)).build();
        byte[] byteArray = getIntent().getByteArrayExtra("ImageData");
        paint = new Paint();

        paint.setColor(Color.RED);

        try {
            model = LiteModelMovenetSingleposeLightningTfliteFloat164.newInstance(this);


        } catch (IOException e) {}

        if(imageURI != null){

            try
            {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver() , Uri.parse(imageURI));

                maxHeight = bitmap.getHeight();
                maxWidth = bitmap.getWidth();

                InputImage image = InputImage.fromBitmap(bitmap, 0);

                Task<SegmentationMask> result =
                        segmenter.process(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<SegmentationMask>() {
                                            @Override
                                            public void onSuccess(SegmentationMask mask) {
                                                maskBuffer = mask.getBuffer();
                                                maskWidth = mask.getWidth() ;
                                                maskHeight = mask.getHeight();
                                                Bitmap bitmap1 = getResizedBitmap(bitmap, mask.getWidth(), mask.getHeight());
                                                Bitmap bitmap2 = generateMaskImage(bitmap1);
                                                Mat src = new Mat();
                                                Utils.bitmapToMat(bitmap2, src);
                                                Size newSize = new Size(8064, 8064);
                                                Mat dst = new Mat(newSize, src.type());
                                                Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGBA2BGR);
                                                Imgproc.resize(src, dst, newSize);
                                                Mat grayScaleImage = new Mat();
                                                Imgproc.cvtColor(dst, grayScaleImage, Imgproc.COLOR_BGR2GRAY);
                                                Mat edgedImage = new Mat();
                                                Imgproc.Canny(grayScaleImage, edgedImage, 30, 200);
                                                List<MatOfPoint> contours = new ArrayList<>();
                                                Mat hierarchy = new Mat();
                                                Imgproc.findContours(edgedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                                                Imgproc.drawContours(dst, contours, -1, new Scalar(0, 255, 0), 10);
                                                //Imgproc.line(dst, new Point(0, 0), new Point(dst.width(), dst.height()), new Scalar(0, 0, 255), 5);



                                                for (MatOfPoint contour : contours) {
                                                    Rect rect = Imgproc.boundingRect(contour);
                                                    Imgproc.rectangle(dst, rect, new Scalar(255, 0, 0), 20);
                                                    x = rect.x;
                                                     y = rect.y;
                                                }


                                                Bitmap dstBitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
                                                Utils.matToBitmap(dst, dstBitmap);
                                                midEndPoints = "";
                                                InputImage image = InputImage.fromBitmap(dstBitmap, 0);
                                                Task<Pose> result = poseDetector.process(image)
                                                        .addOnSuccessListener(
                                                                new OnSuccessListener<Pose>() {
                                                                    @Override
                                                                    public void onSuccess(Pose pose) {
                                                                        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
                                                                        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
                                                                        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
                                                                        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
                                                                        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
                                                                        PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
                                                                        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
                                                                        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
                                                                        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
                                                                        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
                                                                        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
                                                                        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
                                                                        PoseLandmark leftEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER);
                                                                        PoseLandmark rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER);


                                                                        if (leftAnkle != null && rightAnkle != null && rightShoulder != null && leftShoulder != null && rightWrist != null && leftWrist != null && leftKnee != null && rightKnee != null) {
                                                                            PointF leftAnklePosition = leftAnkle.getPosition();
                                                                            PointF rightAnklePosition = rightAnkle.getPosition();
                                                                            PointF rightShoulderPosition = rightShoulder.getPosition();
                                                                            PointF leftShoulderPosition = leftShoulder.getPosition();
                                                                            PointF leftElbowPosition = leftElbow.getPosition();
                                                                            PointF rightElbowPosition = rightElbow.getPosition();
                                                                            PointF rightWristPosition = rightWrist.getPosition();
                                                                            PointF leftWristPosition = leftWrist.getPosition();
                                                                            PointF leftHipPosition = leftHip.getPosition();
                                                                            PointF rightHipPosition = rightHip.getPosition();
                                                                            PointF leftKneePosition = leftKnee.getPosition();
                                                                            PointF rightKneePosition = rightKnee.getPosition();
                                                                            PointF leftEyeInnerPosition = leftEyeInner.getPosition();
                                                                            PointF rightEyeInnerPosition = rightEyeInner.getPosition();


                                                                            float middlePointX = (leftAnklePosition.x + rightAnklePosition.x) / 2;
                                                                            float middlePointY = (leftAnklePosition.y + rightAnklePosition.y) / 2;

                                                                            float middlePointShoulderX = (leftShoulderPosition.x + rightShoulderPosition.x) / 2;
                                                                            float middlePointShoulderY = (leftShoulderPosition.y + rightShoulderPosition.y) / 2;

                                                                            float midHipX = (leftHipPosition.x + rightHipPosition.x) / 2;
                                                                            float midHipY = (leftHipPosition.y + rightHipPosition.y) / 2;

                                                                            float middleEyePointX = (leftEyeInnerPosition.x + rightEyeInnerPosition.x) / 2;
                                                                            float middleEyePointY = (leftEyeInnerPosition.y + rightEyeInnerPosition.y) / 2;


                                                                            PointF middlePoint = new PointF(middlePointX, middlePointY);
                                                                            PointF middleShoulderPoint = new PointF(middlePointShoulderX, middlePointShoulderY);
                                                                            PointF middleHipPoint = new PointF(midHipX, midHipY);
                                                                            PointF forehead = new PointF(middleEyePointX, middleEyePointY);


                                                                            Mat newSrc = new Mat();
                                                                            Utils.bitmapToMat(dstBitmap, newSrc);
                                                                            Imgproc.line(newSrc, new Point(rightShoulderPosition.x, rightShoulderPosition.y), new Point(leftShoulderPosition.x, leftShoulderPosition.y), new Scalar(0, 0, 255), 50);
                                                                            Imgproc.line(newSrc, new Point(leftShoulderPosition.x, leftShoulderPosition.y), new Point(leftElbowPosition.x, leftElbowPosition.y), new Scalar(0, 0, 255), 50);
                                                                            Imgproc.line(newSrc, new Point(rightShoulderPosition.x, rightShoulderPosition.y), new Point(rightElbowPosition.x, rightElbowPosition.y), new Scalar(0, 0, 255), 50);
                                                                            Imgproc.line(newSrc, new Point(rightWristPosition.x, rightWristPosition.y), new Point(rightElbowPosition.x, rightElbowPosition.y), new Scalar(0, 0, 255), 50);
                                                                            Imgproc.line(newSrc, new Point(leftWristPosition.x, leftWristPosition.y), new Point(leftElbowPosition.x, leftElbowPosition.y), new Scalar(0, 0, 255), 50);
                                                                            Imgproc.line(newSrc, new Point(leftHipPosition.x, leftHipPosition.y), new Point(rightHipPosition.x, rightHipPosition.y), new Scalar(0, 0, 255), 50);
                                                                            Imgproc.line(newSrc, new Point(middleShoulderPoint.x, middleShoulderPoint.y), new Point(middleHipPoint.x, middleHipPoint.y), new Scalar(0, 0, 255), 50);

                                                                            Imgproc.line(newSrc, new Point(leftHipPosition.x, leftHipPosition.y), new Point(leftKneePosition.x, rightKneePosition.y), new Scalar(0, 0, 255), 50);
                                                                            Imgproc.line(newSrc, new Point(rightHipPosition.x, rightHipPosition.y), new Point(rightKneePosition.x, rightKneePosition.y), new Scalar(0, 0, 255), 50);

                                                                            Imgproc.line(newSrc, new Point(rightAnklePosition.x, rightAnklePosition.y), new Point(rightKneePosition.x, rightKneePosition.y), new Scalar(0, 0, 255), 50);
                                                                            Imgproc.line(newSrc, new Point(leftAnklePosition.x, leftAnklePosition.y), new Point(leftKneePosition.x, leftKneePosition.y), new Scalar(0, 0, 255), 50);
                                                                            Imgproc.line(newSrc, new Point(forehead.x, forehead.y), new Point(middleShoulderPoint.x, middleShoulderPoint.y), new Scalar(0, 0, 255), 50);
                                                                            //Imgproc.line(newSrc, new Point(middlePoint.x, middlePoint.y), new Point(middleHipPoint.x, middleHipPoint.y), new Scalar(0, 0, 255), 50);
                                                                            float xDistance = forehead.x - middlePoint.x;
                                                                            float yDistance = forehead.y - middlePoint.y;
                                                                            float heightDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance) + 1152;

                                                                            xDistance = rightShoulderPosition.x - leftShoulderPosition.x;
                                                                            yDistance = rightShoulderPosition.y - leftShoulderPosition.y;
                                                                            float shoulderDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                            xDistance = rightShoulderPosition.x - rightElbowPosition.x;
                                                                            yDistance = rightShoulderPosition.y - rightElbowPosition.y;
                                                                            float rightShoulderToElbowDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                            xDistance = leftShoulderPosition.x - leftElbowPosition.x;
                                                                            yDistance = leftShoulderPosition.y - leftElbowPosition.y;
                                                                            float leftShoulderToElbowDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                            xDistance = middleShoulderPoint.x - middleHipPoint.x;
                                                                            yDistance = middleShoulderPoint.y - middleHipPoint.y;
                                                                            float ShoulderToHipDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                            xDistance = rightHipPosition.x - rightKneePosition.x;
                                                                            yDistance = rightHipPosition.y - rightKneePosition.y;
                                                                            float rightHipToKneeDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                            xDistance = leftHipPosition.x - leftKneePosition.x;
                                                                            yDistance = leftHipPosition.y - leftKneePosition.y;
                                                                            float leftHipToKneeDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                            xDistance = rightKneePosition.x - rightAnklePosition.x;
                                                                            yDistance = rightKneePosition.y - rightAnklePosition.y;
                                                                            float rightKneeToAnkleDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                            xDistance = leftKneePosition.x - leftAnklePosition.x;
                                                                            yDistance = leftKneePosition.y - leftAnklePosition.y;
                                                                            float leftKneeToAnkleDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                            Intent intent1 = new Intent(CalculatingMeasurementActivity.this, HeightResultActivity.class);
                                                                            intent1.putExtra("FullHeight", heightDistance /1152);
                                                                            intent1.putExtra("RSToLS", shoulderDistance/1152);
                                                                            intent1.putExtra("RightShoulderToElbowDistance",  rightShoulderToElbowDistance / 1152);
                                                                            intent1.putExtra("LeftShoulderToElbowDistance", leftShoulderToElbowDistance / 1152);
                                                                            intent1.putExtra("ShoulderToHipDistance", ShoulderToHipDistance/ 1152);
                                                                            intent1.putExtra("RightHipToKneeDistance", rightHipToKneeDistance / 1152);
                                                                            intent1.putExtra("RightKneeToAnkleDistance", rightKneeToAnkleDistance/1152);
                                                                            intent1.putExtra("LeftHipToKneeDistance", leftHipToKneeDistance/1152);
                                                                            intent1.putExtra("LeftKneeToAnkleDistance", leftKneeToAnkleDistance/1152);
                                                                            Bitmap newDstBitmap = Bitmap.createBitmap(newSrc.cols(), newSrc.rows(), Bitmap.Config.ARGB_8888);
                                                                            Utils.matToBitmap(newSrc, newDstBitmap);
                                                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                                                            resizeBitmapForPreview(newDstBitmap).compress(Bitmap.CompressFormat.PNG, 100, stream);
                                                                            byte[] byteArray = stream.toByteArray();
                                                                            intent1.putExtra("ImageData", byteArray);
                                                                            startActivity(intent1);



                                                                        }

                                                                    }
                                                                })
                                                        .addOnFailureListener(
                                                                new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {

                                                                    }
                                                                });




                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                            }
                                        });


            }
            catch (Exception e) {



            }




        }else {

            try{
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            InputImage image = InputImage.fromBitmap(bmp, 0);

            Task<SegmentationMask> result =
                    segmenter.process(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<SegmentationMask>() {
                                        @Override
                                        public void onSuccess(SegmentationMask mask) {
                                            maskBuffer = mask.getBuffer();
                                            maskWidth = mask.getWidth() ;
                                            maskHeight = mask.getHeight();
                                            Bitmap bitmap1 = getResizedBitmap(bmp, mask.getWidth(), mask.getHeight());
                                            Bitmap bitmap2 = generateMaskImage(bitmap1);
                                            Mat src = new Mat();
                                            Utils.bitmapToMat(bitmap2, src);
                                            Size newSize = new Size(8064, 8064);
                                            Mat dst = new Mat(newSize, src.type());
                                            Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGBA2BGR);
                                            Imgproc.resize(src, dst, newSize);
                                            Mat grayScaleImage = new Mat();
                                            Imgproc.cvtColor(dst, grayScaleImage, Imgproc.COLOR_BGR2GRAY);
                                            Mat edgedImage = new Mat();
                                            Imgproc.Canny(grayScaleImage, edgedImage, 30, 200);
                                            List<MatOfPoint> contours = new ArrayList<>();
                                            Mat hierarchy = new Mat();
                                            Imgproc.findContours(edgedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                                            Imgproc.drawContours(dst, contours, -1, new Scalar(0, 255, 0), 10);
                                            //Imgproc.line(dst, new Point(0, 0), new Point(dst.width(), dst.height()), new Scalar(0, 0, 255), 5);



                                            for (MatOfPoint contour : contours) {
                                                Rect rect = Imgproc.boundingRect(contour);
                                                Imgproc.rectangle(dst, rect, new Scalar(255, 0, 0), 20);
                                                x = rect.x;
                                                y = rect.y;
                                            }


                                            Bitmap dstBitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
                                            Utils.matToBitmap(dst, dstBitmap);
                                            midEndPoints = "";
                                            InputImage image = InputImage.fromBitmap(dstBitmap, 0);
                                            Task<Pose> result = poseDetector.process(image)
                                                    .addOnSuccessListener(
                                                            new OnSuccessListener<Pose>() {
                                                                @Override
                                                                public void onSuccess(Pose pose) {
                                                                    PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
                                                                    PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
                                                                    PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
                                                                    PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
                                                                    PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
                                                                    PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
                                                                    PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
                                                                    PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
                                                                    PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
                                                                    PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
                                                                    PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
                                                                    PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
                                                                    PoseLandmark leftEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER);
                                                                    PoseLandmark rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER);


                                                                    if (leftAnkle != null && rightAnkle != null && rightShoulder != null && leftShoulder != null && rightWrist != null && leftWrist != null && leftKnee != null && rightKnee != null) {
                                                                        PointF leftAnklePosition = leftAnkle.getPosition();
                                                                        PointF rightAnklePosition = rightAnkle.getPosition();
                                                                        PointF rightShoulderPosition = rightShoulder.getPosition();
                                                                        PointF leftShoulderPosition = leftShoulder.getPosition();
                                                                        PointF leftElbowPosition = leftElbow.getPosition();
                                                                        PointF rightElbowPosition = rightElbow.getPosition();
                                                                        PointF rightWristPosition = rightWrist.getPosition();
                                                                        PointF leftWristPosition = leftWrist.getPosition();
                                                                        PointF leftHipPosition = leftHip.getPosition();
                                                                        PointF rightHipPosition = rightHip.getPosition();
                                                                        PointF leftKneePosition = leftKnee.getPosition();
                                                                        PointF rightKneePosition = rightKnee.getPosition();
                                                                        PointF leftEyeInnerPosition = leftEyeInner.getPosition();
                                                                        PointF rightEyeInnerPosition = rightEyeInner.getPosition();



                                                                        float middlePointX = (leftAnklePosition.x + rightAnklePosition.x) / 2;
                                                                        float middlePointY = (leftAnklePosition.y + rightAnklePosition.y) / 2;

                                                                        float middlePointShoulderX = (leftShoulderPosition.x + rightShoulderPosition.x) / 2;
                                                                        float middlePointShoulderY = (leftShoulderPosition.y + rightShoulderPosition.y) / 2;

                                                                        float midHipX = (leftHipPosition.x + rightHipPosition.x) / 2;
                                                                        float midHipY = (leftHipPosition.y + rightHipPosition.y) / 2;

                                                                        float middleEyePointX = (leftEyeInnerPosition.x + rightEyeInnerPosition.x) / 2;
                                                                        float middleEyePointY = (leftEyeInnerPosition.y + rightEyeInnerPosition.y) / 2;




                                                                        PointF middlePoint = new PointF(middlePointX, middlePointY);
                                                                        PointF middleShoulderPoint = new PointF(middlePointShoulderX, middlePointShoulderY);
                                                                        PointF middleHipPoint = new PointF(midHipX, midHipY);
                                                                        PointF forehead = new PointF(middleEyePointX, middleEyePointY);



                                                                        // Toast.makeText(CalculatingMeasurementActivity.this,  middlePoint.x+","+ middlePoint.y, Toast.LENGTH_SHORT).show();
                                                                        Mat newSrc = new Mat();
                                                                        Utils.bitmapToMat(dstBitmap, newSrc);
                                                                        // Imgproc.line(newSrc, new Point(Math.round(newSrc.width() / 2), 0), new Point((int) middlePoint.x, (int) middlePoint.y), new Scalar(0, 0, 255), 5);
                                                                        Imgproc.line(newSrc, new Point(rightShoulderPosition.x, rightShoulderPosition.y), new Point(leftShoulderPosition.x, leftShoulderPosition.y), new Scalar(0, 0, 255), 50);
                                                                        Imgproc.line(newSrc, new Point(leftShoulderPosition.x, leftShoulderPosition.y), new Point(leftElbowPosition.x, leftElbowPosition.y), new Scalar(0, 0, 255), 50);
                                                                        Imgproc.line(newSrc, new Point(rightShoulderPosition.x, rightShoulderPosition.y), new Point(rightElbowPosition.x, rightElbowPosition.y), new Scalar(0, 0, 255), 50);
                                                                        Imgproc.line(newSrc, new Point(rightWristPosition.x, rightWristPosition.y), new Point(rightElbowPosition.x, rightElbowPosition.y), new Scalar(0, 0, 255), 50);
                                                                        Imgproc.line(newSrc, new Point(leftWristPosition.x, leftWristPosition.y), new Point(leftElbowPosition.x, leftElbowPosition.y), new Scalar(0, 0, 255), 50);
                                                                        Imgproc.line(newSrc, new Point(leftHipPosition.x, leftHipPosition.y), new Point(rightHipPosition.x, rightHipPosition.y), new Scalar(0, 0, 255), 50);
                                                                        Imgproc.line(newSrc, new Point(middleShoulderPoint.x, middleShoulderPoint.y), new Point(middleHipPoint.x, middleHipPoint.y), new Scalar(0, 0, 255), 50);

                                                                        Imgproc.line(newSrc, new Point(leftHipPosition.x, leftHipPosition.y), new Point(leftKneePosition.x, rightKneePosition.y), new Scalar(0, 0, 255), 50);
                                                                        Imgproc.line(newSrc, new Point(rightHipPosition.x, rightHipPosition.y), new Point(rightKneePosition.x, rightKneePosition.y), new Scalar(0, 0, 255), 50);

                                                                        Imgproc.line(newSrc, new Point(rightAnklePosition.x, rightAnklePosition.y), new Point(rightKneePosition.x, rightKneePosition.y), new Scalar(0, 0, 255), 50);
                                                                        Imgproc.line(newSrc, new Point(leftAnklePosition.x, leftAnklePosition.y), new Point(leftKneePosition.x, leftKneePosition.y), new Scalar(0, 0, 255), 50);
                                                                        Imgproc.line(newSrc, new Point(forehead.x, forehead.y), new Point(middleShoulderPoint.x, middleShoulderPoint.y), new Scalar(0, 0, 255), 50);
                                                                        //Imgproc.line(newSrc, new Point(middlePoint.x, middlePoint.y), new Point(middleHipPoint.x, middleHipPoint.y), new Scalar(0, 0, 255), 50);

                                                                        float xDistance = forehead.x - middlePoint.x;
                                                                        float yDistance = forehead.y - middlePoint.y;
                                                                        float heightDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance) + 1152;

                                                                        xDistance = rightShoulderPosition.x - leftShoulderPosition.x;
                                                                        yDistance = rightShoulderPosition.y - leftShoulderPosition.y;
                                                                        float shoulderDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                        xDistance = rightShoulderPosition.x - rightElbowPosition.x;
                                                                        yDistance = rightShoulderPosition.y - rightElbowPosition.y;
                                                                        float rightShoulderToElbowDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                        xDistance = leftShoulderPosition.x - leftElbowPosition.x;
                                                                        yDistance = leftShoulderPosition.y - leftElbowPosition.y;
                                                                        float leftShoulderToElbowDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                        xDistance = middleShoulderPoint.x - middleHipPoint.x;
                                                                        yDistance = middleShoulderPoint.y - middleHipPoint.y;
                                                                        float ShoulderToHipDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                        xDistance = rightHipPosition.x - rightKneePosition.x;
                                                                        yDistance = rightHipPosition.y - rightKneePosition.y;
                                                                        float rightHipToKneeDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                        xDistance = leftHipPosition.x - leftKneePosition.x;
                                                                        yDistance = leftHipPosition.y - leftKneePosition.y;
                                                                        float leftHipToKneeDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                        xDistance = rightKneePosition.x - rightAnklePosition.x;
                                                                        yDistance = rightKneePosition.y - rightAnklePosition.y;
                                                                        float rightKneeToAnkleDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                        xDistance = leftKneePosition.x - leftAnklePosition.x;
                                                                        yDistance = leftKneePosition.y - leftAnklePosition.y;
                                                                        float leftKneeToAnkleDistance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);

                                                                        Intent intent1 = new Intent(CalculatingMeasurementActivity.this, HeightResultActivity.class);
                                                                        intent1.putExtra("FullHeight", heightDistance /1152);
                                                                        intent1.putExtra("RSToLS", shoulderDistance/1152);
                                                                        intent1.putExtra("RightShoulderToElbowDistance",  rightShoulderToElbowDistance / 1152);
                                                                        intent1.putExtra("LeftShoulderToElbowDistance", leftShoulderToElbowDistance / 1152);
                                                                        intent1.putExtra("ShoulderToHipDistance", ShoulderToHipDistance/ 1152);
                                                                        intent1.putExtra("RightHipToKneeDistance", rightHipToKneeDistance / 1152);
                                                                        intent1.putExtra("RightKneeToAnkleDistance", rightKneeToAnkleDistance/1152);
                                                                        intent1.putExtra("LeftHipToKneeDistance", leftHipToKneeDistance/1152);
                                                                        intent1.putExtra("LeftKneeToAnkleDistance", leftKneeToAnkleDistance/1152);
                                                                        Bitmap newDstBitmap = Bitmap.createBitmap(newSrc.cols(), newSrc.rows(), Bitmap.Config.ARGB_8888);
                                                                        Utils.matToBitmap(newSrc, newDstBitmap);
                                                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                                                        resizeBitmapForPreview(newDstBitmap).compress(Bitmap.CompressFormat.PNG, 100, stream);
                                                                        byte[] byteArray = stream.toByteArray();
                                                                        intent1.putExtra("ImageData", byteArray);
                                                                        startActivity(intent1);



                                                                    }

                                                                }
                                                            })
                                                    .addOnFailureListener(
                                                            new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    // Task failed with an exception
                                                                    // ...
                                                                }
                                                            });

                                            //  String midEndPoints = getAnkleLandMarks(dstBitmap);



                                            // Bitmap bitmap3 = getResizedBitmap(bitmap2, 8064, 8064);
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });


        }
            catch (Exception e) {



        }



    }



    }


    private Bitmap resizeBitmapForPreview(Bitmap srcBitmap){
        int maxSize = 500;

        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();

        int dstWidth, dstHeight;
        if (srcWidth >= srcHeight) {
            dstWidth = maxSize;
            dstHeight = (int) ((float) srcHeight / srcWidth * maxSize);
        } else {
            dstHeight = maxSize;
            dstWidth = (int) ((float) srcWidth / srcHeight * maxSize);
        }

        Bitmap dstBitmap = Bitmap.createScaledBitmap(srcBitmap, dstWidth, dstHeight, true);

        return dstBitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
    private Bitmap generateMaskImage(Bitmap image) {
        Bitmap maskBitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), image.getConfig());
        float scaleX = (float) image.getWidth()/ maskWidth ;
        float scaleY = (float)  image.getHeight()/ maskHeight;


        for (int y = 0; y < maskHeight; y++) {
            for (int x = 0; x < maskWidth; x++) {
                int foregroundConfidence = (int) ((1.0 - maskBuffer.getFloat()) * 255);

//                int scaledX = Math.round(x * scaleX);
//                int scaledY = Math.round(y * scaleY);

                maskBitmap.setPixel(x, y, Color.argb(foregroundConfidence, 0, 0, 0));
            }
        }
            maskBuffer.rewind();
            return mergeBitmaps(image, maskBitmap);

    }

    private Bitmap mergeBitmaps(Bitmap bmp1, Bitmap bmp2){
        Bitmap merged = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(merged);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);

        return merged;
    }


    public String getAnkleLandMarks(Bitmap bitmap){

        final String[] points = {""};
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Task<Pose> result = poseDetector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<Pose>() {
                            @Override
                            public void onSuccess(Pose pose) {
                                PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
                                PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
                                if (leftAnkle != null && rightAnkle != null) {
                                    PointF leftAnklePosition = leftAnkle.getPosition();
                                    PointF rightAnklePosition = rightAnkle.getPosition();
                                    float middlePointX = (leftAnklePosition.x + rightAnklePosition.x) / 2;
                                    float middlePointY = (leftAnklePosition.y + rightAnklePosition.y) / 2;
                                    PointF middlePoint = new PointF(middlePointX, middlePointY);
                                    points[0] = (int) middlePoint.x +","+ (int) middlePoint.y;
                                    Toast.makeText(CalculatingMeasurementActivity.this,  middlePoint.x+","+ middlePoint.y, Toast.LENGTH_SHORT).show();

                                }

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });

        return points[0];

    }


    public float[][] getKeyPointsArray(float[] originalArray){
        ArrayList<Float> newArray = new ArrayList<Float>();
        int sum = 0;
        for (float n : originalArray) {
            if (sum != 2) {
                newArray.add(n);
                sum += 1;
            } else {
                sum = 0;
                continue;
            }
        }


        final float[] arr = new float[newArray.size()];
        int index = 0;
        for (final Float value: newArray) {
            arr[index++] = value;
        }


        int n = arr.length / 2; // Number of pairs
        float[][] newArray1 = new float[n][2];

        for (int i = 0; i < n; i++) {
            newArray1[i][0] = arr[2*i];
            newArray1[i][1] = arr[2*i + 1];
        }



        return newArray1;
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        model.close();
    }
}


//                tensorImage.load(bitmap);
//                tensorImage = imageProcessor.process(tensorImage);
//
//
//                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 192, 192, 3}, DataType.UINT8);
//                inputFeature0.loadBuffer(tensorImage.getBuffer());
//                LiteModelMovenetSingleposeLightningTfliteFloat164.Outputs outputs = model.process(inputFeature0);
//                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//                float[] output = outputFeature0.getFloatArray();
//                // Toast.makeText(this, output.toString(), Toast.LENGTH_SHORT).show();
//                System.out.println(output.length);
//                System.out.println(Arrays.toString(output));
//                Bitmap mutableBitmap =  bitmap.copy(Bitmap.Config.ARGB_8888, true);
//                Canvas canvas = new Canvas(mutableBitmap);
//                int height = bitmap.getHeight();
//                int width = bitmap.getWidth();
//                int x = 0;
//                float[][] keypointArray =  getKeyPointsArray(output);
//                System.out.println(keypointArray.length);
//                System.out.println(Arrays.deepToString(keypointArray));
//                while (x <= 49)
//                {
//                   if(output[x+2] > 0.45){
//                       canvas.drawCircle(output[x+1]*width, output[x]*height, 10f, paint);
//                   }
//                   x+=3;
//                }
//                test.setImageBitmap(mutableBitmap);