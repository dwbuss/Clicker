package com.example.clicker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.example.clicker.MainActivity.NOTIFICATION_CHANNEL_ID;

public class MyReceiver extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "notification-id";
    private Location loc;

    public MyReceiver(Location loc) {
        this.loc = loc;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
            Solunar solunar = new Solunar();
            Calendar cal = GregorianCalendar.getInstance();
            solunar.populate(loc, cal);
            String event = solunar.getEventNotification(solunar.parseTime(cal.getTime()));
            if (!event.isEmpty()) {
                String notificationString = "Solunar Event";
                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        context, NOTIFICATION_ID)
                        .setSmallIcon(R.drawable.cc_notification)
                        .setTicker("ticker")
                        .setContentTitle(notificationString)
                        .setChannelId(NOTIFICATION_CHANNEL_ID)
                        .setContentText(event)
                        //.addAction(R.drawable.ic_launcher, "Action Button", pIntent)
                        // Set PendingIntent into Notification
                        //.setContentIntent(pIntent)
                        .setAutoCancel(true);

                NotificationManager notificationmanager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
                    assert notificationmanager != null;
                    notificationmanager.createNotificationChannel(notificationChannel);
                }
                // Build Notification with Notification Manager
                notificationmanager.notify(1, builder.build());
            }
        }
    }
}