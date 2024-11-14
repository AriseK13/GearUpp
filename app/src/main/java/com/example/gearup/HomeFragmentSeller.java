package com.example.gearup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragmentSeller extends Fragment implements ProductAdapterBuyer.OnProductClickListener {
    private FirebaseFirestore db;
    private ViewPager2 viewPagerCentralComponents, viewPagerBody, viewPagerConnectors, viewPagerPeripherals;
    private ProductAdapterBuyer adapterCentralComponents, adapterBody, adapterConnectors, adapterPeripherals;
    private List<Product> centralComponentsList = new ArrayList<>();
    private List<Product> bodyList = new ArrayList<>();
    private List<Product> connectorsList = new ArrayList<>();
    private List<Product> peripheralsList = new ArrayList<>();

    private EditText searchBar;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_seller, container, false); // Changed to fragment_home_seller

        viewPagerCentralComponents = view.findViewById(R.id.viewPager_central_components);
        viewPagerBody = view.findViewById(R.id.viewPager_body);
        viewPagerConnectors = view.findViewById(R.id.viewPager_connectors);
        viewPagerPeripherals = view.findViewById(R.id.viewPager_peripherals);
        searchBar = view.findViewById(R.id.search_bar);

        ImageView iconCart = view.findViewById(R.id.icon_cart);
        iconCart.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CartActivity.class);
            startActivity(intent);
        });

        // Click listeners for See All buttons
        TextView textSeeAllCentral = view.findViewById(R.id.text_see_all_central);
        TextView textSeeAllBody = view.findViewById(R.id.text_see_all_body);
        TextView textSeeAllConnectors = view.findViewById(R.id.text_see_all_connectors);
        TextView textSeeAllPeripherals = view.findViewById(R.id.text_see_all_peripherals);

        textSeeAllConnectors.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ConnectorsActivity.class);
            startActivity(intent);
        });

        textSeeAllCentral.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CentralComponentsActivity.class);
            startActivity(intent);
        });

        textSeeAllBody.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), BodyActivity.class);
            startActivity(intent);
        });

        textSeeAllPeripherals.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PeripheralsActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        loadProducts();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    loadProducts();
                } else {
                    filterProducts(s.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadProducts() {
        db.collectionGroup("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        centralComponentsList.clear();
                        bodyList.clear();
                        connectorsList.clear();
                        peripheralsList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                product.setId(document.getId()); // Set the Firestore document ID
                                categorizeProduct(product);
                                loadSellerProfile(product);
                                Log.d("HomeFragmentSeller", "Loaded product: " + product.getName() + " with ID: " + product.getId());
                            }
                        }

                        setAdapters();
                    } else {
                        Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void categorizeProduct(Product product) {
        String category = product.getCategory();
        if (category == null) {
            Log.e("HomeFragmentSeller", "Product category is null for product: " + product.getName());
            return;
        }

        switch (category) {
            case "Central Components":
                centralComponentsList.add(product);
                break;
            case "Body":
                bodyList.add(product);
                break;
            case "Connectors":
                connectorsList.add(product);
                break;
            case "Peripherals":
                peripheralsList.add(product);
                break;
            default:
                Log.e("HomeFragmentSeller", "Unknown category: " + category + " for product: " + product.getName());
                break;
        }
    }

    private void setAdapters() {
        adapterCentralComponents = new ProductAdapterBuyer(new ArrayList<>(centralComponentsList), "Central Components", this);
        viewPagerCentralComponents.setAdapter(adapterCentralComponents);
        viewPagerCentralComponents.setOffscreenPageLimit(1);

        adapterBody = new ProductAdapterBuyer(new ArrayList<>(bodyList), "Body", this);
        viewPagerBody.setAdapter(adapterBody);
        viewPagerBody.setOffscreenPageLimit(1);

        adapterConnectors = new ProductAdapterBuyer(new ArrayList<>(connectorsList), "Connectors", this);
        viewPagerConnectors.setAdapter(adapterConnectors);
        viewPagerConnectors.setOffscreenPageLimit(1);

        adapterPeripherals = new ProductAdapterBuyer(new ArrayList<>(peripheralsList), "Peripherals", this);
        viewPagerPeripherals.setAdapter(adapterPeripherals);
        viewPagerPeripherals.setOffscreenPageLimit(1);

        Log.d("HomeFragmentSeller", "Total products loaded: " + (centralComponentsList.size() + bodyList.size() + connectorsList.size() + peripheralsList.size()));
    }

    private void filterProducts(String query) {
        List<Product> filteredCentralComponents = new ArrayList<>();
        List<Product> filteredBody = new ArrayList<>();
        List<Product> filteredConnectors = new ArrayList<>();
        List<Product> filteredPeripherals = new ArrayList<>();

        for (Product product : centralComponentsList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredCentralComponents.add(product);
            }
        }

        for (Product product : bodyList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredBody.add(product);
            }
        }

        for (Product product : connectorsList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredConnectors.add(product);
            }
        }

        for (Product product : peripheralsList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredPeripherals.add(product);
            }
        }

        adapterCentralComponents.updateProductList(filteredCentralComponents);
        adapterBody.updateProductList(filteredBody);
        adapterConnectors.updateProductList(filteredConnectors);
        adapterPeripherals.updateProductList(filteredPeripherals);
    }

    private void loadSellerProfile(Product product) {
        String sellerId = product.getSellerId();
        if (sellerId != null) {
            db.collection("sellers").document(sellerId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String profileImageUrl = document.getString("profileImageUrl");
                        product.setSellerProfileImageUrl(profileImageUrl);
                        notifyAdapters();
                    }
                }
            });
        }
    }

    private void notifyAdapters() {
        adapterCentralComponents.notifyDataSetChanged();
        adapterBody.notifyDataSetChanged();
        adapterConnectors.notifyDataSetChanged();
        adapterPeripherals.notifyDataSetChanged();
    }

    @Override
    public void onProductClick(int position, String category) {
        Product clickedProduct;

        if (category.equals("Central Components")) {
            clickedProduct = centralComponentsList.get(position);
        } else if (category.equals("Body")) {
            clickedProduct = bodyList.get(position);
        } else if (category.equals("Connectors")) {
            clickedProduct = connectorsList.get(position);
        } else { // Peripherals
            clickedProduct = peripheralsList.get(position);
        }

        Intent intent = new Intent(getContext(), ProductDetailsBuyer.class);
        intent.putExtra("PRODUCT", clickedProduct);
        startActivity(intent);
    }
}
