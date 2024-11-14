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

public class MeFragmentBuyer extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView nameTextView, emailTextView, addressTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me_buyer, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameTextView = view.findViewById(R.id.textView);
        emailTextView = view.findViewById(R.id.textView2);

        loadBuyerInfo();

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

    private void loadBuyerInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("buyers").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String fullName = firstName + " " + lastName;
                        String email = currentUser.getEmail();


                        nameTextView.setText(fullName);
                        emailTextView.setText(email);

                    } else {
                        nameTextView.setText("No buyer info found.");
                    }
                } else {
                    nameTextView.setText("Failed to load buyer info.");
                }
            });
        }
    }

    private void logoutUser() {
        mAuth.signOut();

        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
            startActivity(intent);
            getActivity().finish();
        } else {
            Toast.makeText(getContext(), "Failed to logout: Activity not found", Toast.LENGTH_SHORT).show();
        }
    }
}
