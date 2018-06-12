package com.example.personalfinance.model;

import java.util.Date;

public class AlarmItem {

    public final int id;
    public final String title;
    public final String description;
    public final Category category;
    public final Date dueDate;

    public AlarmItem(int id, String title, String description, Category category, Date dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.dueDate = dueDate;
    }
}
