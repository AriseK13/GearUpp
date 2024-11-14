package com.example.gearup;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddNewAddressActivity extends AppCompatActivity {

    private EditText etFullName, etPhoneNumber, etRegionProvinceCity, etPostalCode, etStreetDetails;
    private RadioGroup rgLabel;
    private Switch switchDefaultAddress;
    private Button btnSubmitAddress;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_address);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize the views
        etFullName = findViewById(R.id.et_full_name);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        etRegionProvinceCity = findViewById(R.id.et_region_province_city);
        etPostalCode = findViewById(R.id.et_postal_code);
        etStreetDetails = findViewById(R.id.et_street_details);
        rgLabel = findViewById(R.id.rg_label);
        switchDefaultAddress = findViewById(R.id.switch_default_address);
        btnSubmitAddress = findViewById(R.id.btn_submit_address);

        // Set a click listener for the submit button
        btnSubmitAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get values from the input fields
                String fullName = etFullName.getText().toString().trim();
                String phoneNumber = etPhoneNumber.getText().toString().trim();
                String regionProvinceCity = etRegionProvinceCity.getText().toString().trim();
                String postalCode = etPostalCode.getText().toString().trim();
                String streetDetails = etStreetDetails.getText().toString().trim();
                boolean isDefaultAddress = switchDefaultAddress.isChecked();

                // Get the label selection
                String label = "";
                int selectedLabelId = rgLabel.getCheckedRadioButtonId();
                if (selectedLabelId == R.id.rb_work) {
                    label = "Work";
                } else if (selectedLabelId == R.id.rb_home) {
                    label = "Home";
                }

                // Validate inputs
                if (fullName.isEmpty() || phoneNumber.isEmpty() || regionProvinceCity.isEmpty() ||
                        postalCode.isEmpty() || streetDetails.isEmpty()) {
                    Toast.makeText(AddNewAddressActivity.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save the address to Firestore
                saveAddressToFirestore(fullName, phoneNumber, regionProvinceCity, postalCode, streetDetails, label, isDefaultAddress);
            }
        });
    }

    private void saveAddressToFirestore(String fullName, String phoneNumber, String regionProvinceCity,
                                        String postalCode, String streetDetails, String label, boolean isDefaultAddress) {
        // Get the current user ID
        String userId = mAuth.getCurrentUser().getUid();

        // Create a new address map
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("fullName", fullName);
        addressMap.put("phoneNumber", phoneNumber);
        addressMap.put("regionProvinceCity", regionProvinceCity);
        addressMap.put("postalCode", postalCode);
        addressMap.put("streetDetails", streetDetails);
        addressMap.put("label", label);
        addressMap.put("isDefaultAddress", isDefaultAddress);

        // Save the address to Firestore under the user's collection
        db.collection("buyers").document(userId)
                .collection("addresses")
                .add(addressMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddNewAddressActivity.this, "Address saved successfully", Toast.LENGTH_SHORT).show();
                    // Optionally finish the activity or reset fields
                    finish(); // Return to the previous screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddNewAddressActivity.this, "Error saving address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

