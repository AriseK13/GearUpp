package com.example.gearup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryFragment extends Fragment {

    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_CODE_PRODUCT_DETAILS = 2; // Changed to avoid conflict

    private Map<String, List<Product>> categorizedProducts = new HashMap<>();
    private Map<String, Integer> categoryImages = new HashMap<>();
    private Uri selectedImageUri;
    private AlertDialog alertDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false); // New layout for fragment

        recyclerViewCategories = view.findViewById(R.id.recyclerView_categories);
        recyclerViewCategories.setLayoutManager(new GridLayoutManager(getContext(), 2));

        initializeCategories();

        categoryAdapter = new CategoryAdapter(getContext(),
                new ArrayList<>(categorizedProducts.keySet()),
                categorizedProducts,
                categoryImages,
                this::showProductsForCategory);
        recyclerViewCategories.setAdapter(categoryAdapter);

        Button addProductButton = view.findViewById(R.id.btn_add_product);
        addProductButton.setOnClickListener(v -> showAddProductDialog());

        return view;
    }

    private void initializeCategories() {
        categorizedProducts.put("Central Components", new ArrayList<>());
        categorizedProducts.put("Peripherals", new ArrayList<>());
        categorizedProducts.put("Connectors", new ArrayList<>());
        categorizedProducts.put("Body", new ArrayList<>());

        categoryImages.put("Central Components", R.drawable.automotive);
        categoryImages.put("Peripherals", R.drawable.automotive);
        categoryImages.put("Connectors", R.drawable.automotive);
        categoryImages.put("Body", R.drawable.automotive);
    }

    private void showProductsForCategory(String category) {
        List<Product> products = categorizedProducts.get(category);

        Intent intent = new Intent(getContext(), ProductList.class);
        intent.putExtra("CATEGORY", category);
        intent.putParcelableArrayListExtra("PRODUCTS", new ArrayList<>(products));
        startActivityForResult(intent, REQUEST_CODE_PRODUCT_DETAILS);
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        builder.setView(dialogView);

        EditText productName = dialogView.findViewById(R.id.et_product_name);
        EditText productPrice = dialogView.findViewById(R.id.et_product_price);
        EditText productDescription = dialogView.findViewById(R.id.et_product_description);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_category);
        Button chooseImageButton = dialogView.findViewById(R.id.btn_choose_image);
        ImageView productImage = dialogView.findViewById(R.id.iv_product_image);
        Button addProductButton = dialogView.findViewById(R.id.btn_add_product);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>(categorizedProducts.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        chooseImageButton.setOnClickListener(v -> openFileChooser());

        addProductButton.setOnClickListener(v -> {
            String name = productName.getText().toString();
            String priceString = productPrice.getText().toString();
            String description = productDescription.getText().toString();
            String category = categorySpinner.getSelectedItem().toString();

            if (name.isEmpty() || priceString.isEmpty() || selectedImageUri == null) {
                Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceString);

            Product product = new Product(name, price, description, selectedImageUri.toString(), category);
            categorizedProducts.get(category).add(product);

            Toast.makeText(getContext(), "Product added", Toast.LENGTH_SHORT).show();
            categoryAdapter.notifyDataSetChanged();
            alertDialog.dismiss();
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ImageView productImage = alertDialog.findViewById(R.id.iv_product_image);
            if (productImage != null) {
                productImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(selectedImageUri).into(productImage);
            }
        } else if (requestCode == REQUEST_CODE_PRODUCT_DETAILS && resultCode == Activity.RESULT_OK && data != null) {
            String category = data.getStringExtra("CATEGORY");
            int countChange = data.getIntExtra("PRODUCT_COUNT_CHANGE", 0);
            List<Product> products = categorizedProducts.get(category);
            if (products != null) {
                int newSize = products.size() - countChange;
                // Update category count based on the newSize if needed
                Toast.makeText(getContext(), "Category updated", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
