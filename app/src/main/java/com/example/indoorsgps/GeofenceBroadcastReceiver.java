package com.example.indoorsgps;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiv";
    private final String ACTION_START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE";
    private final String ACTION_STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_SHORT).show();

        NotificationHelper notificationHelper = new NotificationHelper();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();

        for (Geofence geofence : geofenceList) {
            Log.d(TAG, "onReceive : " + geofence.getRequestId());
        }

        // Get trigerring location
        //Location location = geofencingEvent.getTriggeringLocation();

        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                // Start notification service
                startLocationUpdatesService(context);
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                // Stop notification service
                stopLocationUpdatesService(context);
                break;
        }

    }

    public void startLocationUpdatesService(Context context) {
        Intent locationUpdatesServiceIntent = new Intent(context, LocationUpdatesService.class);
        ContextCompat.startForegroundService(context, locationUpdatesServiceIntent); // starts service when the app is in background service
    }

    public void stopLocationUpdatesService(Context context) {
        Intent locationUpdatesServiceIntent = new Intent(context, LocationUpdatesService.class);
        locationUpdatesServiceIntent.setAction(ACTION_STOP_FOREGROUND_SERVICE);
        ContextCompat.startForegroundService(context, locationUpdatesServiceIntent);
    }
}
