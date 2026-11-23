package com.adithya.campusmarketplace.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.databinding.ItemMyListingCardBinding;
import com.adithya.campusmarketplace.models.Product;
import com.adithya.campusmarketplace.utils.CategoryUtils;
import com.adithya.campusmarketplace.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class MyListingsAdapter extends RecyclerView.Adapter<MyListingsAdapter.MyListingViewHolder> {

    private final List<Product> listings = new ArrayList<>();
    private final OnListingActionListener actionListener;

    public interface OnListingActionListener {
        void onListingClick(Product product);
        void onEditListing(Product product);
        void onDeleteListing(Product product);
    }

    public MyListingsAdapter(OnListingActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<Product> products) {
        listings.clear();
        if (products != null) {
            listings.addAll(products);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyListingCardBinding binding = ItemMyListingCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MyListingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyListingViewHolder holder, int position) {
        holder.bind(listings.get(position));
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    class MyListingViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyListingCardBinding binding;

        MyListingViewHolder(ItemMyListingCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        void bind(Product product) {
            Context context = binding.getRoot().getContext();

            binding.tvListingTitle.setText(product.getTitle());

            if (product.getPrice() <= 0) {
                binding.tvListingPrice.setText("FREE • DONATION");
                binding.tvListingPrice.setTextColor(ContextCompat.getColor(context, R.color.success_green));
            } else {
                binding.tvListingPrice.setText("₹ " + (product.getPrice() == (long) product.getPrice() ? String.format(java.util.Locale.getDefault(), "%d", (long) product.getPrice()) : String.format(java.util.Locale.getDefault(), "%.2f", product.getPrice())));
                binding.tvListingPrice.setTextColor(ContextCompat.getColor(context, R.color.primary_blue));
            }

            String category = product.getCategory() != null ? product.getCategory() : "Item";
            String condition = product.getCondition() != null ? product.getCondition() : "Good";
            String date = DateUtils.formatRelativeTime(product.getCreatedAt());
            binding.tvListingMeta.setText(category + " • " + condition + " • " + date);

            com.adithya.campusmarketplace.utils.ImageUtils.displayProductImage(binding.ivListingImage, product);

            binding.cardMyListing.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onListingClick(product);
                }
            });

            binding.btnEditListing.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onEditListing(product);
                }
            });

            binding.btnDeleteListing.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onDeleteListing(product);
                }
            });
        }
    }
}
