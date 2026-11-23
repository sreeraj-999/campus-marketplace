package com.adithya.campusmarketplace.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.databinding.ActivityProfileBinding;
import com.adithya.campusmarketplace.fragments.ProfileFragment;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarProfile.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerProfile, new ProfileFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
