package com.adithya.campusmarketplace.utils;

import com.adithya.campusmarketplace.R;

public final class CategoryUtils {

    private CategoryUtils() {
    }

    public static int getCategoryPlaceholder(String category) {
        if (category == null) {
            return R.drawable.placeholder_product;
        }

        switch (category.trim()) {
            case Constants.CATEGORY_BOOKS:
                return R.drawable.placeholder_books;
            case Constants.CATEGORY_ELECTRONICS:
                return R.drawable.placeholder_electronics;
            case Constants.CATEGORY_HOSTEL:
                return R.drawable.placeholder_hostel;
            case Constants.CATEGORY_FURNITURE:
                return R.drawable.placeholder_furniture;
            case Constants.CATEGORY_SPORTS:
                return R.drawable.placeholder_sports;
            case Constants.CATEGORY_OTHERS:
            default:
                return R.drawable.placeholder_others;
        }
    }
}
