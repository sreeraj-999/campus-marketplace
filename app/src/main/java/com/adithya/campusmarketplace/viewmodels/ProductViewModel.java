package com.adithya.campusmarketplace.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.adithya.campusmarketplace.database.ProductRepository;
import com.adithya.campusmarketplace.database.WishlistRepository;
import com.adithya.campusmarketplace.firebase.FirebaseManager;
import com.adithya.campusmarketplace.models.Product;
import com.adithya.campusmarketplace.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProductViewModel extends ViewModel {

    private final ProductRepository productRepo;
    private final WishlistRepository wishlistRepo;

    private final MutableLiveData<List<Product>> allProducts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Product>> displayedProducts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Product>> searchResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Product>> myListings = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Product>> similarProducts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();

    private String currentSelectedCategory = Constants.CATEGORY_ALL;

    public ProductViewModel() {
        this.productRepo = new ProductRepository();
        this.wishlistRepo = new WishlistRepository();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public LiveData<List<Product>> getDisplayedProducts() {
        return displayedProducts;
    }

    public LiveData<List<Product>> getSearchResults() {
        return searchResults;
    }

    public LiveData<List<Product>> getMyListings() {
        return myListings;
    }

    public LiveData<List<Product>> getSimilarProducts() {
        return similarProducts;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public void resetOperationSuccess() {
        operationSuccess.setValue(null);
    }

    public void fetchProducts() {
        isLoading.setValue(true);
        String userId = FirebaseManager.getInstance().getCurrentUserId();

        productRepo.getAllProducts(new ProductRepository.ProductCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                if (userId != null) {
                    wishlistRepo.getUserWishlistProductIds(userId, new WishlistRepository.WishlistCallback<Set<String>>() {
                        @Override
                        public void onSuccess(Set<String> wishlistedIds) {
                            for (Product p : products) {
                                p.setWishlisted(wishlistedIds.contains(p.getProductId()));
                            }
                            allProducts.setValue(products);
                            applyCategoryFilter(currentSelectedCategory);
                            isLoading.setValue(false);
                        }

                        @Override
                        public void onError(Exception e) {
                            allProducts.setValue(products);
                            applyCategoryFilter(currentSelectedCategory);
                            isLoading.setValue(false);
                        }
                    });
                } else {
                    allProducts.setValue(products);
                    applyCategoryFilter(currentSelectedCategory);
                    isLoading.setValue(false);
                }
            }

            @Override
            public void onError(Exception e) {
                isLoading.setValue(false);
                errorMessage.setValue(e.getMessage());
            }
        });
    }

    public void filterByCategory(String category) {
        this.currentSelectedCategory = category;
        applyCategoryFilter(category);
    }

    private void applyCategoryFilter(String category) {
        List<Product> fullList = allProducts.getValue();
        if (fullList == null) return;

        if (category == null || category.equalsIgnoreCase(Constants.CATEGORY_ALL)) {
            displayedProducts.setValue(new ArrayList<>(fullList));
        } else {
            List<Product> filtered = new ArrayList<>();
            for (Product p : fullList) {
                if (category.equalsIgnoreCase(p.getCategory())) {
                    filtered.add(p);
                }
            }
            displayedProducts.setValue(filtered);
        }
    }

    public void searchAndSort(String query, String category, int sortOption) {
        List<Product> fullList = allProducts.getValue();
        if (fullList == null) {
            searchResults.setValue(new ArrayList<>());
            return;
        }

        List<Product> results = new ArrayList<>();
        String queryLower = query != null ? query.trim().toLowerCase(Locale.getDefault()) : "";

        for (Product p : fullList) {
            boolean matchesCategory = (category == null ||
                    category.trim().equalsIgnoreCase(Constants.CATEGORY_ALL) ||
                    (p.getCategory() != null && category.trim().equalsIgnoreCase(p.getCategory().trim())));

            boolean matchesQuery = queryLower.isEmpty() ||
                    (p.getTitle() != null && p.getTitle().toLowerCase(Locale.getDefault()).contains(queryLower)) ||
                    (p.getDescription() != null && p.getDescription().toLowerCase(Locale.getDefault()).contains(queryLower)) ||
                    (p.getCategory() != null && p.getCategory().toLowerCase(Locale.getDefault()).contains(queryLower)) ||
                    (p.getSellerName() != null && p.getSellerName().toLowerCase(Locale.getDefault()).contains(queryLower));

            if (matchesCategory && matchesQuery) {
                results.add(p);
            }
        }

        // Apply Sorting
        if (sortOption == Constants.SORT_PRICE_LOW_HIGH) {
            results.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
        } else if (sortOption == Constants.SORT_PRICE_HIGH_LOW) {
            results.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
        } else {
            // Latest
            results.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        }

        searchResults.setValue(results);
    }

    public void addProduct(Product product) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        operationSuccess.setValue(null);
        productRepo.addProduct(product,
                aVoid -> {
                    isLoading.setValue(false);
                    operationSuccess.setValue(true);
                    fetchProducts();
                },
                e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                });
    }

    public void updateProduct(Product product) {
        isLoading.setValue(true);
        productRepo.updateProduct(product,
                aVoid -> {
                    isLoading.setValue(false);
                    operationSuccess.setValue(true);
                    fetchProducts();
                    if (product.getSellerId() != null) {
                        fetchMyListings(product.getSellerId());
                    }
                },
                e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                });
    }

    public void deleteProduct(String productId, String sellerId) {
        isLoading.setValue(true);
        productRepo.deleteProduct(productId,
                aVoid -> {
                    isLoading.setValue(false);
                    operationSuccess.setValue(true);
                    fetchProducts();
                    if (sellerId != null) {
                        fetchMyListings(sellerId);
                    }
                },
                e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                });
    }

    public void fetchMyListings(String sellerId) {
        isLoading.setValue(true);
        productRepo.getMyListings(sellerId, new ProductRepository.ProductCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                isLoading.setValue(false);
                myListings.setValue(products);
            }

            @Override
            public void onError(Exception e) {
                isLoading.setValue(false);
                errorMessage.setValue(e.getMessage());
            }
        });
    }

    public void fetchSimilarProducts(String category, String currentProductId) {
        productRepo.getSimilarProducts(category, currentProductId, new ProductRepository.ProductCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                similarProducts.setValue(products);
            }

            @Override
            public void onError(Exception e) {
                // Non-critical, ignore or log
            }
        });
    }
}
