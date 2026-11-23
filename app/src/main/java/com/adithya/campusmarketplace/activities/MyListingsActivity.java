package com.adithya.campusmarketplace.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.adapters.MyListingsAdapter;
import com.adithya.campusmarketplace.databinding.ActivityMyListingsBinding;
import com.adithya.campusmarketplace.databinding.DialogEditProductBinding;
import com.adithya.campusmarketplace.firebase.FirebaseManager;
import com.adithya.campusmarketplace.models.Product;
import com.adithya.campusmarketplace.utils.Constants;
import com.adithya.campusmarketplace.utils.DialogUtils;
import com.adithya.campusmarketplace.viewmodels.ProductViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MyListingsActivity extends AppCompatActivity implements
        MyListingsAdapter.OnListingActionListener {

    private ActivityMyListingsBinding binding;
    private ProductViewModel productViewModel;
    private MyListingsAdapter adapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyListingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = FirebaseManager.getInstance().getCurrentUserId();
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        binding.toolbarMyListings.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        setupRecyclerView();
        observeViewModel();

        if (currentUserId != null) {
            productViewModel.fetchMyListings(currentUserId);
        }
    }

    private void setupRecyclerView() {
        adapter = new MyListingsAdapter(this);
        binding.rvMyListings.setAdapter(adapter);
    }

    private void observeViewModel() {
        productViewModel.getMyListings().observe(this, products -> {
            if (products == null || products.isEmpty()) {
                binding.rvMyListings.setVisibility(View.GONE);
                binding.layoutEmptyMyListings.setVisibility(View.VISIBLE);
            } else {
                binding.rvMyListings.setVisibility(View.VISIBLE);
                binding.layoutEmptyMyListings.setVisibility(View.GONE);
                adapter.submitList(products);
            }
        });

        productViewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBarMyListings.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        productViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(MyListingsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onListingClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(Constants.EXTRA_PRODUCT, product);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onEditListing(Product product) {
        DialogEditProductBinding dialogBinding = DialogEditProductBinding.inflate(getLayoutInflater());
        dialogBinding.etEditTitle.setText(product.getTitle());
        dialogBinding.etEditPrice.setText(String.valueOf(product.getPrice()));
        dialogBinding.etEditDesc.setText(product.getDescription());

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
                String newTitle = dialogBinding.etEditTitle.getText() != null ?
                        dialogBinding.etEditTitle.getText().toString().trim() : "";
                String newPriceStr = dialogBinding.etEditPrice.getText() != null ?
                        dialogBinding.etEditPrice.getText().toString().trim() : "";
                String newDesc = dialogBinding.etEditDesc.getText() != null ?
                        dialogBinding.etEditDesc.getText().toString().trim() : "";

                if (TextUtils.isEmpty(newTitle)) {
                    dialogBinding.tilEditTitle.setError(getString(R.string.err_empty_fields));
                    return;
                }

                if (TextUtils.isEmpty(newPriceStr)) {
                    dialogBinding.tilEditPrice.setError(getString(R.string.err_empty_fields));
                    return;
                }

                double price;
                try {
                    price = Double.parseDouble(newPriceStr);
                } catch (NumberFormatException e) {
                    dialogBinding.tilEditPrice.setError("Invalid price");
                    return;
                }

                product.setTitle(newTitle);
                product.setPrice(price);
                product.setDescription(newDesc);

                productViewModel.updateProduct(product);
                Toast.makeText(MyListingsActivity.this, R.string.listing_updated, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    @Override
    public void onDeleteListing(Product product) {
        DialogUtils.showConfirmationDialog(
                this,
                getString(R.string.delete),
                getString(R.string.delete_listing_confirm),
                getString(R.string.delete),
                () -> {
                    productViewModel.deleteProduct(product.getProductId(), currentUserId);
                    Toast.makeText(MyListingsActivity.this, R.string.listing_deleted, Toast.LENGTH_SHORT).show();
                }
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
