package com.adithya.campusmarketplace.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.adapters.ProductAdapter;
import com.adithya.campusmarketplace.databinding.ActivityProductDetailBinding;
import com.adithya.campusmarketplace.firebase.FirebaseManager;
import com.adithya.campusmarketplace.models.Product;
import com.adithya.campusmarketplace.utils.CategoryUtils;
import com.adithya.campusmarketplace.utils.Constants;
import com.adithya.campusmarketplace.utils.DateUtils;
import com.adithya.campusmarketplace.viewmodels.ProductViewModel;
import com.adithya.campusmarketplace.viewmodels.WishlistViewModel;

public class ProductDetailActivity extends AppCompatActivity implements
        ProductAdapter.OnProductClickListener,
        ProductAdapter.OnWishlistToggleListener {

    private ActivityProductDetailBinding binding;
    private Product currentProduct;
    private WishlistViewModel wishlistViewModel;
    private ProductViewModel productViewModel;
    private ProductAdapter similarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        wishlistViewModel = new ViewModelProvider(this).get(WishlistViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Retrieve product from Intent
        if (getIntent().hasExtra(Constants.EXTRA_PRODUCT)) {
            currentProduct = (Product) getIntent().getSerializableExtra(Constants.EXTRA_PRODUCT);
        }

        if (currentProduct == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateProductData();
        setupSimilarProducts();
        setupListeners();
        observeViewModels();

        wishlistViewModel.checkWishlistStatus(currentProduct.getProductId());
        productViewModel.fetchSimilarProducts(currentProduct.getCategory(), currentProduct.getProductId());
    }

    @SuppressLint("SetTextI18n")
    private void populateProductData() {
        binding.tvDetailTitle.setText(currentProduct.getTitle());

        if (currentProduct.getPrice() <= 0) {
            binding.tvDetailPrice.setText("FREE • DONATION");
            binding.tvDetailPrice.setTextColor(ContextCompat.getColor(this, R.color.success_green));
        } else {
            binding.tvDetailPrice.setText("₹ " + (currentProduct.getPrice() == (long) currentProduct.getPrice() ?
                    String.format(java.util.Locale.getDefault(), "%d", (long) currentProduct.getPrice()) :
                    String.format(java.util.Locale.getDefault(), "%.2f", currentProduct.getPrice())));
            binding.tvDetailPrice.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
        }

        binding.tvDetailCategory.setText(currentProduct.getCategory() != null ? currentProduct.getCategory() : "General");
        binding.tvDetailCondition.setText("Condition: " + (currentProduct.getCondition() != null ? currentProduct.getCondition() : "Good"));
        binding.tvDetailDate.setText(DateUtils.formatRelativeTime(currentProduct.getCreatedAt()));

        String seller = currentProduct.getSellerName() != null && !currentProduct.getSellerName().isEmpty() ?
                currentProduct.getSellerName() : "Campus Student";
        binding.tvDetailSellerName.setText(seller);

        String phone = currentProduct.getPhone() != null && !currentProduct.getPhone().isEmpty() ?
                currentProduct.getPhone() : "Not provided";
        binding.tvDetailPhone.setText(phone);

        binding.tvDetailDescription.setText(currentProduct.getDescription() != null && !currentProduct.getDescription().isEmpty() ?
                currentProduct.getDescription() : "No detailed description provided by the seller.");

        com.adithya.campusmarketplace.utils.ImageUtils.displayProductImage(binding.ivDetailProductImage, currentProduct);
    }

    private void setupSimilarProducts() {
        similarAdapter = new ProductAdapter(this, this);
        binding.rvSimilarProducts.setAdapter(similarAdapter);
    }

    private void setupListeners() {
        binding.btnDetailBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        // Call seller action
        View.OnClickListener callAction = v -> {
            if (currentProduct.getPhone() != null && !currentProduct.getPhone().isEmpty()) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + currentProduct.getPhone()));
                try {
                    startActivity(callIntent);
                } catch (Exception e) {
                    Toast.makeText(ProductDetailActivity.this, "Unable to launch dialer", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProductDetailActivity.this, "No phone number available for this seller", Toast.LENGTH_SHORT).show();
            }
        };

        binding.btnBottomCall.setOnClickListener(callAction);
        binding.btnQuickCall.setOnClickListener(callAction);

        // Wishlist toggles
        View.OnClickListener wishlistAction = v -> {
            String uid = FirebaseManager.getInstance().getCurrentUserId();
            if (uid != null) {
                wishlistViewModel.toggleWishlist(currentProduct);
            } else {
                Toast.makeText(ProductDetailActivity.this, "Please log in to save items", Toast.LENGTH_SHORT).show();
            }
        };

        binding.btnBottomWishlist.setOnClickListener(wishlistAction);
        binding.btnQuickWishlist.setOnClickListener(wishlistAction);
    }

    private void observeViewModels() {
        wishlistViewModel.getIsCurrentProductWishlisted().observe(this, isWishlisted -> {
            currentProduct.setWishlisted(isWishlisted);
            updateWishlistButtons(isWishlisted);
        });

        wishlistViewModel.getToastMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(ProductDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        productViewModel.getSimilarProducts().observe(this, list -> {
            if (list == null || list.isEmpty()) {
                binding.layoutSimilarProducts.setVisibility(View.GONE);
            } else {
                binding.layoutSimilarProducts.setVisibility(View.VISIBLE);
                similarAdapter.submitList(list);
            }
        });
    }

    private void updateWishlistButtons(boolean isWishlisted) {
        if (isWishlisted) {
            binding.btnBottomWishlist.setText(R.string.btn_remove_from_wishlist);
            binding.btnBottomWishlist.setIconResource(R.drawable.ic_favorite_filled);
            binding.btnBottomWishlist.setIconTint(ContextCompat.getColorStateList(this, R.color.heart_red));
            binding.btnBottomWishlist.setTextColor(ContextCompat.getColor(this, R.color.heart_red));
            binding.btnBottomWishlist.setStrokeColor(ContextCompat.getColorStateList(this, R.color.heart_red));

            binding.ivQuickHeart.setImageResource(R.drawable.ic_favorite_filled);
            binding.ivQuickHeart.setColorFilter(ContextCompat.getColor(this, R.color.heart_red));
        } else {
            binding.btnBottomWishlist.setText(R.string.btn_add_to_wishlist);
            binding.btnBottomWishlist.setIconResource(R.drawable.ic_favorite_border);
            binding.btnBottomWishlist.setIconTint(ContextCompat.getColorStateList(this, R.color.primary_blue));
            binding.btnBottomWishlist.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
            binding.btnBottomWishlist.setStrokeColor(ContextCompat.getColorStateList(this, R.color.primary_blue));

            binding.ivQuickHeart.setImageResource(R.drawable.ic_favorite_border);
            binding.ivQuickHeart.setColorFilter(ContextCompat.getColor(this, R.color.secondary_text));
        }
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(Constants.EXTRA_PRODUCT, product);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onWishlistToggle(Product product, int position) {
        wishlistViewModel.toggleWishlist(product);
        similarAdapter.notifyItemChanged(position);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
