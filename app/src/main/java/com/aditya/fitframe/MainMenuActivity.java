package com.aditya.fitframe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainMenuActivity extends AppCompatActivity {

    TextView userProfileTextView, cloudSavesTextView, aboutTextView, logOutTextView;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        userProfileTextView = findViewById(R.id.userProfileTextView);
        cloudSavesTextView = findViewById(R.id.cloudSavesTextView);
        aboutTextView = findViewById(R.id.aboutTextView);
        logOutTextView = findViewById(R.id.logOutTextView);
        backButton = findViewById(R.id.backButton);

        userProfileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenuActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });
        cloudSavesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenuActivity.this, CloudSavesActivity.class);
                startActivity(intent);
            }
        });
        aboutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        logOutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
                startActivity(intent);
                finishAffinity();
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}