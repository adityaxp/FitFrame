package com.aditya.fitframe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, nameEditText;
    private AutoCompleteTextView ageGroupAutoCompleteTextView;
    private ImageButton backImageButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        backImageButton = findViewById(R.id.backImageButton);
        emailEditText = findViewById(R.id.emailEditText);
        nameEditText = findViewById(R.id.nameEditText);
        ageGroupAutoCompleteTextView = findViewById(R.id.ageAutoCompleteTextView);

        databaseReference = FirebaseDatabase.getInstance("https://fitframe-5a029-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameEditText.setText(dataSnapshot.child("userName").getValue().toString());
                emailEditText.setText(dataSnapshot.child("userEmail").getValue().toString());
                ageGroupAutoCompleteTextView.setText(dataSnapshot.child("ageGroup").getValue().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserProfileActivity.this, "Failed to retrieve profile data", Toast.LENGTH_SHORT).show();
            }
        });


        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}