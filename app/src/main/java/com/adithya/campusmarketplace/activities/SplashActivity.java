package com.adithya.campusmarketplace.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.databinding.ActivitySplashBinding;
import com.adithya.campusmarketplace.firebase.FirebaseManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Splash fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        binding.splashContent.startAnimation(fadeIn);

        // Automatically navigate after 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (FirebaseManager.getInstance().isUserLoggedIn()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
}
