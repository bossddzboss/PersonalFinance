package com.example.personalfinance.model;

import com.example.personalfinance.etc.Utils;

public class TotalItem implements Item {

    private final int mTotalAmount;

    public TotalItem(int totalAmount) {
        this.mTotalAmount = totalAmount;
    }

    @Override
    public int getType() {
        return 2;
    }

    @Override
    public String getTitle() {
        return Utils.formatCurrency(mTotalAmount);
    }
}
