package com.adithya.campusmarketplace.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.databinding.ItemWishlistCardBinding;
import com.adithya.campusmarketplace.models.Product;
import com.adithya.campusmarketplace.utils.CategoryUtils;

import java.util.ArrayList;
import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private final List<Product> wishlistProducts = new ArrayList<>();
    private final OnWishlistItemClickListener clickListener;
    private final OnWishlistRemoveListener removeListener;

    public interface OnWishlistItemClickListener {
        void onProductClick(Product product);
    }

    public interface OnWishlistRemoveListener {
        void onRemoveClick(Product product, int position);
    }

    public WishlistAdapter(OnWishlistItemClickListener clickListener, OnWishlistRemoveListener removeListener) {
        this.clickListener = clickListener;
        this.removeListener = removeListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<Product> products) {
        wishlistProducts.clear();
        if (products != null) {
            wishlistProducts.addAll(products);
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < wishlistProducts.size()) {
            wishlistProducts.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWishlistCardBinding binding = ItemWishlistCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new WishlistViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        holder.bind(wishlistProducts.get(position));
    }

    @Override
    public int getItemCount() {
        return wishlistProducts.size();
    }

    class WishlistViewHolder extends RecyclerView.ViewHolder {
        private final ItemWishlistCardBinding binding;

        WishlistViewHolder(ItemWishlistCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        void bind(Product product) {
            Context context = binding.getRoot().getContext();

            binding.tvWishlistTitle.setText(product.getTitle());

            if (product.getPrice() <= 0) {
                binding.tvWishlistPrice.setText("FREE • DONATION");
                binding.tvWishlistPrice.setTextColor(ContextCompat.getColor(context, R.color.success_green));
            } else {
                binding.tvWishlistPrice.setText("₹ " + (product.getPrice() == (long) product.getPrice() ? String.format(java.util.Locale.getDefault(), "%d", (long) product.getPrice()) : String.format(java.util.Locale.getDefault(), "%.2f", product.getPrice())));
                binding.tvWishlistPrice.setTextColor(ContextCompat.getColor(context, R.color.primary_blue));
            }

            String category = product.getCategory() != null ? product.getCategory() : "General";
            String condition = product.getCondition() != null ? product.getCondition() : "Used";
            binding.tvWishlistCategory.setText(category + " • " + condition);

            String seller = product.getSellerName() != null && !product.getSellerName().isEmpty() ?
                    product.getSellerName() : "Campus Student";
            binding.tvWishlistSeller.setText("Seller: " + seller);

            com.adithya.campusmarketplace.utils.ImageUtils.displayProductImage(binding.ivWishlistImage, product);

            binding.cardWishlist.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onProductClick(product);
                }
            });

            binding.btnRemoveWishlist.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && removeListener != null) {
                    removeListener.onRemoveClick(product, pos);
                }
            });
        }
    }
}
