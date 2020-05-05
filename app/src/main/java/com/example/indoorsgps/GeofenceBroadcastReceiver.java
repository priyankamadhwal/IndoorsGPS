package com.example.indoorsgps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
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
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                // Start location updates service

                if (isSignedIn(context))
                    startLocationUpdatesService(context, id);
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "GEOFENCE_TRANSITION_DWELL");
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(TAG, "GEOFENCE_TRANSITION_EXIT");
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                // Stop location updates service
                if (isSignedIn(context))
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

    private boolean isSignedIn(Context context) {
        return (GoogleSignIn.getLastSignedInAccount(context) != null);
    }
}
