package com.example.personalfinance.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.personalfinance.R;
import com.example.personalfinance.etc.Utils;
import com.example.personalfinance.model.AccountItem;
import com.example.personalfinance.model.Item;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class AccountItemListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Item> mItemList;
    //private int mAccountItemLayoutResId;
    //private int mDateHeaderItemLayoutResId;

    public AccountItemListAdapter(Context context, List<Item> itemList
                                  /*int accountItemLayoutResId, int dateHeaderItemLayoutResId*/) {
        mContext = context;
        mItemList = itemList;
        //mAccountItemLayoutResId = accountItemLayoutResId;
        //mDateHeaderItemLayoutResId = dateHeaderItemLayoutResId;
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public Item getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return null;

        Item item = mItemList.get(position);

        if (item.getType() == 0) { // item is AccountItem
            itemView = inflater.inflate(R.layout.item_account, parent, false);

            TextView titleTextView = itemView.findViewById(R.id.title_text_view);
            TextView descriptionTextView = itemView.findViewById(R.id.description_text_view);
            TextView amountTextView = itemView.findViewById(R.id.amount_text_view);
            ImageView categoryImageView = itemView.findViewById(R.id.category_image_view);

            AccountItem accountItem = (AccountItem) item;
            titleTextView.setText(accountItem.title);
            descriptionTextView.setText(accountItem.description);

            if (accountItem.type == AccountItem.TYPE_INCOME) {
                amountTextView.setTextColor(mContext.getResources().getColor(R.color.income));
            } else if (accountItem.type == AccountItem.TYPE_EXPENSE) {
                amountTextView.setTextColor(mContext.getResources().getColor(R.color.expense));
            } else if (accountItem.type == AccountItem.TYPE_INVESTMENT) {
                amountTextView.setTextColor(mContext.getResources().getColor(R.color.investment));
            }
            amountTextView.setText(Utils.formatCurrency(accountItem.amount));

            categoryImageView.setImageDrawable(accountItem.category.getImageDrawable(mContext));

        } else if (item.getType() == 1) { // item is DateHeaderItem
            itemView = inflater.inflate(R.layout.item_date_header, parent, false);
            itemView.setEnabled(false);
            itemView.setOnClickListener(null);

            TextView titleTextView = itemView.findViewById(R.id.title_text_view);
            titleTextView.setText(item.getTitle());

        } else { // item is TotalItem
            itemView = inflater.inflate(R.layout.item_total, parent, false);
            itemView.setEnabled(false);
            itemView.setOnClickListener(null);

            TextView titleTextView = itemView.findViewById(R.id.total_amount_text_view);
            titleTextView.setText(item.getTitle());
        }

        return itemView;
    }
}
