package com.example.personalfinance.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateHeaderItem implements Item {

    private final String dateString;

    public DateHeaderItem(String date) {
        this.dateString = date;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public String getTitle() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = dateFormat.parse(dateString);
            dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return dateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
