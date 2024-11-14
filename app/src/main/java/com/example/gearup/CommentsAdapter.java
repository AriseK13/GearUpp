package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    // Constructor that accepts a List<Comment>
    public CommentsAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // Set the comment text
        if (comment.getCommentText() != null && !comment.getCommentText().isEmpty()) {
            holder.tvCommentText.setText(comment.getCommentText());
        } else {
            holder.tvCommentText.setText("No comment provided");
        }

        // Set the full name or default to "Anonymous"
        if (comment.getFullName() != null && !comment.getFullName().isEmpty()) {
            holder.tvUsername.setText(comment.getFullName());
        } else {
            holder.tvUsername.setText("Anonymous");
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentText, tvUsername;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentText = itemView.findViewById(R.id.tv_comment_text);
            tvUsername = itemView.findViewById(R.id.tv_username);
        }
    }

    // Optional: If you need to update the comments dynamically
    public void updateComments(List<Comment> newComments) {
        commentList.clear();
        commentList.addAll(newComments);
        notifyDataSetChanged();
    }
}

