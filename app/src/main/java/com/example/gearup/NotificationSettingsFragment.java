package com.example.gearup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

public class NotificationSettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification_settings, container, false);

        // Find the Switch for Push Notifications
        Switch pushNotificationSwitch = view.findViewById(R.id.push_notifications_switch);

        // Set up the listener for the switch (optional functionality)
        pushNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Handle push notification toggle here
        });

        return view;
    }
}
