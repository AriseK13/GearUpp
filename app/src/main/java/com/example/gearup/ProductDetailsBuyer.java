package com.example.gearup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.gearup.Comment;
import com.example.gearup.CommentsAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailsBuyer extends AppCompatActivity {
    private TextView productName, productPrice, productDescription, availableQuantityText, sellerName;
    private Button addToCartButton, checkoutButton;
    private EditText productQuantity;
    private ViewPager2 viewPager;
    private ImageView sellerProfileImage;
    private int maxQuantity;

    // Comment UI
    private EditText commentEditText;
    private Button submitCommentButton;
    private RecyclerView commentsRecyclerView;
    private List<Comment> commentList;
    private CommentsAdapter commentsAdapter;
    private FirebaseAuth mAuth;


    // Stripe variables
    private String PublishableKey = "pk_test_51PF3ByC6MmcIFikTjKhzCftwVaWmffD2iAqfquBroHxyujRLOG6QJ07t0tljO8FzDYbsNZld6sSjbTSTFUfT8J1c00D2b0tfvg";
    private String SecretKey = "sk_test_51PF3ByC6MmcIFikTxmE9dhgo5ZLxCWlNgqBaBMwZUKCCeRd0pkgKBQZOBO9UymYma2sNPpNIKlU2befDh0JeISU700OoXXptWX";
    private String CustomerId;
    private String EphericalKey;
    private String ClientSecret;
    private PaymentSheet paymentSheet;

    private FirebaseFirestore db;
    private Product product; // Make product an instance variable

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details_buyer);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        productName = findViewById(R.id.tv_product_name);
        productPrice = findViewById(R.id.tv_product_price);
        productDescription = findViewById(R.id.tv_product_description);
        availableQuantityText = findViewById(R.id.tv_available_quantity);
        sellerName = findViewById(R.id.tv_seller_name);
        sellerProfileImage = findViewById(R.id.iv_seller_profile);
        addToCartButton = findViewById(R.id.btn_add_to_cart);
        checkoutButton = findViewById(R.id.btn_checkout);
        productQuantity = findViewById(R.id.et_product_quantity);
        viewPager = findViewById(R.id.viewPager);

        db = FirebaseFirestore.getInstance();

        // Initialize Stripe
        PaymentConfiguration.init(this, PublishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        // Get product from intent
        product = getIntent().getParcelableExtra("PRODUCT");

        if (product != null) {
            Log.d("ProductDetails", "Product loaded successfully");

            productName.setText(product.getName());
            productPrice.setText(String.format("₱%.2f", product.getPrice()));
            productDescription.setText(product.getDescription());
            maxQuantity = product.getQuantity();

            // Display available quantity
            availableQuantityText.setText("Available Quantity: " + maxQuantity);

            // Load images into ViewPager2
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(product.getImageUrls());
                viewPager.setAdapter(imageSliderAdapter);
            } else {
                Log.e("ProductDetails", "Image URLs are null or empty");
            }

            // Retrieve seller info from Firestore
            getSellerInfo(product.getSellerId());
        } else {
            Log.e("ProductDetails", "Product is null");
            Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no product is passed
            return;
        }

        // Set up button click listeners
        addToCartButton.setOnClickListener(v -> addToCart(product));
        checkoutButton.setOnClickListener(v -> paymentFlow());

        // Initialize comment section
        commentEditText = findViewById(R.id.et_comment);
        submitCommentButton = findViewById(R.id.btn_submit_comment);
        commentsRecyclerView = findViewById(R.id.rv_comments);
        commentList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        commentList.clear();
        commentsAdapter.notifyDataSetChanged();

        // Load comments from Firebase Firestore
        loadCommentsFromDatabase();

        // Handle comment submission
        submitCommentButton.setOnClickListener(v -> {
            String commentText = commentEditText.getText().toString().trim();

            if (!commentText.isEmpty()) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Fetch user's full name from Firestore
                db.collection("buyers").document(userId).get()
                        .addOnCompleteListener(task -> {
                            String fullName = "Anonymous";  // Default value
                            if (task.isSuccessful() && task.getResult().exists()) {
                                fullName = task.getResult().getString("fullName");
                            }

                            // Create a new comment object including the product ID
                            Comment newComment = new Comment(commentText, fullName, product.getId());
                            commentList.add(newComment);
                            commentsAdapter.notifyItemInserted(commentList.size() - 1);  // Update UI
                            saveCommentToDatabase(newComment);  // Save in Firestore
                            commentEditText.setText("");  // Clear input field
                        });
            } else {
                Toast.makeText(ProductDetailsBuyer.this, "Please write a comment!", Toast.LENGTH_SHORT).show();
            }
        });
    }

        private void getCurrentFullName(OnFullNameFetchedListener listener) {
        String userId = mAuth.getCurrentUser().getUid();

        // Fetch user's full name from Firestore
        db.collection("buyers").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String fullName = task.getResult().getString("fullName");

                        if (listener != null) {
                            listener.onFullNameFetched(fullName);
                        }
                    } else {
                        if (listener != null) {
                            listener.onFullNameFetched(null); // Handle case where user details are not found
                        }
                        Toast.makeText(ProductDetailsBuyer.this, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Interface for callback
    interface OnFullNameFetchedListener {
        void onFullNameFetched(String fullName);
    }


    // Callback interface to handle async Firestore retrieval
    private interface FirestoreCallback {
        void onCallback(String fullName);
    }


    private String getCurrentUsername() {
        // Assuming you're using Firebase Auth to get the current user's information
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Replace with how you store the username, e.g., in Firestore
            return user.getDisplayName(); // or retrieve from Firestore if you save it there
        } else {
            return "Guest"; // or handle the case when the user is not logged in
        }
    }


    private void getSellerInfo(String sellerId) {
        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String sellerNameStr = documentSnapshot.getString("shopName");
                        String sellerProfileImageUrl = documentSnapshot.getString("profile_image_url");
                        if (sellerNameStr != null) {
                            sellerName.setText(sellerNameStr);
                        }
                        Glide.with(ProductDetailsBuyer.this)
                                .load(sellerProfileImageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(sellerProfileImage);
                    } else {
                        Toast.makeText(ProductDetailsBuyer.this, "Seller not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProductDetailsBuyer.this, "Error getting seller info", Toast.LENGTH_SHORT).show());
    }

    private void addToCart(Product product) {
        if (product != null) {
            String quantityText = productQuantity.getText().toString();
            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                quantity = 1; // Default quantity
            }

            if (quantity < 1 || quantity > maxQuantity) {
                Toast.makeText(this, "Please enter a valid quantity between 1 and " + maxQuantity, Toast.LENGTH_SHORT).show();
                return;
            }

            boolean alreadyInCart = false;
            for (CartItem item : Cart.getInstance().getItems()) {
                if (item.getProduct().getId().equals(product.getId())) {
                    item.setQuantity(item.getQuantity() + quantity);
                    alreadyInCart = true;
                    break;
                }
            }

            if (!alreadyInCart) {
                Cart.getInstance().addToCart(product, quantity);
            }

            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProductDetailsBuyer.this, CartActivity.class);
            startActivity(intent);
        }
    }

    private void paymentFlow() {
        // Create customer and start payment flow
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/customers",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            CustomerId = object.getString("id");
                            getEmphericalKey();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProductDetailsBuyer.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                return header;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getEmphericalKey() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/ephemeral_keys",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            EphericalKey = object.getString("id");
                            getClientSecret(CustomerId, EphericalKey);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProductDetailsBuyer.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                header.put("Stripe-Version", "2024-09-30.acacia");
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getClientSecret(String customerId, String ephericalKey) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            ClientSecret = object.getString("client_secret");
                            paymentFlowPresent();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProductDetailsBuyer.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                params.put("amount", "10000"); // Example amount
                params.put("currency", "PHP");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void paymentFlowPresent() {
        paymentSheet.presentWithPaymentIntent(ClientSecret, new PaymentSheet.Configuration("Learn with Arvind", new PaymentSheet.CustomerConfiguration(CustomerId, EphericalKey)));
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCommentsFromDatabase() {
        // Load comments from Firestore filtered by the current product's ID
        db.collection("comments")
                .whereEqualTo("productId", product.getId()) // Assuming comments are stored with the associated product ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentList.clear(); // Clear existing comments to avoid duplication
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Comment comment = snapshot.toObject(Comment.class);
                        commentList.add(comment);
                    }
                    commentsAdapter.notifyDataSetChanged(); // Notify the adapter to refresh the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProductDetailsBuyer.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                });
    }



    private void saveCommentToDatabase(Comment comment) {
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("commentText", comment.getCommentText());
        commentData.put("fullName", comment.getFullName()); // Storing full name
        commentData.put("productId", comment.getProductId()); // Save the product ID

        db.collection("comments").add(commentData)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProductDetailsBuyer.this, "Comment added!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error adding comment", e);
                    Toast.makeText(ProductDetailsBuyer.this, "Error adding comment", Toast.LENGTH_SHORT).show();
                });
    }
}
