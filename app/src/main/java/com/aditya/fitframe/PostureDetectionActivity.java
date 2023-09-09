package com.aditya.fitframe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PointF;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class PostureDetectionActivity extends AppCompatActivity implements ImageAnalysis.Analyzer{

    private PreviewView cameraXPreviewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Button generateButton;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    PoseDetector poseDetector;
    private PoseDetectorOptions options;

    private List<Float> adjustedAngles = new ArrayList<>();
    private ImageAnalysis imageAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posture_detection);

        generateButton = findViewById(R.id.generateButton);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraXPreviewView = findViewById(R.id.cameraXPreviewView);

        options = new PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE).build();
        poseDetector = PoseDetection.getClient(options);

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (adjustedAngles.isEmpty()) {
                    Toast.makeText(PostureDetectionActivity.this, "Not enough data acquired. Please wait", Toast.LENGTH_SHORT).show();
                } else {
                    float[] floatArray = new float[adjustedAngles.size()];
                    int i = 0;
                    for (Float f : adjustedAngles) {
                        floatArray[i++] = (f != null ? f : Float.NaN);
                    }
                    Intent intent = new Intent(PostureDetectionActivity.this, PostureResultActivity.class);
                    intent.putExtra("adjustedAngles", floatArray);
                    startActivity(intent);
                }
            }
        });



        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());


    }

    Executor getExecutor(){
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(cameraXPreviewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // Video capture use case
        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        // Image analysis use case
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(getExecutor(), this);

        //bind to lifecycle:
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        @SuppressLint("UnsafeOptInUsageError")
        Image mediaImage = image.getImage();
       // System.out.println("Running Analyzer");

        if (mediaImage != null) {
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
            Task<Pose> result =
                    poseDetector.process(inputImage)
                            .addOnSuccessListener(
                                    new OnSuccessListener<Pose>() {
                                        @Override
                                        public void onSuccess(Pose pose) {
                                            PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
                                            PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
                                            PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
                                            PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
                                            PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
                                            PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
                                            if (rightShoulder != null && leftShoulder != null && rightHip != null && leftHip != null && leftKnee != null && rightKnee != null) {
                                                PointF rightShoulderPosition = rightShoulder.getPosition();
                                                PointF leftShoulderPosition = leftShoulder.getPosition();
                                                PointF leftKneePosition = leftKnee.getPosition();
                                                PointF rightKneePosition = rightKnee.getPosition();
                                                PointF leftHipPosition = leftHip.getPosition();
                                                PointF rightHipPosition = rightHip.getPosition();

                                                float middlePointShoulderX = (leftShoulderPosition.x + rightShoulderPosition.x) / 2;
                                                float middlePointShoulderY = (leftShoulderPosition.y + rightShoulderPosition.y) / 2;

                                                float midHipX = (leftHipPosition.x + rightHipPosition.x) / 2;
                                                float midHipY = (leftHipPosition.y + rightHipPosition.y) / 2;

                                                float midKneeX = (leftKneePosition.x + rightKneePosition.x) / 2;
                                                float midKneeY = (leftKneePosition.y + rightKneePosition.y) / 2;

                                                PointF middlePointShoulder = new PointF(middlePointShoulderX, middlePointShoulderY);
                                                PointF midHip = new PointF(midHipX, midHipY);
                                                PointF middleKnee = new PointF(midKneeX, midKneeY);

                                                PointF vecShoulderToHip = new PointF(midHip.x - middlePointShoulder.x, midHip.y - middlePointShoulder.y);

                                                PointF vecKneeToHip = new PointF(middleKnee.x - midHip.x, middleKnee.y - midHip.y);

                                                float dotProduct = vecShoulderToHip.x * vecKneeToHip.x + vecShoulderToHip.y * vecKneeToHip.y;

                                                float lenShoulderToHip = (float) Math.sqrt(vecShoulderToHip.x * vecShoulderToHip.x + vecShoulderToHip.y * vecShoulderToHip.y);
                                                float lenKneeToHip = (float) Math.sqrt(vecKneeToHip.x * vecKneeToHip.x + vecKneeToHip.y * vecKneeToHip.y);

                                                float cosAngle = dotProduct / (lenShoulderToHip * lenKneeToHip);

                                                float angle = (float) Math.toDegrees(Math.acos(cosAngle));
                                                float adjustedAngle = 180.0f - angle;
                                                Toast.makeText(PostureDetectionActivity.this, "Adjusted posture angle: " + adjustedAngle, Toast.LENGTH_SHORT).show();
                                                adjustedAngles.add(adjustedAngle);

                                            }else {
                                                Toast.makeText(PostureDetectionActivity.this, "Not enough key-points found", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            image.close();
                                          //  Toast.makeText(PostureDetectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    });
            image.close();

        }

    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

