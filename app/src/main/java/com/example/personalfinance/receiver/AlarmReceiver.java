package com.example.personalfinance.receiver;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmManager.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "ALARM!!!", Toast.LENGTH_LONG).show();
    }
}
