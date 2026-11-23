package com.adithya.campusmarketplace.database;

import com.adithya.campusmarketplace.firebase.FirebaseManager;
import com.adithya.campusmarketplace.models.Product;
import com.adithya.campusmarketplace.models.Wishlist;
import com.adithya.campusmarketplace.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WishlistRepository {

    private final FirebaseFirestore db;
    private final CollectionReference wishlistRef;
    private final CollectionReference productsRef;

    public interface WishlistCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public WishlistRepository() {
        this.db = FirebaseManager.getInstance().getFirestore();
        this.wishlistRef = db.collection(Constants.COLLECTION_WISHLIST);
        this.productsRef = db.collection(Constants.COLLECTION_PRODUCTS);
    }

    private String getWishlistDocId(String userId, String productId) {
        return userId + "_" + productId;
    }

    /**
     * Add a product to the user's wishlist
     */
    public void addToWishlist(String userId, String productId, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        String docId = getWishlistDocId(userId, productId);
        Wishlist item = new Wishlist(docId, userId, productId, System.currentTimeMillis());
        wishlistRef.document(docId).set(item)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Remove a product from the user's wishlist
     */
    public void removeFromWishlist(String userId, String productId, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        String docId = getWishlistDocId(userId, productId);
        wishlistRef.document(docId).delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Check if a product is already wishlisted
     */
    public void checkIsWishlisted(String userId, String productId, WishlistCallback<Boolean> callback) {
        String docId = getWishlistDocId(userId, productId);
        wishlistRef.document(docId).get()
                .addOnSuccessListener(documentSnapshot -> callback.onSuccess(documentSnapshot.exists()))
                .addOnFailureListener(callback::onError);
    }

    /**
     * Get all wishlisted product IDs for a user (for quick set matching)
     */
    public void getUserWishlistProductIds(String userId, WishlistCallback<Set<String>> callback) {
        wishlistRef.whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> productIds = new HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Wishlist w = doc.toObject(Wishlist.class);
                        if (w != null && w.getProductId() != null) {
                            productIds.add(w.getProductId());
                        }
                    }
                    callback.onSuccess(productIds);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Get full product objects for the user's wishlist
     */
    public void getUserWishlistProducts(String userId, WishlistCallback<List<Product>> callback) {
        wishlistRef.whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> productIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Wishlist w = doc.toObject(Wishlist.class);
                        if (w != null && w.getProductId() != null) {
                            productIds.add(w.getProductId());
                        }
                    }

                    if (productIds.isEmpty()) {
                        callback.onSuccess(Collections.emptyList());
                        return;
                    }

                    // Firestore whereIn supports up to 30 elements per batch
                    // For student project, if <= 30 we can query directly
                    if (productIds.size() <= 30) {
                        productsRef.whereIn(FieldPath.documentId(), productIds)
                                .get()
                                .addOnSuccessListener(productDocs -> {
                                    List<Product> products = new ArrayList<>();
                                    for (DocumentSnapshot pDoc : productDocs) {
                                        Product p = pDoc.toObject(Product.class);
                                        if (p != null) {
                                            p.setWishlisted(true);
                                            products.add(p);
                                        }
                                    }
                                    callback.onSuccess(products);
                                })
                                .addOnFailureListener(callback::onError);
                    } else {
                        // Query subset
                        productsRef.whereIn(FieldPath.documentId(), productIds.subList(0, 30))
                                .get()
                                .addOnSuccessListener(productDocs -> {
                                    List<Product> products = new ArrayList<>();
                                    for (DocumentSnapshot pDoc : productDocs) {
                                        Product p = pDoc.toObject(Product.class);
                                        if (p != null) {
                                            p.setWishlisted(true);
                                            products.add(p);
                                        }
                                    }
                                    callback.onSuccess(products);
                                })
                                .addOnFailureListener(callback::onError);
                    }
                })
                .addOnFailureListener(callback::onError);
    }
}
