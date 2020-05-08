package com.acms.iexplore.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acms.iexplore.geofence.GeofenceHelper;
import com.acms.iexplore.googlesigninoptions.GoogleSignInOptionsInstance;
import com.acms.iexplore.location.LocationUpdatesService;
import com.acms.iexplore.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        updateHeader();
        bindSignOutItem();

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_buildings, R.id.nav_profile)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (getIntent().getBooleanExtra("signed_in", false)) {
            Snackbar.make(findViewById(R.id.drawer_layout), "Signed in successfully! Geofences are added.", Snackbar.LENGTH_LONG).show();
        }
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
            Toast.makeText(this, "Error while updating nav header: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void bindSignOutItem() {
        navigationView.getMenu().findItem(R.id.nav_sign_out).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                removeGeofencesAndSignOut();
                stopLocationUpdatesService();
                return true;
            }
        });
    }

    private void removeGeofencesAndSignOut() {
        geofencingClient.removeGeofences(geofenceHelper.getPendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed successfully, sign out
                        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient (HomeActivity.this, GoogleSignInOptionsInstance.getGoogleSignInOptionsInstance(HomeActivity.this));
                        googleSignInClient.signOut().addOnCompleteListener(HomeActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // Go to sign in activity
                                Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                                intent.putExtra("signed_out", true);
                                startActivity(intent);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to remove geofences and sign out.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void stopLocationUpdatesService() {
        Intent locationUpdatesServiceIntent = new Intent(HomeActivity.this, LocationUpdatesService.class);
        locationUpdatesServiceIntent.setAction("STOP_FOREGROUND_SERVICE");
        ContextCompat.startForegroundService(HomeActivity.this, locationUpdatesServiceIntent);
    }
}
