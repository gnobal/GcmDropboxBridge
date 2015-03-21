package net.gnobal.gcmdropboxbridge;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmWakefulMessageReceiver extends WakefulBroadcastReceiver {
    public GcmWakefulMessageReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        Intent service = new Intent(context, GcmWakefulMessageService.class);
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmWakefulMessageService.class.getName());
        Intent service = intent.setComponent(comp);

        // Start the service, keeping the device awake while it is launching.
        Log.i("SimpleWakefulReceiver", "Starting service @ " + SystemClock.elapsedRealtime());
        startWakefulService(context, service);
    }
}
