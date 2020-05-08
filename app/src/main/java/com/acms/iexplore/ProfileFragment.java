package com.acms.iexplore;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private final String TAG = "ProfileFragment";

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Update profile

        try {

            TextView userIdView = root.findViewById(R.id.userIdView);
            userIdView.setText("ID: " + GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext()).getId());

            ImageView userProfileImageView = root.findViewById(R.id.userProfileImageView);
            String userProfileImageUrl = GoogleSignIn.getLastSignedInAccount(getContext()).getPhotoUrl().toString();
            userProfileImageUrl = userProfileImageUrl.replace("s96-c", "s400-c");
            Picasso.get().load(userProfileImageUrl)
                    .placeholder(R.drawable.profile_picture_placeholder)
                    .error(R.drawable.profile_picture_placeholder)
                    .into(userProfileImageView);

            TextView userDisplayNameView = root.findViewById(R.id.userDisplayNameView);
            userDisplayNameView.setText(GoogleSignIn.getLastSignedInAccount(getContext()).getDisplayName());

            TextView userEmailView = root.findViewById(R.id.userEmailView);
            userEmailView.setText(GoogleSignIn.getLastSignedInAccount(getContext()).getEmail());
        }
        catch (Exception e) {
            Log.d(TAG, "Error in getting account info");
        }

        return root;
    }
}
