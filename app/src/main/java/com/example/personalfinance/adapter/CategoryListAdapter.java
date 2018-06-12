package com.example.personalfinance.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.personalfinance.R;
import com.example.personalfinance.model.Category;

import java.util.List;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Category> mCategoryList;
    private final CategoryListAdapterListener mListener;

    public CategoryListAdapter(Context context,
                               List<Category> categoryList,
                               CategoryListAdapterListener listener) {
        this.mContext = context;
        this.mCategoryList = categoryList;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    //private int mSelectedCategoryPosition = 0;
    private View mLastSelectedCategoryView = null;

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (mCategoryList != null) {
            final Category category = mCategoryList.get(position);

            holder.mCategory = category;
            holder.mCategoryImageView.setImageDrawable(category.getImageDrawable(mContext));
            holder.mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onCategoryClick(category);

                        if (mLastSelectedCategoryView != null) {
                            mLastSelectedCategoryView.setBackgroundColor(Color.TRANSPARENT);
                        }
                        view.setBackgroundColor(Color.parseColor("#dedede"));
                        //mSelectedCategoryPosition = position;
                        mLastSelectedCategoryView = view;
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mCategoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final View mRootView;
        private final ImageView mCategoryImageView;
        private Category mCategory;

        public ViewHolder(View itemView) {
            super(itemView);
            this.mRootView = itemView;
            this.mCategoryImageView = itemView.findViewById(R.id.category_image_view);;
        }
    }

    public interface CategoryListAdapterListener {
        void onCategoryClick(Category category);
    }
}
