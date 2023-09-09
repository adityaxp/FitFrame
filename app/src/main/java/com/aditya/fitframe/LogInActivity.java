package com.aditya.fitframe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button nextButton;
    private EditText emailEditText;
    private EditText passwordEditText;

    private TextView signUpTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        mAuth = FirebaseAuth.getInstance();

        nextButton = findViewById(R.id.nextButton);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpTextView = findViewById(R.id.signUpTextView);


        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new  Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        CustomLoadingDialog customLoadingDialog = new CustomLoadingDialog(this, "");
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                customLoadingDialog.customLoadingDialogShow();
                if (emailEditText.getText().toString().equals("") || passwordEditText.getText().toString().equals("")) {
                    Toast.makeText(LogInActivity.this, "Fields can't be empty!", Toast.LENGTH_SHORT).show();
                    customLoadingDialog.customLoadingDialogDismiss();
                } else {
                    mAuth.signInWithEmailAndPassword(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim())
                            .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        customLoadingDialog.customLoadingDialogDismiss();
                                        Toast.makeText(LogInActivity.this, "Authentication Successful!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LogInActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        customLoadingDialog.customLoadingDialogDismiss();
                                        Toast.makeText(LogInActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });




    }
}