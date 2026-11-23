package com.adithya.campusmarketplace.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.adithya.campusmarketplace.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class DialogUtils {

    private DialogUtils() {
    }

    public interface ConfirmationListener {
        void onConfirm();
    }

    public static void showConfirmationDialog(Context context, String title, String message,
                                              String positiveButtonText,
                                              final ConfirmationListener listener) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    if (listener != null) {
                        listener.onConfirm();
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static void showSuccessDialog(Context context, String title, String message,
                                         final ConfirmationListener listener) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setIcon(R.drawable.ic_check_circle)
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    if (listener != null) {
                        listener.onConfirm();
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static Dialog createLoadingDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(new ProgressBar(context));
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);
        return dialog;
    }
}
