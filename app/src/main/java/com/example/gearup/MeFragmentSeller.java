package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MeFragmentSeller extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, addressTextView;
    private Uri imageUri;
    private final int PICK_IMAGE_REQUEST = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me_seller, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileImageView = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.textView);
        emailTextView = view.findViewById(R.id.textView2);
        addressTextView = view.findViewById(R.id.addressTextView);

        loadSellerInfo();

        // Set up upload button
        Button uploadButton = view.findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> openImageChooser());

        view.findViewById(R.id.logoutbutton).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Logout button clicked", Toast.LENGTH_SHORT).show();
            logoutUser();
        });

        // Handle settings button click
        view.findViewById(R.id.settingbutton).setOnClickListener(v -> {
            // Replace the current fragment with SettingsFragment
            Fragment settingsFragment = new SettingsFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, settingsFragment) // The container where your fragments are displayed
                    .addToBackStack(null) // Optional, if you want to allow back navigation
                    .commit();
        });

        // Handle editprofile button click
        view.findViewById(R.id.editbutton).setOnClickListener(v -> {
            // Create an instance of EditProfileBuyerFragment
            Fragment editProfileBuyerFragment = new EditProfileBuyerFragment();

            // Replace the current fragment with EditProfileBuyerFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, editProfileBuyerFragment) // The container where your fragments are displayed
                    .addToBackStack(null) // Optional, to allow back navigation
                    .commit();
        });

        return view;
    }

    private void loadSellerInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("sellers").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String fullName = firstName + " " + lastName;
                        String email = currentUser.getEmail();
                        String address = document.getString("address");
                        String profileImageUrl = document.getString("profileImageUrl");

                        nameTextView.setText(fullName);
                        emailTextView.setText(email);
                        addressTextView.setText(address != null ? address : "Address not available");
                        Glide.with(this).load(profileImageUrl).into(profileImageView);  // Load profile image
                    } else {
                        nameTextView.setText("No seller info found.");
                    }
                } else {
                    nameTextView.setText("Failed to load seller info.");
                }
            });
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
                imageUri = data.getData();
                profileImageView.setImageURI(imageUri);
                uploadImageToFirebase(imageUri);
            } else {
                Toast.makeText(getContext(), "Image selection failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebase(Uri uri) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_images/" + userId + ".jpg");

            storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                storageReference.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    db.collection("sellers").document(userId).update("profileImageUrl", downloadUrl.toString())
                            .addOnSuccessListener(aVoid -> {
                                // Optionally update UI after saving the URL
                                Glide.with(this).load(downloadUrl).into(profileImageView);
                            });
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        } else {
            Toast.makeText(getContext(), "Failed to logout: Activity not found", Toast.LENGTH_SHORT).show();
        }
    }
}
