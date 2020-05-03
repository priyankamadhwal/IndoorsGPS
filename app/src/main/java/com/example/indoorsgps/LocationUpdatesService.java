package com.example.indoorsgps;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class LocationUpdatesService extends Service {

    private LocationUpdatesHelper locationUpdatesHelper;
    private NotificationHelper notificationHelper;

    public LocationUpdatesService() {
    }

    @Override
    public void onCreate() {
        // Called when we create service (only once in the lifetime of service)
        super.onCreate();
        locationUpdatesHelper = new LocationUpdatesHelper(this);
        notificationHelper = new NotificationHelper();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Called every time we start service

        String locationContent = "Getting location updates...";
        String buildingId = "-1";
        if (intent.hasExtra("buildingId"))
            buildingId = intent.getStringExtra("buildingId");
        if (intent.hasExtra("latitude") && intent.hasExtra("longitude") && intent.hasExtra("altitude"))
        {
            String latitude = intent.getStringExtra("latitude");
            String longitude = intent.getStringExtra("longitude");
            String altitude = intent.getStringExtra("altitude");

            locationContent = "Latitude : " + latitude + "\nLongitude : " + longitude + "\nAltitude : " + altitude;

            sendLocalBroadcast(latitude, longitude, altitude, buildingId);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                new Intent(this, MainActivity.class),
                0);

        Notification notification = notificationHelper.sendNotification(
                this,
                "Location Updates Service",
                locationContent,
                pendingIntent
        );

        startForeground(1, notification);

        locationUpdatesHelper.checkSettingsAndStartLocationUpdates(buildingId);

        if (intent.getAction() != null && intent.getAction().equals("STOP_FOREGROUND_SERVICE")) {
            sendLocalBroadcast("0", "0", "0", "-1");
            locationUpdatesHelper.stopLocationUpdates();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        return START_STICKY;
    }

    private void sendLocalBroadcast(String latitude, String longitude, String altitude, String buildingId) {
        Intent locIntent = new Intent("newLocationUpdate");
        locIntent.putExtra("latitude", latitude);
        locIntent.putExtra("longitude", longitude);
        locIntent.putExtra("altitude", altitude);
        locIntent.putExtra("buildingId", buildingId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(locIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
