package com.adithya.campusmarketplace.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.activities.LoginActivity;
import com.adithya.campusmarketplace.activities.MyListingsActivity;
import com.adithya.campusmarketplace.databinding.DialogEditProfileBinding;
import com.adithya.campusmarketplace.databinding.FragmentProfileBinding;
import com.adithya.campusmarketplace.firebase.FirebaseManager;
import com.adithya.campusmarketplace.models.User;
import com.adithya.campusmarketplace.utils.Constants;
import com.adithya.campusmarketplace.utils.DialogUtils;
import com.adithya.campusmarketplace.utils.ValidationUtils;
import com.adithya.campusmarketplace.viewmodels.AuthViewModel;
import com.adithya.campusmarketplace.viewmodels.ProductViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.atomic.AtomicInteger;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;
    private ProductViewModel productViewModel;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        setupListeners();
        observeViewModel();

        authViewModel.loadCurrentUserProfile();
    }

    private void setupListeners() {
        // My Listings Button
        binding.btnMyListings.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MyListingsActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Edit Profile Button
        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Campus Safety Notice
        binding.btnCampusNotice.setOnClickListener(v -> DialogUtils.showSuccessDialog(
                requireActivity(),
                getString(R.string.campus_notice_title),
                getString(R.string.campus_notice_msg),
                null
        ));

        // Clear All Products from Database
        binding.btnClearAllProducts.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Clear Database")
                    .setIcon(R.drawable.ic_delete)
                    .setMessage("Are you sure you want to delete all products from the cloud database? This will clear all existing products so you start fresh.")
                    .setPositiveButton("Delete All", (dialog, which) -> performClearDatabase())
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Logout Button
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void performClearDatabase() {
        Dialog loadingDialog = DialogUtils.createLoadingDialog(requireActivity());
        loadingDialog.show();

        FirebaseFirestore db = FirebaseManager.getInstance().getFirestore();
        db.collection(Constants.COLLECTION_PRODUCTS).get().addOnSuccessListener(snapshots -> {
            if (snapshots.isEmpty()) {
                loadingDialog.dismiss();
                DialogUtils.showSuccessDialog(requireActivity(), "Database Clean", "There are no products in the database.", null);
                if (productViewModel != null) {
                    productViewModel.fetchProducts();
                }
                return;
            }

            int total = snapshots.size();
            AtomicInteger deletedCount = new AtomicInteger(0);
            AtomicInteger failedCount = new AtomicInteger(0);
            AtomicInteger processed = new AtomicInteger(0);

            for (DocumentSnapshot doc : snapshots) {
                doc.getReference().delete()
                        .addOnSuccessListener(aVoid -> {
                            deletedCount.incrementAndGet();
                            checkClearProgress(processed.incrementAndGet(), total, deletedCount.get(), failedCount.get(), loadingDialog);
                        })
                        .addOnFailureListener(e -> {
                            failedCount.incrementAndGet();
                            checkClearProgress(processed.incrementAndGet(), total, deletedCount.get(), failedCount.get(), loadingDialog);
                        });
            }
        }).addOnFailureListener(e -> {
            loadingDialog.dismiss();
            Toast.makeText(requireContext(), "Failed to access database: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void checkClearProgress(int current, int total, int deleted, int failed, Dialog loadingDialog) {
        if (current >= total) {
            loadingDialog.dismiss();
            if (productViewModel != null) {
                productViewModel.fetchProducts();
            }
            if (failed == 0) {
                DialogUtils.showSuccessDialog(
                        requireActivity(),
                        "Database Cleared",
                        "Successfully cleared all " + deleted + " product(s). Your marketplace is now 100% clean and fresh!",
                        null
                );
            } else {
                new MaterialAlertDialogBuilder(requireActivity())
                        .setTitle("Products Cleared (" + deleted + " removed)")
                        .setMessage(failed + " demo product(s) could not be removed directly from the app due to Firestore security permissions (they were created under a demo seller account).\n\nTo remove them completely in 5 seconds:\n1. Open Firebase Console in your browser\n2. Open Firestore Database\n3. Click ⋮ next to the 'products' collection and click 'Delete collection'.")
                        .setPositiveButton("Got It", null)
                        .show();
            }
        }
    }

    private void showEditProfileDialog() {
        DialogEditProfileBinding dialogBinding = DialogEditProfileBinding.inflate(getLayoutInflater());

        if (currentUser != null) {
            dialogBinding.etProfileName.setText(currentUser.getName());
            dialogBinding.etProfilePhone.setText(currentUser.getPhone());
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireActivity())
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, null) // Set null to override and keep dialog open on error
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
                String newName = dialogBinding.etProfileName.getText() != null ?
                        dialogBinding.etProfileName.getText().toString().trim() : "";
                String newPhone = dialogBinding.etProfilePhone.getText() != null ?
                        dialogBinding.etProfilePhone.getText().toString().trim() : "";

                if (!ValidationUtils.isValidName(newName)) {
                    dialogBinding.tilProfileName.setError(getString(R.string.err_short_name));
                    return;
                }

                if (!ValidationUtils.isValidPhone(newPhone)) {
                    dialogBinding.tilProfilePhone.setError(getString(R.string.err_invalid_phone));
                    return;
                }

                authViewModel.updateUserProfile(newName, newPhone);
                Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showLogoutConfirmation() {
        if (getActivity() == null) return;

        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setIcon(R.drawable.ic_lock)
                .setPositiveButton(R.string.logout, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        authViewModel.logout();
        Context context = getContext();
        if (context == null && getActivity() != null) {
            context = getActivity().getApplicationContext();
        }
        if (context != null) {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            getActivity().finish();
        }
    }

    private void observeViewModel() {
        authViewModel.getCurrentUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                binding.tvProfileName.setText(user.getName() != null ? user.getName() : "Campus Student");
                binding.tvProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                binding.tvProfilePhone.setText(user.getPhone() != null ? user.getPhone() : "Not Provided");
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && authViewModel != null) {
            authViewModel.loadCurrentUserProfile();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (authViewModel != null) {
            authViewModel.loadCurrentUserProfile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
