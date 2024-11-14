package com.example.gearup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Retrieve user role from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("userRole", "buyer");  // Default to "buyer" if no role is set

        // Find the "Account and Security" button
        Button accountButton = view.findViewById(R.id.accountbutton);

        // Set OnClickListener to navigate based on user role
        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                // Check the user's role and navigate accordingly
                if ("buyer".equals(userRole)) {
                    // Navigate to EditProfileBuyerFragment
                    fragmentTransaction.replace(R.id.fragment_container, new EditProfileBuyerFragment());
                } else if ("seller".equals(userRole)) {
                    // Navigate to EditProfileSellerFragment
                    fragmentTransaction.replace(R.id.fragment_container, new EditProfileSellerFragment());
                }

                fragmentTransaction.addToBackStack(null);  // Allow back navigation
                fragmentTransaction.commit();
            }
        });

        // Set up the other buttons as usual

        Button addressesButton = view.findViewById(R.id.addressesbutton);
        addressesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new AddressesFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        Button rulesButton = view.findViewById(R.id.rulesbutton);
        rulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CommunityRulesActivity.class);
                startActivity(intent);
            }
        });

        Button notificationButton = view.findViewById(R.id.notificationbutton);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new NotificationSettingsFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return view;
    }
}
