package com.acms.iexplore.googlesigninoptions;

import android.content.Context;

import com.acms.iexplore.R;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class GoogleSignInOptionsInstance {

    public static GoogleSignInOptions getGoogleSignInOptionsInstance(Context context) {

        // Request only the user's ID token, which can be used to identify the
        // user securely to your backend. This will contain the user's basic
        // profile (name, profile picture URL, etc) so you should not need to
        // make an additional call to personalize your application.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.server_client_id))
                .requestEmail()
                .build();

        return gso;
    }
}
