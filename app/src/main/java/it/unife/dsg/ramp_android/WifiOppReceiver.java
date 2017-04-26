package it.unife.dsg.ramp_android;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class WifiOppReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO: React to the Intent Broadcast received.
        System.out.println("new Intent Received ");
        System.out.println("Intent Action:" + intent.getAction());

        if (intent.getData() != null)
            System.out.println("Intent Data:" + intent.getData().toString());

        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                System.out.println("Intent Extra key=" + key + ":" + extras.get(key));
                showNotification(context, "Pippo", "Pluto");
            }
        }

    }

    /**
     * Show a notification while thread is running.
     */
    private void showNotification(Context context, String ticker, String text) {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context.getApplicationContext(), RampManagerActivity.class), 0);
        // initialize the Notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ramp_logo)
                        .setContentTitle("RAMP")
                        .setTicker(ticker)
                        .setWhen(System.currentTimeMillis()).setContentText(text)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(13, notificationBuilder.build());
    }
}