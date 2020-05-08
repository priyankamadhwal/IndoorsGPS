package com.example.indoorsgps;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper extends Application {

    public static final String CHANNEL_ID = "locationUpdatesServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Updates Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.setSound(null, null);
            notificationChannel.setShowBadge(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public Notification sendNotification(Context context, String title, String body, PendingIntent pendingIntent) {

        return  new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_location)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
    }

    public Notification createNotification(Context context, String content) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0,
                new Intent(context, MainActivity.class),
                0);

        Notification notification = sendNotification(
                context,
                "Sending Location Updates",
                content,
                pendingIntent
        );
        return notification;
    }

    public void updateNotification(Context context, double latitude, double longitude, double altitude) {
        String content = "Latitude : " + latitude + "\nLongitude : " + longitude + "\nAltitude : " + altitude;

        Notification notification = createNotification(context, content);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(1, notification);
    }
}
