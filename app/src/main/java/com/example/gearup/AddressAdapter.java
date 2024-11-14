package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Map<String, Object>> addressList;
    private OnAddressClickListener listener;

    public interface OnAddressClickListener {
        void onDefaultButtonClick(int position);
        void onDeleteButtonClick(int position);
    }

    public AddressAdapter(List<Map<String, Object>> addressList, OnAddressClickListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Map<String, Object> address = addressList.get(position);

        // Bind address details
        holder.fullNameTextView.setText((String) address.get("fullName"));
        holder.mobileNumberTextView.setText((String) address.get("phoneNumber"));
        holder.addressTextView.setText((String) address.get("regionProvinceCity") + ", " + (String) address.get("streetDetails"));

        // Handle default button click
        holder.defaultButton.setOnClickListener(v -> listener.onDefaultButtonClick(position));

        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteButtonClick(position));
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView fullNameTextView, mobileNumberTextView, addressTextView;
        Button defaultButton, deleteButton;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.tv_full_name);
            mobileNumberTextView = itemView.findViewById(R.id.tv_mobile_number);
            addressTextView = itemView.findViewById(R.id.tv_address);
            defaultButton = itemView.findViewById(R.id.btn_default);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}
