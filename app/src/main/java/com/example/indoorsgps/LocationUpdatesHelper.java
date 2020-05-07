package com.example.indoorsgps;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class LocationUpdatesHelper {

    private final String TAG = "LocationUpdatesHelper";

    private Context context;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    private String id;
    private String name;

    private double latitude;
    private double longitude;
    private double altitude;
    private String buildingId;

    private NotificationHelper notificationHelper;

    LocationUpdatesHelper(Context context) {
        this.context = context;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        id = GoogleSignIn.getLastSignedInAccount(context).getId();
        name = GoogleSignIn.getLastSignedInAccount(context).getDisplayName();

        notificationHelper = new NotificationHelper();
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            onNewLocation(locationResult.getLastLocation());
        }
    };

    private void onNewLocation(Location location) {
        if (!(buildingId.equals("-1"))) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();

            // Update notification
            notificationHelper.updateNotification(context, latitude, longitude, altitude);
        }
        else {
            latitude = 0;
            longitude = 0;
            altitude = 0;
            stopLocationUpdates();
        }

        // Update location in DB
        updateLocationInDB();

        // Send local broadcast
        sendLocalBroadcast();
    }

    private void updateLocationInDB() {
        Retrofit retrofit = RetrofitClientInstance.getRetrofitInstance();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        UserLocationModel userLocation = new UserLocationModel(name, latitude, longitude, altitude, buildingId);

        Call<Void> call = retrofitInterface.executeUpdateLocation(id, userLocation);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Log.d(TAG, "Location successfully saved in db...");
                } else if (response.code() == 400) {
                    Log.d(TAG, "Failed to save location in db...");
                }

            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d(TAG, "Failed to send location to server..." + t.getMessage());
            }
        });
    }

    private void sendLocalBroadcast() {
        Intent locIntent = new Intent("newLocationUpdate");
        locIntent.putExtra("latitude", latitude);
        locIntent.putExtra("longitude", longitude);
        locIntent.putExtra("altitude", altitude);
        locIntent.putExtra("buildingId", buildingId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(locIntent);
    }

    void checkSettingsAndStartLocationUpdates(final String buildingId) {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();
        SettingsClient client = LocationServices.getSettingsClient(context);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // Settings of device are satisfied and we can start location updates
                startLocationUpdates(buildingId);
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                /*
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(activity, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
                */
            }

        });
    }

    private void startLocationUpdates(String buildingId) {
        this.buildingId = buildingId;
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
