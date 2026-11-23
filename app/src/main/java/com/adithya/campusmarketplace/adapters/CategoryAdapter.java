package com.adithya.campusmarketplace.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.adithya.campusmarketplace.R;
import com.adithya.campusmarketplace.databinding.ItemCategoryChipBinding;
import com.adithya.campusmarketplace.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;
    private int selectedPosition = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categoryList, OnCategoryClickListener listener) {
        if (categoryList != null) {
            this.categories.addAll(categoryList);
        }
        this.listener = listener;
    }

    public void setSelectedCategory(String categoryName) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getName().equalsIgnoreCase(categoryName)) {
                int prev = selectedPosition;
                selectedPosition = i;
                notifyItemChanged(prev);
                notifyItemChanged(selectedPosition);
                break;
            }
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryChipBinding binding = ItemCategoryChipBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        boolean isSelected = position == selectedPosition;
        holder.bind(category, isSelected);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryChipBinding binding;

        CategoryViewHolder(ItemCategoryChipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Category category, boolean isSelected) {
            binding.tvChipName.setText(category.getName());

            if (isSelected) {
                binding.chipContainer.setBackgroundResource(R.drawable.bg_chip_selected);
                binding.tvChipName.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.white));
            } else {
                binding.chipContainer.setBackgroundResource(R.drawable.bg_chip_unselected);
                binding.tvChipName.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.text_dark));
            }

            binding.chipContainer.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos != selectedPosition) {
                    int prev = selectedPosition;
                    selectedPosition = pos;
                    notifyItemChanged(prev);
                    notifyItemChanged(selectedPosition);
                    if (listener != null) {
                        listener.onCategoryClick(categories.get(pos));
                    }
                }
            });
        }
    }
}
