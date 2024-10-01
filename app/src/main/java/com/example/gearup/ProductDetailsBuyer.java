package com.example.gearup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

public class ProductDetailsBuyer extends AppCompatActivity {
    private TextView productName, productPrice, productDescription;
    private Button addToCartButton, checkoutButton;
    private EditText productQuantity; // EditText for quantity
    private ViewPager2 viewPager; // ViewPager2 for image slider

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details_buyer);

        productName = findViewById(R.id.tv_product_name);
        productPrice = findViewById(R.id.tv_product_price);
        productDescription = findViewById(R.id.tv_product_description);
        addToCartButton = findViewById(R.id.btn_add_to_cart);
        checkoutButton = findViewById(R.id.btn_checkout);
        productQuantity = findViewById(R.id.et_product_quantity); // Initialize the EditText
        viewPager = findViewById(R.id.viewPager); // Initialize ViewPager2

        // Get product from intent
        Product product = getIntent().getParcelableExtra("PRODUCT");

        if (product != null) {
            productName.setText(product.getName());
            productPrice.setText(String.format("₱%.2f", product.getPrice()));
            productDescription.setText(product.getDescription());

            // Load images into ViewPager2
            ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(product.getImageUrls());
            viewPager.setAdapter(imageSliderAdapter);
        }

        // Set up button click listeners
        addToCartButton.setOnClickListener(v -> addToCart(product));
        checkoutButton.setOnClickListener(v -> checkout());
    }

    private void addToCart(Product product) {
        if (product != null) {
            String quantityText = productQuantity.getText().toString();
            int quantity = quantityText.isEmpty() ? 1 : Integer.parseInt(quantityText); // Default to 1 if empty

            // Check if the product is already in the cart
            boolean alreadyInCart = false;
            for (CartItem item : Cart.getInstance().getItems()) {
                if (item.getProduct().getId().equals(product.getId())) {
                    item.setQuantity(item.getQuantity() + quantity); // Increase the quantity
                    alreadyInCart = true;
                    break;
                }
            }

            if (!alreadyInCart) {
                Cart.getInstance().addToCart(product, quantity); // Add as new item
            }

            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();

            // Navigate to CartActivity
            Intent intent = new Intent(ProductDetailsBuyer.this, CartActivity.class);
            startActivity(intent);
        }
    }

    private void checkout() {
        // Navigate to checkout logic
        Toast.makeText(this, "Checkout not implemented", Toast.LENGTH_SHORT).show();
    }
}
