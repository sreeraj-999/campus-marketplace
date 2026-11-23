package com.adithya.campusmarketplace.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    private DateUtils() {
    }

    public static String formatRelativeTime(long timestamp) {
        if (timestamp <= 0) {
            return "Recently";
        }
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60 * 1000) {
            return "Just now";
        } else if (diff < 60 * 60 * 1000) {
            long minutes = diff / (60 * 1000);
            return minutes + (minutes == 1 ? " min ago" : " mins ago");
        } else if (diff < 24 * 60 * 60 * 1000) {
            long hours = diff / (60 * 60 * 1000);
            return hours + (hours == 1 ? " hr ago" : " hrs ago");
        } else if (diff < 7 * 24 * 60 * 60 * 1000) {
            long days = diff / (24 * 60 * 60 * 1000);
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    public static String formatFullDate(long timestamp) {
        if (timestamp <= 0) {
            return "N/A";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
