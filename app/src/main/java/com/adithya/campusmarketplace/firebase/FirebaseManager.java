package com.adithya.campusmarketplace.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {

    private static FirebaseManager instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void signOut() {
        auth.signOut();
    }
}
