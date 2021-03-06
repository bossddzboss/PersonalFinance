package com.example.personalfinance.etc;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.personalfinance.receiver.AlarmReceiver;

import java.util.Calendar;
import java.util.Date;

public class ExpenseAlarmManager {

    @SuppressLint("StaticFieldLeak")
    private static ExpenseAlarmManager sInstance;
    private final Context mContext;

    private ExpenseAlarmManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static ExpenseAlarmManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ExpenseAlarmManager(context);
        }
        return sInstance;
    }

    // alarmId ใช้ _id ในเทเบิล alarm ใน db
    public void setAlarm(int alarmId, Date alarmDate) {
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(mContext, alarmId, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        //todo: set alarm's date/time
        /*calendar.set(Calendar.DAY_OF_MONTH, 28);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 44);*/

        if (am != null) {
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        }
    }

    public void cancelAlarm(int alarmId) {
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(mContext, alarmId, intent, 0);

        if (am != null) {
            am.cancel(alarmIntent);
        }
    }
}
