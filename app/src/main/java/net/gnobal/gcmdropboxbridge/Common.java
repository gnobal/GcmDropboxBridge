package net.gnobal.gcmdropboxbridge;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

class Common {
    private static final String TAG = "MNCommon";

    static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    static void storeRegistrationIdInDropbox(Context context, String regId) {
        final SharedPreferences prefs = Common.getPreferences(context);
        final String dbAccessToken = prefs.getString(Constants.PROPERTY_DB_ACCESS_TOKEN, null);
        if (dbAccessToken == null) {
            Log.i(TAG, "Dropbox has not been authorized yet. Cannot store.");
            return;
        }

        AppKeyPair appKeys = new AppKeyPair(Secrets.DB_APP_KEY, Secrets.DB_APP_SECRET);
        //AccessTokenPair accessToken = new AccessTokenPair()
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        session.setOAuth2AccessToken(dbAccessToken);
        DropboxAPI<AndroidAuthSession> mDBApi = new DropboxAPI<>(session);
        final byte[] regIdBytes = regId.getBytes(StandardCharsets.UTF_8);
        try {
            mDBApi.putFileOverwrite("/regid.txt", new ByteArrayInputStream(regIdBytes),
                    regIdBytes.length, null);
        } catch (DropboxException e) {
            Log.e(TAG, "Error writing regId", e);
            return;
        }
        Log.i(TAG, "Wrote regId to Dropbox");
    }

}
