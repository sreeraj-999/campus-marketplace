package com.adithya.campusmarketplace.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.activities.LoginActivity;
import com.adithya.campusmarketplace.activities.MainActivity;
import com.adithya.campusmarketplace.databinding.FragmentSellBinding;
import com.adithya.campusmarketplace.firebase.FirebaseManager;
import com.adithya.campusmarketplace.models.Product;
import com.adithya.campusmarketplace.utils.CategoryUtils;
import com.adithya.campusmarketplace.utils.Constants;
import com.adithya.campusmarketplace.utils.DialogUtils;
import com.adithya.campusmarketplace.utils.ImageUtils;
import com.adithya.campusmarketplace.utils.ValidationUtils;
import com.adithya.campusmarketplace.viewmodels.AuthViewModel;
import com.adithya.campusmarketplace.viewmodels.ProductViewModel;
import com.google.android.material.snackbar.Snackbar;

public class SellFragment extends Fragment {

    private FragmentSellBinding binding;
    private ProductViewModel productViewModel;
    private AuthViewModel authViewModel;

    private String currentSellerName = "College Student";
    private String selectedImageBase64 = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register modern photo/gallery picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && getContext() != null) {
                        try {
                            Bitmap bitmap = ImageUtils.decodeSampledBitmapFromUri(requireContext(), uri, 600);
                            if (bitmap != null) {
                                selectedImageBase64 = ImageUtils.bitmapToBase64(bitmap);
                                binding.ivSellPlaceholder.setImageBitmap(bitmap);
                                binding.ivSellPlaceholder.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                binding.ivSellPlaceholder.setPadding(0, 0, 0, 0);
                                binding.btnChooseImage.setText("Change Selected Photo");
                                Toast.makeText(requireContext(), "Photo inserted successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Could not decode image", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "Error picking photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSellBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupSpinners();
        setupListeners();
        observeViewModel();

        authViewModel.loadCurrentUserProfile();
    }

    private void setupSpinners() {
        // Category Spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Constants.CATEGORIES_POST
        );
        binding.spinnerCategory.setAdapter(categoryAdapter);

        // Condition Spinner
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Constants.CONDITIONS
        );
        binding.spinnerCondition.setAdapter(conditionAdapter);

        // When category changes and user hasn't selected a custom photo, update placeholder
        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (selectedImageBase64 == null) {
                    String selectedCategory = Constants.CATEGORIES_POST[position];
                    binding.ivSellPlaceholder.setImageResource(CategoryUtils.getCategoryPlaceholder(selectedCategory));
                    binding.ivSellPlaceholder.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupListeners() {
        // Tap button to pick image from gallery
        binding.btnChooseImage.setOnClickListener(v -> openImagePicker());

        // Tap the preview frame itself to pick image as well
        binding.ivSellPlaceholder.setOnClickListener(v -> openImagePicker());

        // Submit Button: "Post Product"
        binding.btnPostProduct.setOnClickListener(v -> validateAndPostProduct());
    }

    private void openImagePicker() {
        try {
            imagePickerLauncher.launch("image/*");
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Unable to open gallery: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void validateAndPostProduct() {
        String title = binding.etProductTitle.getText() != null ? binding.etProductTitle.getText().toString().trim() : "";
        String desc = binding.etProductDesc.getText() != null ? binding.etProductDesc.getText().toString().trim() : "";
        String priceStr = binding.etProductPrice.getText() != null ? binding.etProductPrice.getText().toString().trim() : "";
        String phone = binding.etContactPhone.getText() != null ? binding.etContactPhone.getText().toString().trim() : "";
        String category = binding.spinnerCategory.getSelectedItem() != null ? binding.spinnerCategory.getSelectedItem().toString() : Constants.CATEGORY_OTHERS;
        String condition = binding.spinnerCondition.getSelectedItem() != null ? binding.spinnerCondition.getSelectedItem().toString() : Constants.CONDITION_GOOD;

        binding.tilProductTitle.setError(null);
        binding.tilProductDesc.setError(null);
        binding.tilProductPrice.setError(null);
        binding.tilContactPhone.setError(null);

        // Title Validation
        if (TextUtils.isEmpty(title)) {
            binding.tilProductTitle.setError(getString(R.string.err_empty_fields));
            Toast.makeText(requireContext(), "Please enter product title", Toast.LENGTH_SHORT).show();
            binding.etProductTitle.requestFocus();
            return;
        }

        // Description Validation
        if (TextUtils.isEmpty(desc)) {
            binding.tilProductDesc.setError(getString(R.string.err_empty_fields));
            Toast.makeText(requireContext(), "Please enter product description", Toast.LENGTH_SHORT).show();
            binding.etProductDesc.requestFocus();
            return;
        }

        // Price Validation
        if (TextUtils.isEmpty(priceStr)) {
            binding.tilProductPrice.setError(getString(R.string.err_empty_fields));
            Toast.makeText(requireContext(), "Please enter price (or 0 for donation)", Toast.LENGTH_SHORT).show();
            binding.etProductPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                binding.tilProductPrice.setError("Price cannot be negative");
                Toast.makeText(requireContext(), "Price cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            binding.tilProductPrice.setError("Invalid price format");
            Toast.makeText(requireContext(), "Please enter a valid price number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Phone Validation (accepts 10-13 digits)
        if (!ValidationUtils.isValidPhone(phone)) {
            binding.tilContactPhone.setError(getString(R.string.err_invalid_phone));
            Toast.makeText(requireContext(), getString(R.string.err_invalid_phone), Toast.LENGTH_SHORT).show();
            binding.etContactPhone.requestFocus();
            return;
        }

        // Session check
        String currentUid = FirebaseManager.getInstance().getCurrentUserId();
        if (currentUid == null) {
            Toast.makeText(requireContext(), "Please log in to post items", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
            return;
        }

        // Show uploading progress
        binding.progressBarSell.setVisibility(View.VISIBLE);
        binding.btnPostProduct.setEnabled(false);

        Product newProduct = new Product(
                null,
                currentUid,
                title,
                desc,
                price,
                category,
                condition,
                currentSellerName,
                phone,
                System.currentTimeMillis(),
                selectedImageBase64
        );

        productViewModel.addProduct(newProduct);
    }

    private void observeViewModel() {
        authViewModel.getCurrentUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                if (user.getName() != null && !user.getName().isEmpty()) {
                    currentSellerName = user.getName();
                }
                if (user.getPhone() != null && !user.getPhone().isEmpty() &&
                        (binding.etContactPhone.getText() == null || binding.etContactPhone.getText().toString().isEmpty())) {
                    binding.etContactPhone.setText(user.getPhone());
                }
            }
        });

        productViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarSell.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnPostProduct.setEnabled(!isLoading);
        });

        productViewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                productViewModel.resetOperationSuccess();
                clearForm();
                DialogUtils.showSuccessDialog(
                        requireContext(),
                        "Product Posted Successfully!",
                        getString(R.string.product_posted_success),
                        () -> {
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).navigateToHomeTab();
                            } else if (getActivity() != null) {
                                getActivity().finish();
                            }
                        }
                );
            }
        });

        productViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                binding.progressBarSell.setVisibility(View.GONE);
                binding.btnPostProduct.setEnabled(true);
                Toast.makeText(requireContext(), "Upload failed: " + error, Toast.LENGTH_LONG).show();
                showSnackbar("Upload failed: " + error);
            }
        });
    }

    private void clearForm() {
        binding.etProductTitle.setText("");
        binding.etProductDesc.setText("");
        binding.etProductPrice.setText("");
        selectedImageBase64 = null;
        binding.btnChooseImage.setText(R.string.btn_choose_image);
        int defaultPos = binding.spinnerCategory.getSelectedItemPosition();
        if (defaultPos >= 0 && defaultPos < Constants.CATEGORIES_POST.length) {
            binding.ivSellPlaceholder.setImageResource(CategoryUtils.getCategoryPlaceholder(Constants.CATEGORIES_POST[defaultPos]));
        } else {
            binding.ivSellPlaceholder.setImageResource(R.drawable.placeholder_books);
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error_red))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && authViewModel != null) {
            authViewModel.loadCurrentUserProfile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
