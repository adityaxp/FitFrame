package com.aditya.fitframe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HeightResultActivity extends AppCompatActivity {

    private ImageButton changeUnitImageButton;
    private ImageButton backButton;
    private EditText fHEditText, kToLEditText, kToL_REditText, wToKEditText, wsToKEditText, nToWEditText, sWEditText;
    private ImageView remeasureImageView;
    private Button submitButton;
    int unitFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_result2);

        changeUnitImageButton = findViewById(R.id.changeUnitImageButton);
        fHEditText = findViewById(R.id.fHEditText);
        kToLEditText = findViewById(R.id.kToLEditText);
        kToL_REditText = findViewById(R.id.kToL_REditText);
        wToKEditText = findViewById(R.id.wToKEditText);
        wsToKEditText = findViewById(R.id.wsToKEditText);
        nToWEditText = findViewById(R.id.nToWEditText);
        sWEditText = findViewById(R.id.sWEditText);
        remeasureImageView = findViewById(R.id.remeasureImageView);
        submitButton = findViewById(R.id.submitButton);
        backButton = findViewById(R.id.backButton);
        setMeasurements(unitFlag);

        remeasureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HeightResultActivity.this, MeasureDimensionsActivity.class);
                startActivity(intent);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadData();
            }
        });

        changeUnitImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUnitOptionsDialog();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void uploadData(){
        CustomLoadingDialog customLoadingDialog = new CustomLoadingDialog(HeightResultActivity.this, "");
        customLoadingDialog.customLoadingDialogShow();
        byte[] byteArray = getIntent().getByteArrayExtra("ImageData");

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        StorageReference imageReference = storageReference.child(timeStamp+".jpg");
        StorageReference storageReference1 = storageReference.child("images/"+timeStamp+".jpg");

        imageReference.getName().equals(storageReference1.getName());
        imageReference.getPath().equals(storageReference1.getPath());


        UploadTask uploadTask = imageReference.putBytes(byteArray);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                customLoadingDialog.customLoadingDialogDismiss();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri urlImage = uriTask.getResult();
                String downloadImageURL = urlImage.toString();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                float fullHeight = getIntent().getFloatExtra("FullHeight", 0f);
                float kneeToAnkleL = getIntent().getFloatExtra("LeftKneeToAnkleDistance", 0f);
                float kneeToAnkleR = getIntent().getFloatExtra("RightKneeToAnkleDistance", 0f);
                float waistToKneeR = getIntent().getFloatExtra("RightHipToKneeDistance", 0f);
                float waistToKneeL = getIntent().getFloatExtra("LeftHipToKneeDistance", 0f);
                float neckToWaist = getIntent().getFloatExtra("ShoulderToHipDistance", 0f);
                float shoulderWidth = getIntent().getFloatExtra("RSToLS", 0f);

                List<Float> heightDataList = Arrays.asList(fullHeight, kneeToAnkleL, kneeToAnkleR, waistToKneeR, waistToKneeL, neckToWaist, shoulderWidth);
                ReportData reportData = new ReportData(heightDataList, date, downloadImageURL);
                FirebaseDatabase.getInstance("https://fitframe-5a029-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("CloudSaves").child(mAuth.getCurrentUser().getUid()).child(timeStamp).setValue(reportData);
                customLoadingDialog.customLoadingDialogDismiss();
                Toast.makeText(HeightResultActivity.this, "Upload successful!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HeightResultActivity.this, "Server bucket full!", Toast.LENGTH_SHORT).show();
                customLoadingDialog.customLoadingDialogDismiss();

            }
        });
    }

    private void showUnitOptionsDialog() {
        String[] units = {"Feet", "Inches", "Centimeters"};
        AlertDialog.Builder builder = new AlertDialog.Builder(HeightResultActivity.this);
        builder.setTitle("Change Unit");
        builder.setSingleChoiceItems(units, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                unitFlag = i;
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setMeasurements(unitFlag);
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void setMeasurements(int flag) {
        float fullHeight = getIntent().getFloatExtra("FullHeight", 0f);
        float kneeToAnkleL = getIntent().getFloatExtra("LeftKneeToAnkleDistance", 0f);
        float kneeToAnkleR = getIntent().getFloatExtra("RightKneeToAnkleDistance", 0f);
        float waistToKneeR = getIntent().getFloatExtra("RightHipToKneeDistance", 0f);
        float waistToKneeL = getIntent().getFloatExtra("LeftHipToKneeDistance", 0f);
        float neckToWaist = getIntent().getFloatExtra("ShoulderToHipDistance", 0f);
        float shoulderWidth = getIntent().getFloatExtra("RSToLS", 0f);


        if(flag == 0){
            fHEditText.setText(String.valueOf(fullHeight));
            kToLEditText.setText(String.valueOf(kneeToAnkleL));
            kToL_REditText.setText(String.valueOf(kneeToAnkleR));
            wToKEditText.setText(String.valueOf(waistToKneeR));
            wsToKEditText.setText(String.valueOf(waistToKneeL));
            nToWEditText.setText(String.valueOf(neckToWaist));
            sWEditText.setText(String.valueOf(shoulderWidth));
        }else if(flag == 1){
            fHEditText.setText(String.valueOf(fullHeight * 12));
            kToLEditText.setText(String.valueOf(kneeToAnkleL * 12));
            kToL_REditText.setText(String.valueOf(kneeToAnkleR * 12));
            wToKEditText.setText(String.valueOf(waistToKneeR * 12));
            wsToKEditText.setText(String.valueOf(waistToKneeL * 12));
            nToWEditText.setText(String.valueOf(neckToWaist * 12));
            sWEditText.setText(String.valueOf(shoulderWidth* 12));
        }else if(flag == 2){
            fHEditText.setText(String.valueOf(fullHeight * 30.48));
            kToLEditText.setText(String.valueOf(kneeToAnkleL * 30.48));
            kToL_REditText.setText(String.valueOf(kneeToAnkleR * 30.48));
            wToKEditText.setText(String.valueOf(waistToKneeR * 30.48));
            wsToKEditText.setText(String.valueOf(waistToKneeL * 30.48));
            nToWEditText.setText(String.valueOf(neckToWaist * 30.48));
            sWEditText.setText(String.valueOf(shoulderWidth* 30.48));
        }else {
            Toast.makeText(this, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MeasureDimensionsActivity.class);
        startActivity(intent);
        finishAffinity();
        finish();
    }
}