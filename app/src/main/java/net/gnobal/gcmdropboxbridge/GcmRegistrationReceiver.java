package net.gnobal.gcmdropboxbridge;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmRegistrationReceiver extends WakefulBroadcastReceiver {
    public GcmRegistrationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, GcmRegistrationIntentService.class);
		Log.i("SimpleWakefulReceiver", "Starting service @ " + SystemClock.elapsedRealtime());
		startWakefulService(context, service);
    }
}
