package com.example.personalfinance.model;

public class AccountItem implements Item {

    public static final int TYPE_INCOME = 0;
    public static final int TYPE_EXPENSE = 1;
    public static final int TYPE_INVESTMENT = 2;

    public int id;
    public String title;
    public String description;
    public int amount;
    public int type;
    public int categoryId;
    public Category category;
    public String date;

    public AccountItem() {}

    public AccountItem(int id, String title, String description, int amount, int type,
                       Category category, String date) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.categoryId = category.id;
        this.category = category;
        this.date = date;
    }

    public AccountItem(int id, String title, String description, int amount, int type,
                       int categoryId, String date) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.category = null;
        this.date = date;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
