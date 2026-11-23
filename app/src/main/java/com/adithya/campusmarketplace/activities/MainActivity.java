package com.adithya.campusmarketplace.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.databinding.ActivityMainBinding;
import com.adithya.campusmarketplace.fragments.HomeFragment;
import com.adithya.campusmarketplace.fragments.ProfileFragment;
import com.adithya.campusmarketplace.fragments.SearchFragment;
import com.adithya.campusmarketplace.fragments.SellFragment;
import com.adithya.campusmarketplace.fragments.WishlistFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment searchFragment = new SearchFragment();
    private final Fragment sellFragment = new SellFragment();
    private final Fragment wishlistFragment = new WishlistFragment();
    private final Fragment profileFragment = new ProfileFragment();
    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
    }

    @SuppressLint("NonConstantResourceId")
    private void setupBottomNavigation() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Add all 5 fragments once and hide the inactive ones for smooth, state-preserving navigation
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, profileFragment, "5").hide(profileFragment)
                .add(R.id.fragment_container, wishlistFragment, "4").hide(wishlistFragment)
                .add(R.id.fragment_container, sellFragment, "3").hide(sellFragment)
                .add(R.id.fragment_container, searchFragment, "2").hide(searchFragment)
                .add(R.id.fragment_container, homeFragment, "1")
                .commit();

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                switchFragment(homeFragment);
                return true;
            } else if (itemId == R.id.navigation_search) {
                switchFragment(searchFragment);
                return true;
            } else if (itemId == R.id.navigation_sell) {
                switchFragment(sellFragment);
                return true;
            } else if (itemId == R.id.navigation_wishlist) {
                switchFragment(wishlistFragment);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });
    }

    private void switchFragment(Fragment targetFragment) {
        if (activeFragment != targetFragment) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .hide(activeFragment)
                    .show(targetFragment)
                    .commit();
            activeFragment = targetFragment;
        }
    }

    public void navigateToSearchTab() {
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_search);
    }

    public void navigateToHomeTab() {
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
    }

    @Override
    public void onBackPressed() {
        if (activeFragment != homeFragment) {
            navigateToHomeTab();
        } else {
            super.onBackPressed();
        }
    }
}
