package it.unife.dsg.ramp_android.helper;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import it.unife.dsg.ramp_android.R;
import it.unife.dsg.ramp_android.RampManagerActivity;
import it.unife.dsg.ramp_android.util.Constants;

public class WifiOppReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("WifiOppReceiver:  onReceive");
        System.out.println("Intent Action:" + intent.getAction());

        if (intent.getAction().equals(Constants.WIFIOPP_INTENT_ACTION)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
//                    for (String key : extras.keySet()) {
//                        System.out.println("Intent Extra key=" + key + ":" + extras.get(key));
//                        showNotification(context, "Pippo", "Pluto");
//                    }
                System.out.println("WifiOppReceiver >>> " + extras.getInt("data"));
                switch (extras.getInt("data")) {
                    case Constants.MESSAGE_WIFIOPP_DEACTIVATE:
                        break;
                    case Constants.MESSAGE_WIFIOPP_ACTIVATE:
                        break;
                    case Constants.MESSAGE_ROLE_CHANGED:
                        sendLocalBroadcast(context, Constants.MESSAGE_ROLE_CHANGED);
                        System.out.println("WifiOppReceiver sent message " +
                                Constants.MESSAGE_ROLE_CHANGED);
                        break;
                    case Constants.MESSAGE_HOTSPOT_CHANGED:
                        sendLocalBroadcast(context, Constants.MESSAGE_HOTSPOT_CHANGED);
                        System.out.println("WifiOppReceiver sent message " +
                                Constants.MESSAGE_HOTSPOT_CHANGED);
                        break;
                }
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

    // Send an Intent with an action named "intent_type". The Intent sent should
    // be received by the Receiver.
    private void sendLocalBroadcast(Context context, int message_id) {
        Intent intent = new Intent(Constants.RAMP_INTENT_ACTION);
        intent.putExtra("data", message_id);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}