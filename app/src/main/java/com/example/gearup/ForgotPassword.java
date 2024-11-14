package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton, backButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialization
        emailEditText = findViewById(R.id.edtForgotPasswordEmail);
        resetPasswordButton = findViewById(R.id.btnReset);
        backButton = findViewById(R.id.btnForgotPasswordBack);
        progressBar = findViewById(R.id.forgetPasswordProgressbar);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Back Button Listener
        backButton.setOnClickListener(v -> onBackPressed());

        // Reset Password Button Listener
        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email field can't be empty");
                return;
            }

            // Call method to reset password
            resetPassword(email);
        });
    }

    private void resetPassword(String email) {
        progressBar.setVisibility(View.VISIBLE);
        resetPasswordButton.setVisibility(View.INVISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    resetPasswordButton.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPassword.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        // Navigate to login after successful reset
                        Intent intent = new Intent(ForgotPassword.this, Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ForgotPassword.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
