package net.gnobal.gcmdropboxbridge;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import android.os.Handler;

public class GcmRegistrationIntentService extends IntentService {
    public static final String EXTRA_MESSAGE = "message";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    static final String TAG = "MNRegSrv";

    private GoogleCloudMessaging gcm;
    private AtomicInteger msgId = new AtomicInteger();
    private Context context;
    private Intent intent;
    private String regid;
    private Handler handler;

    public GcmRegistrationIntentService() {
        super("GcmRegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        handler = new Handler();
        this.intent = intent;
        context = getApplicationContext();

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

//            if (regid.isEmpty()) {
                registerInBackground();
//            } else {
//                GcmRegistrationReceiver.completeWakefulIntent(intent);
//            }
        } else {
            Log.e(TAG, "No valid Google Play Services APK found.");
            GcmRegistrationReceiver.completeWakefulIntent(intent);
        }
    }

    private class RegistrationTaskWithBackoff extends AsyncTask<Void, Void, String> {
        private static final int MAX_ATTEMPTS = 5;
        private int attempt = 0;
        private long nextDelayMillis = 1000;

        @Override
        protected String doInBackground(Void... params) {
            String msg = "";
            Log.i(TAG, "Attempting registration, attempt + " + attempt + " out of " + MAX_ATTEMPTS);
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                regid = gcm.register(Secrets.GCM_SENDER_ID);
                msg = "Device registered, registration ID=" + regid;

                // Persist the regID - no need to register again.
                storeRegistrationIdLocally(context, regid);
                Common.storeRegistrationIdInDropbox(context, regid);
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
                Log.e(TAG, msg, ex);
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
                ++attempt;
                if (attempt >= MAX_ATTEMPTS) {
                    Log.w(TAG, "This was the last attempt");
                    return msg;
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RegistrationTaskWithBackoff.this.execute(null, null, null);
                    }
                }, nextDelayMillis);
                nextDelayMillis *= 2;
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {
            Log.i(TAG, msg);
            GcmRegistrationReceiver.completeWakefulIntent(intent);
        }
    }

    private void registerInBackground() {
        new RegistrationTaskWithBackoff().execute(null, null, null);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                // TODO: convey this error to the user somehow
//                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
//                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                Log.e(TAG, "User recoverable error, resultCode=" + resultCode);
            } else {
                Log.e(TAG, "This device is not supported, resultCode=" + resultCode);
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = Common.getPreferences(context);
        String registrationId = prefs.getString(Constants.PROPERTY_GCM_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(Constants.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void storeRegistrationIdLocally(Context context, String regId) {
        final SharedPreferences prefs = Common.getPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PROPERTY_GCM_REG_ID, regId);
        editor.putInt(Constants.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
}
