package com.adithya.campusmarketplace.utils;

import android.text.TextUtils;
import android.util.Patterns;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 3;
    }

    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        String cleaned = phone.replaceAll("[^0-9]", "");
        return cleaned.length() >= 10 && cleaned.length() <= 13;
    }

    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }
}
