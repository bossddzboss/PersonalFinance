package com.example.personalfinance.model;

public class AlarmItem {

    public final int id;
    public final String title;
    public final String description;
    public final Category category;
    public final String dueDate;

    public AlarmItem(int id, String title, String description, Category category, String dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.dueDate = dueDate;
    }
}
