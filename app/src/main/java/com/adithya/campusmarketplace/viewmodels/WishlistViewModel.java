package com.adithya.campusmarketplace.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.adithya.campusmarketplace.database.WishlistRepository;
import com.adithya.campusmarketplace.firebase.FirebaseManager;
import com.adithya.campusmarketplace.models.Product;

import java.util.ArrayList;
import java.util.List;

public class WishlistViewModel extends ViewModel {

    private final WishlistRepository wishlistRepo;

    private final MutableLiveData<List<Product>> wishlistItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCurrentProductWishlisted = new MutableLiveData<>(false);

    public WishlistViewModel() {
        this.wishlistRepo = new WishlistRepository();
    }

    public LiveData<List<Product>> getWishlistItems() {
        return wishlistItems;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public LiveData<Boolean> getIsCurrentProductWishlisted() {
        return isCurrentProductWishlisted;
    }

    public void loadWishlist() {
        String userId = FirebaseManager.getInstance().getCurrentUserId();
        if (userId == null) return;

        isLoading.setValue(true);
        wishlistRepo.getUserWishlistProducts(userId, new WishlistRepository.WishlistCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                isLoading.setValue(false);
                wishlistItems.setValue(products);
            }

            @Override
            public void onError(Exception e) {
                isLoading.setValue(false);
                toastMessage.setValue("Error loading wishlist: " + e.getMessage());
            }
        });
    }

    public void checkWishlistStatus(String productId) {
        String userId = FirebaseManager.getInstance().getCurrentUserId();
        if (userId == null || productId == null) return;

        wishlistRepo.checkIsWishlisted(userId, productId, new WishlistRepository.WishlistCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isWishlisted) {
                isCurrentProductWishlisted.setValue(isWishlisted);
            }

            @Override
            public void onError(Exception e) {
                isCurrentProductWishlisted.setValue(false);
            }
        });
    }

    public void toggleWishlist(Product product) {
        String userId = FirebaseManager.getInstance().getCurrentUserId();
        if (userId == null || product == null || product.getProductId() == null) return;

        boolean newStatus = !product.isWishlisted();
        product.setWishlisted(newStatus);

        if (newStatus) {
            wishlistRepo.addToWishlist(userId, product.getProductId(),
                    aVoid -> {
                        isCurrentProductWishlisted.setValue(true);
                        toastMessage.setValue("Added to Wishlist");
                        loadWishlist();
                    },
                    e -> {
                        product.setWishlisted(!newStatus);
                        toastMessage.setValue("Failed to update: " + e.getMessage());
                    });
        } else {
            wishlistRepo.removeFromWishlist(userId, product.getProductId(),
                    aVoid -> {
                        isCurrentProductWishlisted.setValue(false);
                        toastMessage.setValue("Removed from Wishlist");
                        loadWishlist();
                    },
                    e -> {
                        product.setWishlisted(!newStatus);
                        toastMessage.setValue("Failed to update: " + e.getMessage());
                    });
        }
    }

    public void removeFromWishlist(Product product) {
        String userId = FirebaseManager.getInstance().getCurrentUserId();
        if (userId == null || product == null || product.getProductId() == null) return;

        wishlistRepo.removeFromWishlist(userId, product.getProductId(),
                aVoid -> {
                    toastMessage.setValue("Removed from Wishlist");
                    loadWishlist();
                },
                e -> toastMessage.setValue("Failed to remove: " + e.getMessage()));
    }
}
