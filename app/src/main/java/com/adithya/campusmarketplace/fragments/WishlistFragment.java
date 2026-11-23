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
import com.adithya.campusmarketplace.activities.ProductDetailActivity;
import com.adithya.campusmarketplace.adapters.WishlistAdapter;
import com.adithya.campusmarketplace.databinding.FragmentWishlistBinding;
import com.adithya.campusmarketplace.models.Product;
import com.adithya.campusmarketplace.utils.Constants;
import com.adithya.campusmarketplace.viewmodels.WishlistViewModel;

public class WishlistFragment extends Fragment implements
        WishlistAdapter.OnWishlistItemClickListener,
        WishlistAdapter.OnWishlistRemoveListener {

    private FragmentWishlistBinding binding;
    private WishlistViewModel wishlistViewModel;
    private WishlistAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWishlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        wishlistViewModel = new ViewModelProvider(requireActivity()).get(WishlistViewModel.class);

        adapter = new WishlistAdapter(this, this);
        binding.rvWishlist.setAdapter(adapter);

        observeViewModel();
        wishlistViewModel.loadWishlist();
    }

    private void observeViewModel() {
        wishlistViewModel.getWishlistItems().observe(getViewLifecycleOwner(), products -> {
            if (products == null || products.isEmpty()) {
                binding.rvWishlist.setVisibility(View.GONE);
                binding.layoutEmptyWishlist.setVisibility(View.VISIBLE);
                binding.tvWishlistSubtitle.setText("0 saved items");
            } else {
                binding.rvWishlist.setVisibility(View.VISIBLE);
                binding.layoutEmptyWishlist.setVisibility(View.GONE);
                binding.tvWishlistSubtitle.setText(products.size() + (products.size() == 1 ? " saved item" : " saved items"));
                adapter.submitList(products);
            }
        });

        wishlistViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarWishlist.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        wishlistViewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra(Constants.EXTRA_PRODUCT, product);
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onRemoveClick(Product product, int position) {
        wishlistViewModel.removeFromWishlist(product);
        adapter.removeItem(position);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && wishlistViewModel != null) {
            wishlistViewModel.loadWishlist();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wishlistViewModel != null) {
            wishlistViewModel.loadWishlist();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
