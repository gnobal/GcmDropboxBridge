package net.gnobal.gcmdropboxbridge;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
    private static final String TAG = "MNMain";

    private DropboxAPI<AndroidAuthSession> mDBApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        AppKeyPair appKeys = new AppKeyPair(Secrets.DB_APP_KEY, Secrets.DB_APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<>(session);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();
                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                Log.i(TAG, "Got Dropbox token: " + accessToken);
				SharedPreferences.Editor e = Common.getPreferences(getApplicationContext()).edit();
				e.putString(Constants.PROPERTY_DB_ACCESS_TOKEN, accessToken);
				e.apply();
                sendBroadcast(new Intent(this, GcmRegistrationReceiver.class));
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error authenticating", e);
            }
        }
    }

   public void onClick(final View view) {
        if (view == findViewById(R.id.setup)) {
            mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
        }
    }
}