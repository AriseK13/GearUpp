package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class BuyerProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView nameTextView, emailTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buyer_profile, container, false);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind UI components
        nameTextView = view.findViewById(R.id.textView);  // for full name
        emailTextView = view.findViewById(R.id.textView2);  // for email

        // Load buyer information from Firestore
        loadBuyerInfo();

        // Add click listener for the edit button
        view.findViewById(R.id.editbutton).setOnClickListener(v -> {
            // Start BuyerEditProfile activity
            Intent intent = new Intent(getActivity(), BuyerEditProfile.class);
            startActivity(intent);
        });

        // Add click listener for logout
        view.findViewById(R.id.logoutbutton).setOnClickListener(v -> {
            // Debugging Toast
            Toast.makeText(getContext(), "Logout button clicked", Toast.LENGTH_SHORT).show();
            logoutUser();
        });

        return view;
    }

    private void loadBuyerInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Retrieve buyer data from Firestore
            db.collection("buyers").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String fullName = firstName + " " + lastName;
                        String email = currentUser.getEmail();

                        // Set full name and email in the TextViews
                        nameTextView.setText(fullName);
                        emailTextView.setText(email);
                    } else {
                        // Handle case where document does not exist
                        nameTextView.setText("No buyer info found.");
                    }
                } else {
                    // Handle task failure
                    nameTextView.setText("Failed to load buyer info.");
                }
            });
        }
    }

    private void logoutUser() {
        // Sign out from Firebase
        mAuth.signOut();

        if (getActivity() != null) {
            // Redirect to Login activity
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
            startActivity(intent);

            // Close the current activity
            getActivity().finish();
        } else {
            // Debugging if getActivity is null
            Toast.makeText(getContext(), "Failed to logout: Activity not found", Toast.LENGTH_SHORT).show();
        }
    }
}
