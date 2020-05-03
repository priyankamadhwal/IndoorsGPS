package com.example.indoorsgps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

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

import java.util.HashMap;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class LocationUpdatesHelper {

    private final String TAG = "LocationUpdatesHelper";

    private Context context;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;


    private  static String id;
    private static String uniqueID = null;
    private static  String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private String latitude = "";
    private String longitude = "";
    private String altitude = "";
    private String buildingId;

    LocationUpdatesHelper(Context context) {
        this.context = context;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        id = getId(context);
    }

    private synchronized static String getId(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPreferences.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.apply();
            }
        }
        return uniqueID;
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {

                if (!(buildingId.equals("-1"))) {
                    latitude = Double.toString(location.getLatitude());
                    longitude = Double.toString(location.getLongitude());
                    altitude = Double.toString(location.getAltitude());

                    // Start location updates service
                    ContextCompat.startForegroundService(context, getLocationUpdatesServiceIntent());
                }
                else {
                    latitude = "0";
                    longitude = "0";
                    altitude = "0";
                }

                updateLocationInDB();

            }
        }
    };

    private Intent getLocationUpdatesServiceIntent() {
        Intent locationUpdatesServiceIntent = new Intent(context, LocationUpdatesService.class);
        locationUpdatesServiceIntent.putExtra("latitude", latitude);
        locationUpdatesServiceIntent.putExtra("longitude", longitude);
        locationUpdatesServiceIntent.putExtra("altitude", altitude);
        locationUpdatesServiceIntent.putExtra("buildingId", buildingId);
        return locationUpdatesServiceIntent;
    }

    private void updateLocationInDB() {
        Retrofit retrofit = RetrofitClientInstance.getRetrofitInstance();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        HashMap<String, String> map = new HashMap<>();

        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("altitude", altitude);
        map.put("buildingId", buildingId);

        Call<Void> call = retrofitInterface.executeUpdateLocation(id, map);

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
