package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddressesFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private AddressAdapter addressAdapter;
    private List<Map<String, Object>> addressList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addresses, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recycler_view_address);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(addressList, new AddressAdapter.OnAddressClickListener() {
            @Override
            public void onDefaultButtonClick(int position) {
                Toast.makeText(getActivity(), "Default address set at position: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteButtonClick(int position) {
                deleteAddress(position);
            }
        });
        recyclerView.setAdapter(addressAdapter);

        view.findViewById(R.id.btn_add_new_address).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddNewAddressActivity.class);
            startActivity(intent);
        });

        loadUserAddresses();  // Load addresses when fragment is created

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload the addresses when returning to this fragment
        loadUserAddresses();
    }

    private void loadUserAddresses() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("buyers").document(userId).collection("addresses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        if (!documents.isEmpty()) {
                            addressList.clear();  // Clear the existing list before adding new data
                            for (DocumentSnapshot document : documents) {
                                Map<String, Object> addressData = document.getData();
                                // Add the document ID to the address data map
                                addressData.put("documentId", document.getId());
                                addressList.add(addressData);
                            }
                            addressAdapter.notifyDataSetChanged();  // Notify adapter about data changes
                        } else {
                            Toast.makeText(getActivity(), "No addresses found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error loading addresses.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteAddress(int position) {
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> address = addressList.get(position);

        // Ensure the documentId exists in the address
        String documentId = (String) address.get("documentId");
        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(getActivity(), "Error: Document ID not found. Unable to delete address.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed with deletion
        db.collection("buyers").document(userId).collection("addresses").document(documentId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Remove the address from the list and update RecyclerView
                        addressList.remove(position);
                        addressAdapter.notifyItemRemoved(position);
                        Toast.makeText(getActivity(), "Address deleted successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to delete address. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

