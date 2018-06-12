package com.example.personalfinance.receiver;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.example.personalfinance.R;
import com.example.personalfinance.db.FinanceDb;
import com.example.personalfinance.model.AlarmItem;

import static com.example.personalfinance.etc.Const.KEY_ALARM_ID;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmManager.class.getName();
    private static final int UNDEFINED_ID = -1;

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        int alarmId = intent.getIntExtra(KEY_ALARM_ID, UNDEFINED_ID);
        if (alarmId != UNDEFINED_ID) {
            AlarmItem alarmItem = new FinanceDb(mContext).getAlarmItemById(alarmId);
            createNotification(alarmItem);
            Toast.makeText(context, "ALARM! id = " + alarmId, Toast.LENGTH_LONG).show();
        }
    }

    private void createNotification(AlarmItem alarmItem) {
        Bitmap categoryBitmap = drawableToBitmap(alarmItem.category.getImageDrawable(mContext));

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setLargeIcon(categoryBitmap)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(alarmItem.title)
                .setContentText(alarmItem.description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(alarmItem.id, mBuilder.build());
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            // Single color bitmap will be created of 1x1 pixel
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
