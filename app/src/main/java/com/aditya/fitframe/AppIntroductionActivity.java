package com.aditya.fitframe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class AppIntroductionActivity extends AppCompatActivity {

    private TextView skipTextView, titleTextView, headingTextView;
    private Button nextButton;
    private ImageView introImageView;
    private Boolean flag = true;
    ImageButton counterOneButton, counterTwoButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_introduction);

        skipTextView = findViewById(R.id.skipTextView);
        nextButton = findViewById(R.id.nextButton);
        titleTextView = findViewById(R.id.titleTextView);
        introImageView = findViewById(R.id.introImageView);
        headingTextView = findViewById(R.id.headingTextView);
        counterTwoButton = findViewById(R.id.counterTwoButton);
        counterOneButton = findViewById(R.id.counterOneButton);


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag){
                    headingTextView.setText("Improve Your Posture");
                    titleTextView.setText("Get real-time feedback in seconds \nwith just a photo or a video.");
                    introImageView.setImageResource(R.drawable.intro2);
                    nextButton.setText("Previous");
                    counterOneButton.setImageResource(R.drawable.counteralt);
                    counterTwoButton.setImageResource(R.drawable.counter);
                    flag = false;
                }else {
                    headingTextView.setText("Know Your Body Better");
                    titleTextView.setText("Get accurate measurements in seconds \nwith just a photo.");
                    nextButton.setText("Next");
                    introImageView.setImageResource(R.drawable.intro1);
                    counterOneButton.setImageResource(R.drawable.counter);
                    counterTwoButton.setImageResource(R.drawable.counteralt);
                    flag = true;
                }
            }
        });

        skipTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppIntroductionActivity.this, LogInActivity.class);
                startActivity(intent);

            }
        });
    }
}