package com.example.gearup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EditProfileBuyerFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextInputEditText firstNameEditText, lastNameEditText, mobileEditText, addressEditText, emailEditText;
    private TextInputEditText currentPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private Button updateButton;
    private TextView userNameTextView, userEmailTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile_buyer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind views
        bindViews(view);

        // Load and display user data
        loadUserData();

        // Handle update button click
        updateButton.setOnClickListener(v -> updateProfileAndPassword());
    }

    private void bindViews(View view) {
        firstNameEditText = view.findViewById(R.id.etFirstName);
        lastNameEditText = view.findViewById(R.id.etLastName);
        mobileEditText = view.findViewById(R.id.etPhoneNumber);
        addressEditText = view.findViewById(R.id.etAddress);
        emailEditText = view.findViewById(R.id.etEmail);
        currentPasswordEditText = view.findViewById(R.id.etCurrentPassword);
        newPasswordEditText = view.findViewById(R.id.etNewPassword);
        confirmNewPasswordEditText = view.findViewById(R.id.etConfirmNewPassword);
        updateButton = view.findViewById(R.id.btnUpdate);
        userNameTextView = view.findViewById(R.id.tvUserName);
        userEmailTextView = view.findViewById(R.id.tvUserEmail);
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("DEBUG", "User not authenticated");
            return;
        }

        db.collection("buyers").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Object> userData = task.getResult().getData();
                        if (userData != null) {
                            populateUserData(userData);
                        } else {
                            Log.e("DEBUG", "No user data found");
                        }
                    } else {
                        Log.e("DEBUG", "Error loading user data", task.getException());
                    }
                });
    }

    private void populateUserData(Map<String, Object> userData) {
        String firstName = (String) userData.get("firstName");
        String lastName = (String) userData.get("lastName");
        String mobile = (String) userData.get("mobileNumber");
        String email = (String) userData.get("email");
        ArrayList<Map<String, String>> addresses = (ArrayList<Map<String, String>>) userData.get("addresses");

        userNameTextView.setText(String.format("%s %s", firstName, lastName));
        userEmailTextView.setText(email);

        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);
        mobileEditText.setText(mobile);
        emailEditText.setText(email);

        if (addresses != null && !addresses.isEmpty()) {
            addressEditText.setText(addresses.get(0).get("address"));
        } else {
            Log.e("DEBUG", "No addresses found");
        }
    }

    private void updateProfileAndPassword() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String mobile = mobileEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (areProfileFieldsEmpty(firstName, lastName, mobile, address, email)) {
            Toast.makeText(getContext(), "All fields except password fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        updateUserProfile(firstName, lastName, mobile, address, email);

        if (!TextUtils.isEmpty(currentPassword) || !TextUtils.isEmpty(newPassword) || !TextUtils.isEmpty(confirmNewPassword)) {
            updatePassword(currentPassword, newPassword, confirmNewPassword);
        }
    }

    private boolean areProfileFieldsEmpty(String firstName, String lastName, String mobile, String address, String email) {
        return TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                TextUtils.isEmpty(mobile) || TextUtils.isEmpty(address) || TextUtils.isEmpty(email);
    }

    private void updateUserProfile(String firstName, String lastName, String mobile, String address, String email) {
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("firstName", firstName);
        userUpdates.put("lastName", lastName);
        userUpdates.put("mobileNumber", mobile);
        userUpdates.put("email", email);

        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("fullName", firstName + " " + lastName);
        addressMap.put("mobileNumber", mobile);
        addressMap.put("address", address);

        userUpdates.put("addresses", Collections.singletonList(addressMap));

        db.collection("buyers").document(uid).update(userUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                Log.e("DEBUG", "Error updating profile", task.getException());
            }
        });
    }

    private void updatePassword(String currentPassword, String newPassword, String confirmNewPassword) {
        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
            Toast.makeText(getContext(), "All password fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(getContext(), "New password and confirm password do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), currentPassword))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
                                    Log.e("DEBUG", "Error changing password", updateTask.getException());
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Re-authentication failed", Toast.LENGTH_SHORT).show();
                            Log.e("DEBUG", "Error re-authenticating user", task.getException());
                        }
                    });
        }
    }
}
