package com.adithya.campusmarketplace.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.activities.ProductDetailActivity;
import com.adithya.campusmarketplace.adapters.CategoryAdapter;
import com.adithya.campusmarketplace.adapters.ProductAdapter;
import com.adithya.campusmarketplace.databinding.FragmentSearchBinding;
import com.adithya.campusmarketplace.firebase.FirebaseManager;
import com.adithya.campusmarketplace.models.Category;
import com.adithya.campusmarketplace.models.Product;
import com.adithya.campusmarketplace.utils.Constants;
import com.adithya.campusmarketplace.viewmodels.ProductViewModel;
import com.adithya.campusmarketplace.viewmodels.WishlistViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements
        CategoryAdapter.OnCategoryClickListener,
        ProductAdapter.OnProductClickListener,
        ProductAdapter.OnWishlistToggleListener {

    private FragmentSearchBinding binding;
    private ProductViewModel productViewModel;
    private WishlistViewModel wishlistViewModel;

    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;

    private String selectedCategory = Constants.CATEGORY_ALL;
    private int currentSortOption = Constants.SORT_LATEST;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        wishlistViewModel = new ViewModelProvider(requireActivity()).get(WishlistViewModel.class);

        setupCategoryChips();
        setupResultsRecyclerView();
        setupSearchInput();
        setupSortButtons();
        observeViewModel();

        // Fetch products so search always operates on latest catalog
        productViewModel.fetchProducts();
        triggerSearch();
    }

    private void setupCategoryChips() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(Constants.CATEGORY_ALL, 0, true));
        categories.add(new Category(Constants.CATEGORY_BOOKS, 0));
        categories.add(new Category(Constants.CATEGORY_ELECTRONICS, 0));
        categories.add(new Category(Constants.CATEGORY_HOSTEL, 0));
        categories.add(new Category(Constants.CATEGORY_FURNITURE, 0));
        categories.add(new Category(Constants.CATEGORY_SPORTS, 0));
        categories.add(new Category(Constants.CATEGORY_OTHERS, 0));

        categoryAdapter = new CategoryAdapter(categories, this);
        binding.rvSearchCategories.setAdapter(categoryAdapter);
    }

    private void setupResultsRecyclerView() {
        productAdapter = new ProductAdapter(this, this);
        binding.rvSearchResults.setAdapter(productAdapter);
    }

    private void setupSearchInput() {
        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                triggerSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Search action on soft keyboard (Done / Search key)
        binding.etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                triggerSearch();
                return true;
            }
            return false;
        });

        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearchQuery.setText("");
            hideKeyboard();
            triggerSearch();
        });
    }

    private void setupSortButtons() {
        binding.btnSortLatest.setOnClickListener(v -> {
            currentSortOption = Constants.SORT_LATEST;
            updateSortButtonStyles(binding.btnSortLatest, binding.btnSortLowToHigh, binding.btnSortHighToLow);
            triggerSearch();
        });

        binding.btnSortLowToHigh.setOnClickListener(v -> {
            currentSortOption = Constants.SORT_PRICE_LOW_HIGH;
            updateSortButtonStyles(binding.btnSortLowToHigh, binding.btnSortLatest, binding.btnSortHighToLow);
            triggerSearch();
        });

        binding.btnSortHighToLow.setOnClickListener(v -> {
            currentSortOption = Constants.SORT_PRICE_HIGH_LOW;
            updateSortButtonStyles(binding.btnSortHighToLow, binding.btnSortLatest, binding.btnSortLowToHigh);
            triggerSearch();
        });
    }

    private void updateSortButtonStyles(MaterialButton active, MaterialButton... inactives) {
        active.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_blue));
        active.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        for (MaterialButton inactive : inactives) {
            inactive.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.chip_background));
            inactive.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
        }
    }

    private void triggerSearch() {
        if (binding == null) return;
        String query = binding.etSearchQuery.getText() != null ?
                binding.etSearchQuery.getText().toString().trim() : "";
        productViewModel.searchAndSort(query, selectedCategory, currentSortOption);
    }

    private void observeViewModel() {
        productViewModel.getSearchResults().observe(getViewLifecycleOwner(), products -> {
            boolean isLoading = Boolean.TRUE.equals(productViewModel.getIsLoading().getValue());
            if (products == null || products.isEmpty()) {
                binding.rvSearchResults.setVisibility(View.GONE);
                binding.layoutEmptySearch.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            } else {
                binding.rvSearchResults.setVisibility(View.VISIBLE);
                binding.layoutEmptySearch.setVisibility(View.GONE);
                productAdapter.submitList(products);
            }
        });

        // Trigger search whenever full catalog updates from Firestore
        productViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            triggerSearch();
        });

        productViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarSearch.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
            if (!Boolean.TRUE.equals(isLoading)) {
                List<Product> currentResults = productViewModel.getSearchResults().getValue();
                if (currentResults == null || currentResults.isEmpty()) {
                    binding.rvSearchResults.setVisibility(View.GONE);
                    binding.layoutEmptySearch.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void hideKeyboard() {
        if (binding != null && getContext() != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && binding.etSearchQuery != null) {
                imm.hideSoftInputFromWindow(binding.etSearchQuery.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onCategoryClick(Category category) {
        selectedCategory = category.getName();
        triggerSearch();
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra(Constants.EXTRA_PRODUCT, product);
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onWishlistToggle(Product product, int position) {
        String currentUid = FirebaseManager.getInstance().getCurrentUserId();
        if (currentUid != null) {
            wishlistViewModel.toggleWishlist(product);
            productAdapter.notifyItemChanged(position);
        } else {
            Toast.makeText(requireContext(), "Please log in to save items", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (productViewModel != null) {
                productViewModel.fetchProducts();
            }
            triggerSearch();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (productViewModel != null) {
            productViewModel.fetchProducts();
        }
        triggerSearch();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
