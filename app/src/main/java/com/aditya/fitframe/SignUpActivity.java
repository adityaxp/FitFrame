package com.aditya.fitframe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button nextButton;
    private TextInputEditText emailEditText, nameEditText;
    private TextInputEditText passwordEditText;
    private TextView logInTextView;

    private AutoCompleteTextView ageGroupAutoCompleteTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nextButton = findViewById(R.id.nextButton);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nameEditText = findViewById(R.id.nameEditText);
        ageGroupAutoCompleteTextView = findViewById(R.id.ageAutoCompleteTextView);
        logInTextView = findViewById(R.id.logInTextView);
        mAuth = FirebaseAuth.getInstance();

        String ageGroup[] = {"5-10 years", "10-20 years", "20-30 years", "30-50 years", "50+ years"};

        ArrayAdapter<String> ageGroupAdapter = new ArrayAdapter<String>(this, R.layout.drop_down_items, ageGroup);
        ageGroupAutoCompleteTextView.setAdapter(ageGroupAdapter);

        logInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new  Intent(SignUpActivity.this, LogInActivity.class);
                startActivity(intent);
            }
        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameEditText.getText().toString().equals("") || emailEditText.getText().toString().equals("") || passwordEditText.getText().toString().equals("")){
                    Toast.makeText(SignUpActivity.this, "Fields can't be empty", Toast.LENGTH_SHORT).show();
                }else{
                    mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        SignUpData signUpData = new SignUpData(nameEditText.getText().toString(), emailEditText.getText().toString(), ageGroupAutoCompleteTextView.getText().toString());
                                        FirebaseDatabase.getInstance("https://fitframe-5a029-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("User").child(mAuth.getCurrentUser().getUid()).setValue(signUpData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(SignUpActivity.this, "Sign up successful", Toast.LENGTH_SHORT).show();
                                                FirebaseAuth.getInstance().signOut();
                                                Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                                                startActivity(intent);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SignUpActivity.this, "Failed!!!", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });



                }
            }
        });

    }
}