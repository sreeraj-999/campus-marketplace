package com.adithya.campusmarketplace.database;

import com.adithya.campusmarketplace.firebase.FirebaseManager;
import com.adithya.campusmarketplace.models.Product;
import com.adithya.campusmarketplace.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private final FirebaseFirestore db;
    private final CollectionReference productsRef;

    public interface ProductCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public ProductRepository() {
        this.db = FirebaseManager.getInstance().getFirestore();
        this.productsRef = db.collection(Constants.COLLECTION_PRODUCTS);
    }

    /**
     * Add a new product to Firestore
     */
    public void addProduct(Product product, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        String docId = productsRef.document().getId();
        product.setProductId(docId);
        productsRef.document(docId).set(product)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Update an existing product in Firestore
     */
    public void updateProduct(Product product, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        if (product.getProductId() == null) return;
        productsRef.document(product.getProductId()).set(product)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Delete a product from Firestore
     */
    public void deleteProduct(String productId, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        productsRef.document(productId).delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * Fetch all products ordered by latest created date
     */
    public void getAllProducts(ProductCallback<List<Product>> callback) {
        productsRef.orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            list.add(p);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Fetch products posted by the current user
     */
    public void getMyListings(String sellerId, ProductCallback<List<Product>> callback) {
        productsRef.whereEqualTo("sellerId", sellerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            list.add(p);
                        }
                    }
                    // Sort descending in memory to avoid composite index requirements
                    list.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Fetch similar products under the same category excluding the current product
     */
    public void getSimilarProducts(String category, String excludeProductId, ProductCallback<List<Product>> callback) {
        productsRef.whereEqualTo("category", category)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Product p = doc.toObject(Product.class);
                        if (p != null && (excludeProductId == null || !p.getProductId().equals(excludeProductId))) {
                            list.add(p);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }
}
