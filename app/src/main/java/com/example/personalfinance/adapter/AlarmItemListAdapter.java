package com.example.personalfinance.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.personalfinance.R;
import com.example.personalfinance.model.AlarmItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class AlarmItemListAdapter extends BaseAdapter {

    private Context mContext;
    private List<AlarmItem> mAlarmItemList;

    public AlarmItemListAdapter(Context context, List<AlarmItem> alarmItemList) {
        this.mContext = context;
        this.mAlarmItemList = alarmItemList;
    }

    @Override
    public int getCount() {
        return mAlarmItemList.size();
    }

    @Override
    public AlarmItem getItem(int position) {
        return mAlarmItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return null;

        if (itemView == null) {
            itemView = inflater.inflate(R.layout.item_alarm, parent, false);
        }

        AlarmItem alarmItem = mAlarmItemList.get(position);

        ImageView categoryImageView = itemView.findViewById(R.id.category_image_view);
        TextView titleTextView = itemView.findViewById(R.id.title_text_view);
        TextView descriptionTextView = itemView.findViewById(R.id.description_text_view);
        TextView dueDateTextView = itemView.findViewById(R.id.due_date_text_view);

        categoryImageView.setImageDrawable(alarmItem.category.getImageDrawable(mContext));
        titleTextView.setText(alarmItem.title);
        descriptionTextView.setText(alarmItem.description);
        dueDateTextView.setText(formatDateTime(alarmItem.dueDate));

        return itemView;
    }

    private String formatDateTime(String dateTimeString) {
        String[] parts = dateTimeString.split(" ");
        String datePart = parts[0];
        String timePart = parts[1];

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = dateFormat.parse(datePart);
            dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            datePart = dateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (timePart.length() > 5) {
            timePart = timePart.substring(0, 5);
        }

        return datePart + "\n" + timePart;
    }
}
