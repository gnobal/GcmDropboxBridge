package net.gnobal.gcmdropboxbridge;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class GcmWakefulMessageService extends IntentService {
    public GcmWakefulMessageService() {
        super("GcmWakefulMessageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        Bundle extras = intent.getExtras();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setShowWhen(true)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setContentText(extras.getString("message"));

        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifyMgr.cancel(R.string.app_name);
        notifyMgr.notify(R.string.app_name, builder.build());

        GcmWakefulMessageReceiver.completeWakefulIntent(intent);
    }
}
