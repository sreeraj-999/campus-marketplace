package com.adithya.campusmarketplace.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;

import com.adithya.campusmarketplace.models.Product;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class ImageUtils {

    private ImageUtils() {
    }

    /**
     * Display either the custom uploaded Base64 image or the category vector placeholder
     */
    public static void displayProductImage(ImageView imageView, Product product) {
        if (imageView == null) return;

        if (product != null && product.getImageBase64() != null && !product.getImageBase64().trim().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(product.getImageBase64().trim(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        // Fallback to crisp category placeholder
        int placeholderRes = CategoryUtils.getCategoryPlaceholder(product != null ? product.getCategory() : null);
        imageView.setImageResource(placeholderRes);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    /**
     * Decode and downscale a bitmap from a content Uri to avoid OutOfMemory errors
     */
    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int maxDimension) {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            InputStream input = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            if (input != null) input.close();

            int inSampleSize = 1;
            int max = Math.max(options.outWidth, options.outHeight);
            if (max > maxDimension) {
                inSampleSize = Math.round((float) max / (float) maxDimension);
            }

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            InputStream secondInput = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(secondInput, null, options);
            if (secondInput != null) secondInput.close();

            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convert bitmap to compressed JPEG Base64 string
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }
}
