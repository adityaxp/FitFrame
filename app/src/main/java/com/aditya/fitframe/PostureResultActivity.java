package com.aditya.fitframe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PostureResultActivity extends AppCompatActivity {

    private EditText postureAngleEditText, resultAngleEditText;
    private Button nextButton;
    private ImageButton backImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posture_result);

        resultAngleEditText = findViewById(R.id.resultAngleEditText);
        postureAngleEditText = findViewById(R.id.postureAngleEditText);
        nextButton = findViewById(R.id.nextButton);
        backImageButton = findViewById(R.id.backImageButton);

        float[] adjustedAnglesArray = getIntent().getFloatArrayExtra("adjustedAngles");
        HashMap<Float, Integer> frequencyMap = new HashMap<Float, Integer>();

        int maxFrequency = 0;
        float mostFrequentValue = 0;


        if (adjustedAnglesArray != null) {
            for (float value : adjustedAnglesArray) {
                if (frequencyMap.containsKey(value)) {
                    frequencyMap.put(value, frequencyMap.get(value) + 1);
                } else {
                    frequencyMap.put(value, 1);
                }
            }

            for (Map.Entry<Float, Integer> entry : frequencyMap.entrySet()) {
                if (entry.getValue() > maxFrequency) {
                    maxFrequency = entry.getValue();
                    mostFrequentValue = entry.getKey();
                }
            }

            postureAngleEditText.setText(String.valueOf(mostFrequentValue));
            resultAngleEditText.setText(String.valueOf(getTrunkColor(mostFrequentValue)));

        }

        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostureResultActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public String getTrunkColor(float trunkAngle) {
        if (trunkAngle < 20) {
            return "Good Posture";
        } else if (trunkAngle <= 60) {
            return "Average Posture";
        } else {
            return "Bad Posture";
        }
    }
}