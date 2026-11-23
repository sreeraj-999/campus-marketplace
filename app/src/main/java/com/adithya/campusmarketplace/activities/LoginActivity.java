package com.adithya.campusmarketplace.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.databinding.ActivityLoginBinding;
import com.adithya.campusmarketplace.utils.ValidationUtils;
import com.adithya.campusmarketplace.viewmodels.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> performLogin());

        binding.btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void performLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        // Reset errors
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        // Validation: Empty fields
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showSnackbar(getString(R.string.err_empty_fields));
            if (TextUtils.isEmpty(email)) binding.tilEmail.setError(getString(R.string.err_empty_fields));
            if (TextUtils.isEmpty(password)) binding.tilPassword.setError(getString(R.string.err_empty_fields));
            return;
        }

        // Validation: Invalid email
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.err_invalid_email));
            showSnackbar(getString(R.string.err_invalid_email));
            return;
        }

        // Call ViewModel login
        authViewModel.login(email, password);
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBarLogin.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showSnackbar(error);
            }
        });

        authViewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.loginRoot, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.error_red))
                .setTextColor(getColor(R.color.white))
                .show();
    }
}
