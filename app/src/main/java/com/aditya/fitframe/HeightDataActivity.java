package com.aditya.fitframe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HeightDataActivity extends AppCompatActivity {

    private ImageButton changeUnitImageButton;
    private ImageButton backButton;
    private TextView nameTextView, ageGroupTextView;
    private EditText fHEditText, kToLEditText, kToL_REditText, wToKEditText, wsToKEditText, nToWEditText, sWEditText;
    private ImageView imageView;
    int unitFlag = 0;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_data);


        changeUnitImageButton = findViewById(R.id.changeUnitImageButton);
        fHEditText = findViewById(R.id.fHEditText);
        kToLEditText = findViewById(R.id.kToLEditText);
        kToL_REditText = findViewById(R.id.kToL_REditText);
        wToKEditText = findViewById(R.id.wToKEditText);
        wsToKEditText = findViewById(R.id.wsToKEditText);
        nToWEditText = findViewById(R.id.nToWEditText);
        sWEditText = findViewById(R.id.sWEditText);
        imageView = findViewById(R.id.imageView);
        backButton = findViewById(R.id.backButton);
        nameTextView = findViewById(R.id.nameTextView);
        ageGroupTextView = findViewById(R.id.ageGroupTextView);

        setMeasurements(unitFlag);


        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://fitframe-5a029-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameTextView.setText("Name: " + dataSnapshot.child("userName").getValue().toString());
                ageGroupTextView.setText("Age Group: " + dataSnapshot.child("ageGroup").getValue().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HeightDataActivity.this, "Failed to retrieve profile data", Toast.LENGTH_SHORT).show();
            }
        });

        Glide.with(this)
                .load(getIntent().getStringExtra("ImageURL"))
                .into(imageView);

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


    private void showUnitOptionsDialog() {
        String[] units = {"Feet", "Inches", "Centimeters"};
        AlertDialog.Builder builder = new AlertDialog.Builder(HeightDataActivity.this);
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
}