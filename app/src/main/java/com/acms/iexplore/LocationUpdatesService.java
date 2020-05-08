package com.acms.iexplore;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

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

        String buildingId = "-1";
        if (intent.hasExtra("buildingId"))
            buildingId = intent.getStringExtra("buildingId");

        startForeground(1, notificationHelper.createNotification(this, "Getting location updates..."));

        locationUpdatesHelper.checkSettingsAndStartLocationUpdates(buildingId);

        if (intent.getAction() != null && intent.getAction().equals("STOP_FOREGROUND_SERVICE")) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        return START_STICKY;
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
