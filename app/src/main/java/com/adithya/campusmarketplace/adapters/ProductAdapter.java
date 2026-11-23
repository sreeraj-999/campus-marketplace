package com.adithya.campusmarketplace.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.databinding.ItemProductCardBinding;
import com.adithya.campusmarketplace.models.Product;
import com.adithya.campusmarketplace.utils.CategoryUtils;
import com.adithya.campusmarketplace.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> products = new ArrayList<>();
    private final OnProductClickListener clickListener;
    private final OnWishlistToggleListener wishlistListener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnWishlistToggleListener {
        void onWishlistToggle(Product product, int position);
    }

    public ProductAdapter(OnProductClickListener clickListener, OnWishlistToggleListener wishlistListener) {
        this.clickListener = clickListener;
        this.wishlistListener = wishlistListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<Product> newProducts) {
        products.clear();
        if (newProducts != null) {
            products.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductCardBinding binding = ItemProductCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductCardBinding binding;

        ProductViewHolder(ItemProductCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        void bind(Product product) {
            Context context = binding.getRoot().getContext();

            // Set Title
            binding.tvTitle.setText(product.getTitle());

            // Price: bold blue, format e.g. "Free (Donate)" if 0, else "₹ Price"
            if (product.getPrice() <= 0) {
                binding.tvPrice.setText("FREE • DONATION");
                binding.tvPrice.setTextColor(ContextCompat.getColor(context, R.color.success_green));
            } else {
                binding.tvPrice.setText("₹ " + (product.getPrice() == (long) product.getPrice() ? String.format(java.util.Locale.getDefault(), "%d", (long) product.getPrice()) : String.format(java.util.Locale.getDefault(), "%.2f", product.getPrice())));
                binding.tvPrice.setTextColor(ContextCompat.getColor(context, R.color.primary_blue));
            }

            // Category & Condition
            binding.tvCategoryBadge.setText(product.getCategory() != null ? product.getCategory() : "Item");
            binding.tvCondition.setText(product.getCondition() != null ? product.getCondition() : "Good");

            // Seller Name
            String seller = product.getSellerName() != null && !product.getSellerName().isEmpty() ?
                    product.getSellerName() : "Campus Student";
            binding.tvSellerName.setText("By " + seller);

            // Date
            binding.tvPostedDate.setText(DateUtils.formatRelativeTime(product.getCreatedAt()));

            // Display product image (Custom uploaded image or category placeholder)
            com.adithya.campusmarketplace.utils.ImageUtils.displayProductImage(binding.ivProductImage, product);

            // Wishlist Heart state
            updateHeartIcon(product.isWishlisted());

            // Card click listener
            binding.cardProduct.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onProductClick(product);
                }
            });

            // Wishlist button click listener
            binding.btnWishlistHeart.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && wishlistListener != null) {
                    wishlistListener.onWishlistToggle(product, pos);
                }
            });
        }

        private void updateHeartIcon(boolean isWishlisted) {
            Context context = binding.getRoot().getContext();
            if (isWishlisted) {
                binding.ivHeart.setImageResource(R.drawable.ic_favorite_filled);
                binding.ivHeart.setColorFilter(ContextCompat.getColor(context, R.color.heart_red));
            } else {
                binding.ivHeart.setImageResource(R.drawable.ic_favorite_border);
                binding.ivHeart.setColorFilter(ContextCompat.getColor(context, R.color.secondary_text));
            }
        }
    }
}
