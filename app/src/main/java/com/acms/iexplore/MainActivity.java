package com.acms.iexplore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView latitudeView;
    private TextView longitudeView;
    private TextView altitudeView;
    private TextView buildingIdView;
    private Button signOutButton;

    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;

    private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            updateUI(intent.getDoubleExtra("latitude", 0),
                    intent.getDoubleExtra("longitude", 0),
                    intent.getDoubleExtra("altitude", 0),
                    intent.getStringExtra("buildingId"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeView = findViewById(R.id.latitude);
        longitudeView = findViewById(R.id.longitude);
        altitudeView = findViewById(R.id.altitude);
        buildingIdView = findViewById(R.id.buildingId);

        signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        // Register a local broadcast manager to receive location updates
        // Receive intents with actions named "newLocationUpdate"
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(locationUpdateReceiver, new IntentFilter("newLocationUpdate"));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);
        super.onDestroy();
    }

    private void updateUI(double latitude, double longitude, double altitude, String buildingId) {
        latitudeView.setText("Latitude : " + latitude);
        longitudeView.setText("Longitude : " + longitude);
        altitudeView.setText("Altitude : " + altitude);
        buildingIdView.setText("Building ID : " + buildingId);
    }

    private void signOut() {
        removeGeofences();
        stopLocationUpdatesService();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient (MainActivity.this, GoogleSignInOptionsInstance.getGoogleSignInOptionsInstance(this));
        googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }
    private void removeGeofences() {
        geofencingClient.removeGeofences(geofenceHelper.getPendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Geofences removed...");
                        Toast.makeText(getApplicationContext(), "Geofences removed...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to remove geofences...");
                        Toast.makeText(getApplicationContext(), "FAiled to remove geofences...", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void stopLocationUpdatesService() {
        Intent locationUpdatesServiceIntent = new Intent(MainActivity.this, LocationUpdatesService.class);
        locationUpdatesServiceIntent.setAction("STOP_FOREGROUND_SERVICE");
        ContextCompat.startForegroundService(MainActivity.this, locationUpdatesServiceIntent);
    }
}
