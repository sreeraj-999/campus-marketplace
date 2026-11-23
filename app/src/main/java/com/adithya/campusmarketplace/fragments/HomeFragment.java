package com.adithya.campusmarketplace.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.activities.MainActivity;
import com.adithya.campusmarketplace.activities.ProductDetailActivity;
import com.adithya.campusmarketplace.activities.SellProductActivity;
import com.adithya.campusmarketplace.adapters.CategoryAdapter;
import com.adithya.campusmarketplace.adapters.ProductAdapter;
import com.adithya.campusmarketplace.databinding.FragmentHomeBinding;
import com.adithya.campusmarketplace.firebase.FirebaseManager;
import com.adithya.campusmarketplace.models.Category;
import com.adithya.campusmarketplace.models.Product;
import com.adithya.campusmarketplace.utils.Constants;
import com.adithya.campusmarketplace.utils.DialogUtils;
import com.adithya.campusmarketplace.viewmodels.AuthViewModel;
import com.adithya.campusmarketplace.viewmodels.ProductViewModel;
import com.adithya.campusmarketplace.viewmodels.WishlistViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements
        CategoryAdapter.OnCategoryClickListener,
        ProductAdapter.OnProductClickListener,
        ProductAdapter.OnWishlistToggleListener {

    private FragmentHomeBinding binding;
    private ProductViewModel productViewModel;
    private WishlistViewModel wishlistViewModel;
    private AuthViewModel authViewModel;

    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        wishlistViewModel = new ViewModelProvider(requireActivity()).get(WishlistViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupCategoriesRecyclerView();
        setupProductsRecyclerView();
        setupListeners();
        observeViewModels();

        authViewModel.loadCurrentUserProfile();
        productViewModel.fetchProducts();
    }

    private void setupCategoriesRecyclerView() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(Constants.CATEGORY_ALL, 0, true));
        categories.add(new Category(Constants.CATEGORY_BOOKS, 0));
        categories.add(new Category(Constants.CATEGORY_ELECTRONICS, 0));
        categories.add(new Category(Constants.CATEGORY_HOSTEL, 0));
        categories.add(new Category(Constants.CATEGORY_FURNITURE, 0));
        categories.add(new Category(Constants.CATEGORY_SPORTS, 0));
        categories.add(new Category(Constants.CATEGORY_OTHERS, 0));

        categoryAdapter = new CategoryAdapter(categories, this);
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void setupProductsRecyclerView() {
        productAdapter = new ProductAdapter(this, this);
        binding.rvProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        // Notification dialog
        binding.btnNotification.setOnClickListener(v -> DialogUtils.showSuccessDialog(
                requireContext(),
                getString(R.string.campus_notice_title),
                getString(R.string.campus_notice_msg),
                null
        ));

        // Tapping search bar switches to SearchFragment tab
        binding.homeSearchBarContainer.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToSearchTab();
            }
        });

        // Floating Action Button opens Sell Product
        binding.fabSellProduct.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SellProductActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Post First Product button from empty state
        binding.btnPostFirstProduct.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SellProductActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void observeViewModels() {
        authViewModel.getCurrentUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getName() != null && !user.getName().isEmpty()) {
                binding.tvGreeting.setText(getString(R.string.greeting_default).replace("Student", user.getName()));
            }
        });

        productViewModel.getDisplayedProducts().observe(getViewLifecycleOwner(), products -> {
            if (products == null || products.isEmpty()) {
                binding.rvProducts.setVisibility(View.GONE);
                binding.layoutEmptyHome.setVisibility(View.VISIBLE);
                binding.tvProductCount.setText("0 items");
            } else {
                binding.rvProducts.setVisibility(View.VISIBLE);
                binding.layoutEmptyHome.setVisibility(View.GONE);
                binding.tvProductCount.setText(products.size() + (products.size() == 1 ? " item" : " items"));
                productAdapter.submitList(products);
            }
        });

        productViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarHome.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        wishlistViewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCategoryClick(Category category) {
        productViewModel.filterByCategory(category.getName());
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
            if (authViewModel != null) {
                authViewModel.loadCurrentUserProfile();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (productViewModel != null) {
            productViewModel.fetchProducts();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
