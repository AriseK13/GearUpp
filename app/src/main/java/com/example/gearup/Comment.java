package com.example.gearup;

public class Comment {
    private String commentText;
    private String fullName;
    private String productId;  // New field for product ID

    // No-argument constructor required for Firebase deserialization
    public Comment() {
        // Required empty constructor
    }

    // Parameterized constructor
    public Comment(String commentText, String fullName, String productId) {
        this.commentText = commentText;
        this.fullName = fullName;
        this.productId = productId;
    }

    // Getters and setters
    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}








