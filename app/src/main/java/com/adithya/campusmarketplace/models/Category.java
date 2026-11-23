package com.adithya.campusmarketplace.models;

import java.io.Serializable;

/**
 * Category model for horizontal chip filters in Home and Search screens.
 */
public class Category implements Serializable {
    private String name;
    private int iconResId;
    private boolean isSelected;

    public Category(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
        this.isSelected = false;
    }

    public Category(String name, int iconResId, boolean isSelected) {
        this.name = name;
        this.iconResId = iconResId;
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
