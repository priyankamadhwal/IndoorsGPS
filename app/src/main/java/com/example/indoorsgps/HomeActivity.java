package com.example.indoorsgps;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HomeActivity extends AppCompatActivity {

    private final String TAG = "HomeActivity";
    private NavigationView navigationView;

    private AppBarConfiguration mAppBarConfiguration;

    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
         */
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        updateHeader();
        bindSignOut();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_profile)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void updateHeader() {
        View headerView = navigationView.getHeaderView(0);


        try {
            ImageView userProfileImageView = headerView.findViewById(R.id.userProfileImageView);
            String userProfileImageUrl = GoogleSignIn.getLastSignedInAccount(this).getPhotoUrl().toString();
            userProfileImageUrl = userProfileImageUrl.replace("s96-c", "s400-c");
            Picasso.get().load(userProfileImageUrl)
                    .placeholder(R.drawable.profile_picture_placeholder)
                    .error(R.drawable.profile_picture_placeholder)
                    .into(userProfileImageView);

            TextView userDisplayNameView = headerView.findViewById(R.id.userDisplayNameView);
            userDisplayNameView.setText(GoogleSignIn.getLastSignedInAccount(this).getDisplayName());

            TextView userEmailView = headerView.findViewById(R.id.userEmailView);
            userEmailView.setText(GoogleSignIn.getLastSignedInAccount(this).getEmail());
        }
        catch (Exception e) {
            Log.d(TAG, "Error in photo load");
        }
    }

    private void bindSignOut() {
        navigationView.getMenu().findItem(R.id.nav_sign_out).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                removeGeofences();
                stopLocationUpdatesService();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient (HomeActivity.this, GoogleSignInOptionsInstance.getGoogleSignInOptionsInstance(HomeActivity.this));
                googleSignInClient.signOut().addOnCompleteListener(HomeActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                        startActivity(intent);
                    }
                });
                return true;
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
        Intent locationUpdatesServiceIntent = new Intent(HomeActivity.this, LocationUpdatesService.class);
        locationUpdatesServiceIntent.setAction("STOP_FOREGROUND_SERVICE");
        ContextCompat.startForegroundService(HomeActivity.this, locationUpdatesServiceIntent);
    }
}
