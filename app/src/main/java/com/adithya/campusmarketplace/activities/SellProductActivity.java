package com.adithya.campusmarketplace.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.databinding.ActivitySellProductBinding;
import com.adithya.campusmarketplace.fragments.SellFragment;

public class SellProductActivity extends AppCompatActivity {

    private ActivitySellProductBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarSell.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerSell, new SellFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
