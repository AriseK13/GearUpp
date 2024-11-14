package com.example.gearup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class EditProfileSellerFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextInputEditText firstNameEditText, lastNameEditText, phoneEditText, shopNameEditText, addressEditText, emailEditText;
    private Button updateButton;
    private TextView userNameTextView, userEmailTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile_seller, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind views
        bindViews(view);

        // Load and display seller data
        loadSellerData();

        // Handle update button click
        updateButton.setOnClickListener(v -> updateProfile());
    }

    private void bindViews(View view) {
        firstNameEditText = view.findViewById(R.id.etFirstName);
        lastNameEditText = view.findViewById(R.id.etLastName);
        phoneEditText = view.findViewById(R.id.etPhoneNumber);
        shopNameEditText = view.findViewById(R.id.etShopName);
        addressEditText = view.findViewById(R.id.etAddress);
        emailEditText = view.findViewById(R.id.etEmail);
        updateButton = view.findViewById(R.id.btnUpdate);
        userNameTextView = view.findViewById(R.id.tvUserName);
        userEmailTextView = view.findViewById(R.id.tvUserEmail);
    }

    private void loadSellerData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("DEBUG", "User not authenticated");
            return;
        }

        db.collection("sellers").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Object> sellerData = task.getResult().getData();
                        if (sellerData != null) {
                            populateSellerData(sellerData);
                        } else {
                            Log.e("DEBUG", "No seller data found");
                        }
                    } else {
                        Log.e("DEBUG", "Error loading seller data", task.getException());
                    }
                });
    }

    private void populateSellerData(Map<String, Object> sellerData) {
        String firstName = (String) sellerData.get("firstName");
        String lastName = (String) sellerData.get("lastName");
        String phone = (String) sellerData.get("phone");
        String shopName = (String) sellerData.get("shopName");
        String address = (String) sellerData.get("address");
        String email = mAuth.getCurrentUser().getEmail(); // Use current authenticated email

        userNameTextView.setText(String.format("%s %s", firstName, lastName));
        userEmailTextView.setText(email);

        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);
        phoneEditText.setText(phone);
        shopNameEditText.setText(shopName);
        addressEditText.setText(address);
        emailEditText.setText(email);
    }

    private void updateProfile() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String shopName = shopNameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(shopName) || TextUtils.isEmpty(address)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> sellerUpdates = new HashMap<>();
        sellerUpdates.put("firstName", firstName);
        sellerUpdates.put("lastName", lastName);
        sellerUpdates.put("phone", phone);
        sellerUpdates.put("shopName", shopName);
        sellerUpdates.put("address", address);

        db.collection("sellers").document(uid).update(sellerUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                Log.e("DEBUG", "Error updating profile", task.getException());
            }
        });
    }
}
