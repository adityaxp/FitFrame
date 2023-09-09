package com.aditya.fitframe;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;

public class MeasureDimensionsActivity extends AppCompatActivity {

    private ImageView capturePhotoImageView;
    private ImageView uploadImageView;
    private Uri uri;

    private Bitmap photo;

    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_dimensions);

        if(OpenCVLoader.initDebug()){
        //   Toast.makeText(this, "OpenCV Loaded", Toast.LENGTH_SHORT).show();
        }

        capturePhotoImageView = findViewById(R.id.capturePhotoImageView);
        uploadImageView = findViewById(R.id.uploadImageView);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MeasureDimensionsActivity.this, CalculatingMeasurementActivity.class);
                if(uri != null){
                    intent.putExtra("ImageURI", uri.toString());
                    startActivity(intent);
                }else if(photo != null){
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    intent.putExtra("ImageData", byteArray);
                    startActivity(intent);
                }
            }
        });

        capturePhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage(view);
            }
        });

        uploadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(view);
            }
        });
    }

    public void captureImage(View view){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 2);
    }

    public void selectImage(View view){
        Intent selectPhoto = new Intent(Intent.ACTION_PICK);
        selectPhoto.setType("image/*");
        startActivityForResult(selectPhoto, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                uri = data.getData();
                capturePhotoImageView.setImageURI(uri);
            } else {
                Toast.makeText(MeasureDimensionsActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK && data != null) {
                photo = (Bitmap) data.getExtras().get("data");
                capturePhotoImageView.setImageBitmap(photo);
            } else {
                Toast.makeText(MeasureDimensionsActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();


            }
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }
}