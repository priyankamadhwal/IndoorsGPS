package com.example.indoorsgps;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView latitudeView;
    private TextView longitudeView;
    private TextView altitudeView;
    private TextView buildingIdView;
    private Button signOutButton;

    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;

    private SharedPreferences sharedPreferences;
    private final String PREF_GEOFENCES = "PREF_GEOFENCES";

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

        sharedPreferences = this.getSharedPreferences(PREF_GEOFENCES, Context.MODE_PRIVATE);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        // Register a local broadcast manager to receive location updates
        // Receive intents with actions named "newLocationUpdate"
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(locationUpdateReceiver, new IntentFilter("newLocationUpdate"));
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermissions();
         */
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            checkBackgroundAndFineLocationPermssions();
        else
            checkFineLocationPermission();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void checkBackgroundAndFineLocationPermssions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            // Add geofences
                            addGeofencesFromDB();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    private void checkFineLocationPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // permission is granted
                        // Add geofence
                        addGeofencesFromDB();
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            // navigate user to app settings
                            showSettingsDialog();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need permissions");
        builder.setMessage("This app requires location permissions. You can grant it from app settings");
        builder.setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
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

    private void addGeofencesFromDB() {

        Retrofit retrofit = RetrofitClientInstance.getRetrofitInstance();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        Call<List<BuildingModel>> call = retrofitInterface.executeGetAllBuildingsInfo();

        call.enqueue(new Callback<List<BuildingModel>>() {
            @Override
            public void onResponse(Call<List<BuildingModel>> call, Response<List<BuildingModel>> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "Getting building info- Failure code " + response.code());
                    return;
                }

                List <BuildingModel> buildings = response.body();
                List <Geofence> geofencesList = getGeofencesList(buildings);

                if (geofencesList.size() > 0) {
                    GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofencesList);
                    PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
                    geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Toast.makeText(MainActivity.this, "Geofences added...", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onSuccess : Geofences added...");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    String errorMessage = geofenceHelper.getErrorString(e);
                                    Log.d(TAG, "onFailure : " + errorMessage);
                                }
                            });
                }
            }

            @Override
            public void onFailure(Call<List<BuildingModel>> call, Throwable t) {
                Log.d(TAG, "Failure getting building from db :" + t.getMessage());
            }
        });
    }

    private List <Geofence> getGeofencesList(List <BuildingModel> buildings) {
        List <Geofence> geofencesList = new ArrayList<Geofence>();
        for (BuildingModel building : buildings) {
            //if (!checkGeofenceExists(building.getId())) {
                Geofence geofence = geofenceHelper.getGeofence(
                        building.getId(),
                        building.getLatitude(),
                        building.getLongitude(),
                        building.getRadius(),
                        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
                );
                geofencesList.add(geofence);
                //saveGeofence(building.getId());
            //}
        }
        return geofencesList;
    }

    private boolean checkGeofenceExists(String geofenceId) {
        // Returns true if exists, else returns false
        return (sharedPreferences.getBoolean(geofenceId, false));
    }

    private void saveGeofence(String geofenceId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(geofenceId, true);
        editor.apply();
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
