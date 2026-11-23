package com.adithya.campusmarketplace.utils;

public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // Firestore Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_PRODUCTS = "products";
    public static final String COLLECTION_WISHLIST = "wishlist";

    // Categories
    public static final String CATEGORY_ALL = "All";
    public static final String CATEGORY_BOOKS = "Books";
    public static final String CATEGORY_ELECTRONICS = "Electronics";
    public static final String CATEGORY_HOSTEL = "Hostel";
    public static final String CATEGORY_FURNITURE = "Furniture";
    public static final String CATEGORY_SPORTS = "Sports";
    public static final String CATEGORY_OTHERS = "Others";

    public static final String[] CATEGORIES_FILTER = {
            CATEGORY_ALL, CATEGORY_BOOKS, CATEGORY_ELECTRONICS,
            CATEGORY_HOSTEL, CATEGORY_FURNITURE, CATEGORY_SPORTS, CATEGORY_OTHERS
    };

    public static final String[] CATEGORIES_POST = {
            CATEGORY_BOOKS, CATEGORY_ELECTRONICS,
            CATEGORY_HOSTEL, CATEGORY_FURNITURE, CATEGORY_SPORTS, CATEGORY_OTHERS
    };

    // Conditions
    public static final String CONDITION_NEW = "New";
    public static final String CONDITION_GOOD = "Good";
    public static final String CONDITION_USED = "Used";

    public static final String[] CONDITIONS = {
            CONDITION_NEW, CONDITION_GOOD, CONDITION_USED
    };

    // Sorting Options
    public static final int SORT_LATEST = 0;
    public static final int SORT_PRICE_LOW_HIGH = 1;
    public static final int SORT_PRICE_HIGH_LOW = 2;

    // Intent Extras
    public static final String EXTRA_PRODUCT = "extra_product";
    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    public static final String EXTRA_USER_NAME = "extra_user_name";
}
