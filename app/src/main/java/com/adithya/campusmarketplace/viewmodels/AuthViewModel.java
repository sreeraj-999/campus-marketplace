package com.adithya.campusmarketplace.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.adithya.campusmarketplace.firebase.FirebaseManager;
import com.adithya.campusmarketplace.models.User;
import com.adithya.campusmarketplace.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthViewModel extends ViewModel {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    private final MutableLiveData<User> currentUserProfile = new MutableLiveData<>();

    public AuthViewModel() {
        this.auth = FirebaseManager.getInstance().getAuth();
        this.firestore = FirebaseManager.getInstance().getFirestore();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<Boolean> getRegistrationSuccess() {
        return registrationSuccess;
    }

    public LiveData<User> getCurrentUserProfile() {
        return currentUserProfile;
    }

    public void login(String email, String password) {
        isLoading.setValue(true);
        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    isLoading.setValue(false);
                    loginSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                });
    }

    public void register(String name, String email, String phone, String password) {
        isLoading.setValue(true);
        auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        User newUser = new User(uid, name.trim(), email.trim(), phone.trim(), System.currentTimeMillis());
                        firestore.collection(Constants.COLLECTION_USERS).document(uid)
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    isLoading.setValue(false);
                                    currentUserProfile.setValue(newUser);
                                    registrationSuccess.setValue(true);
                                })
                                .addOnFailureListener(e -> {
                                    isLoading.setValue(false);
                                    errorMessage.setValue("Saved account, but profile setup failed: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                });
    }

    public void loadCurrentUserProfile() {
        String uid = FirebaseManager.getInstance().getCurrentUserId();
        if (uid == null) {
            return;
        }

        isLoading.setValue(true);
        firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isLoading.setValue(false);
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        currentUserProfile.setValue(user);
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                });
    }

    public void updateUserProfile(String name, String phone) {
        String uid = FirebaseManager.getInstance().getCurrentUserId();
        if (uid == null) return;

        isLoading.setValue(true);
        firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .update("name", name.trim(), "phone", phone.trim())
                .addOnSuccessListener(aVoid -> {
                    isLoading.setValue(false);
                    loadCurrentUserProfile();
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                });
    }

    public void logout() {
        FirebaseManager.getInstance().signOut();
        currentUserProfile.setValue(null);
        loginSuccess.setValue(null);
        registrationSuccess.setValue(null);
    }
}
