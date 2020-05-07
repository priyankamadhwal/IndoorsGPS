package com.example.indoorsgps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiv";

    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_SHORT).show();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();

        // In case of overlapping geofences, choose the last one in the list
        String id = "";
        for (Geofence geofence : geofenceList) {
            Log.d(TAG, "onReceive : " + geofence.getRequestId());
            id = geofence.getRequestId();
        }

        // Get trigerring location
        // Location location = geofencingEvent.getTriggeringLocation();

        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.d(TAG, "GEOFENCE_TRANSITION_ENTER");
                // Start location updates service
                startLocationUpdatesService(context, id);
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "GEOFENCE_TRANSITION_DWELL");
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(TAG, "GEOFENCE_TRANSITION_EXIT");
                // Stop location updates service
                stopLocationUpdatesService(context);
                break;
        }

    }

    private void startLocationUpdatesService(Context context, String geofenceId) {
        Intent locationUpdatesServiceIntent = new Intent(context, LocationUpdatesService.class);
        locationUpdatesServiceIntent.putExtra("buildingId", geofenceId);
        ContextCompat.startForegroundService(context, locationUpdatesServiceIntent);
    }

    private void stopLocationUpdatesService(Context context) {
        Intent locationUpdatesServiceIntent = new Intent(context, LocationUpdatesService.class);
        locationUpdatesServiceIntent.setAction("STOP_FOREGROUND_SERVICE");
        ContextCompat.startForegroundService(context, locationUpdatesServiceIntent);
    }
}
