package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BuyerRegister extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText firstNameEditText, lastNameEditText, mobileNumberEditText, addressEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        firstNameEditText = findViewById(R.id.etfn);
        lastNameEditText = findViewById(R.id.etln);
        mobileNumberEditText = findViewById(R.id.etemobileno);
        addressEditText = findViewById(R.id.etaddress);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.cpassword);
        signupButton = findViewById(R.id.btn_signup);

        signupButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String mobileNumber = mobileNumberEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Check for empty fields
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(mobileNumber) || TextUtils.isEmpty(address) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(BuyerRegister.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(BuyerRegister.this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check password length and complexity
        if (password.length() < 6) {
            Toast.makeText(BuyerRegister.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.equals(confirmPassword)) {
            signupButton.setEnabled(false); // Disable button to prevent multiple clicks
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        signupButton.setEnabled(true); // Re-enable button
                        if (task.isSuccessful()) {
                            saveUserInformation(firstName, lastName, mobileNumber, address, email);
                        } else {
                            Toast.makeText(BuyerRegister.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(BuyerRegister.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserInformation(String firstName, String lastName, String mobileNumber, String address, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("mobileNumber", mobileNumber);
        user.put("email", email);
        user.put("role", "buyer");

        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("fullName", firstName + " " + lastName);
        addressMap.put("mobileNumber", mobileNumber);
        addressMap.put("address", address);

        user.put("addresses", Collections.singletonList(addressMap)); // Store address as a list

        db.collection("buyers").document(mAuth.getCurrentUser().getUid())
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(BuyerRegister.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    } else {
                        Toast.makeText(BuyerRegister.this, "Failed to register user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void navigateToLogin() {
        Intent intent = new Intent(BuyerRegister.this, Login.class);
        startActivity(intent);
        finish();
    }
}
