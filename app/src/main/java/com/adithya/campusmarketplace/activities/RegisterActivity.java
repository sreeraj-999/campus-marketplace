package com.adithya.campusmarketplace.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.databinding.ActivityRegisterBinding;
import com.adithya.campusmarketplace.utils.ValidationUtils;
import com.adithya.campusmarketplace.viewmodels.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> performRegistration());

        binding.btnGoToLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void performRegistration() {
        String name = binding.etRegName.getText() != null ? binding.etRegName.getText().toString().trim() : "";
        String email = binding.etRegEmail.getText() != null ? binding.etRegEmail.getText().toString().trim() : "";
        String phone = binding.etRegPhone.getText() != null ? binding.etRegPhone.getText().toString().trim() : "";
        String password = binding.etRegPassword.getText() != null ? binding.etRegPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etRegConfirmPassword.getText() != null ? binding.etRegConfirmPassword.getText().toString().trim() : "";

        // Reset errors
        binding.tilRegName.setError(null);
        binding.tilRegEmail.setError(null);
        binding.tilRegPhone.setError(null);
        binding.tilRegPassword.setError(null);
        binding.tilRegConfirmPassword.setError(null);

        // Validation: Empty fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            showSnackbar(getString(R.string.err_empty_fields));
            return;
        }

        // Validation: Name minimum 3 characters
        if (!ValidationUtils.isValidName(name)) {
            binding.tilRegName.setError(getString(R.string.err_short_name));
            showSnackbar(getString(R.string.err_short_name));
            return;
        }

        // Validation: Valid email
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilRegEmail.setError(getString(R.string.err_invalid_email));
            showSnackbar(getString(R.string.err_invalid_email));
            return;
        }

        // Validation: Phone 10 digits
        if (!ValidationUtils.isValidPhone(phone)) {
            binding.tilRegPhone.setError(getString(R.string.err_invalid_phone));
            showSnackbar(getString(R.string.err_invalid_phone));
            return;
        }

        // Validation: Password minimum 6 characters
        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilRegPassword.setError(getString(R.string.err_short_password));
            showSnackbar(getString(R.string.err_short_password));
            return;
        }

        // Validation: Passwords must match
        if (!password.equals(confirmPassword)) {
            binding.tilRegConfirmPassword.setError(getString(R.string.err_password_mismatch));
            showSnackbar(getString(R.string.err_password_mismatch));
            return;
        }

        // Call ViewModel
        authViewModel.register(name, email, phone, password);
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBarRegister.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!isLoading);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showSnackbar(error);
            }
        });

        authViewModel.getRegistrationSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(RegisterActivity.this, R.string.registration_successful, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.registerRoot, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.error_red))
                .setTextColor(getColor(R.color.white))
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
