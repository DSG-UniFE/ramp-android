package it.unife.dsg.ramp_android.service.application;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import it.unife.dsg.ramp_android.R;
import it.unife.dsg.ramp_android.RampManagerActivity;
import it.unife.dsg.ramp_android.util.Constants;


public class MessengerService extends Service {
    /** Command to the service to display a message */
    public static final int MSG_SAY_HELLO = 0;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    static private final int ActiveNotificationID = R.string.wifiopp_notification;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_TYPE_DEACTIVATE:
                    //Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
                    String data = msg.getData().getString("data");
                    showNotification("WifiOpp is over", "WifiOpp is over " + data);
                    break;
                case Constants.MESSAGE_TYPE_ACTIVATE:
                    showNotification("WifiOpp is running", "WifiOpp is running");
                    break;
                case Constants.MESSAGE_TYPE_CHANGE:
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }

    /**
     * Show a notification while thread is running.
     */
    private void showNotification(String ticker, String text) {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, RampManagerActivity.class), 0);
        // initialize the Notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ramp_logo)
                        .setContentTitle("RAMP")
                        .setTicker(ticker)
                        .setWhen(System.currentTimeMillis()).setContentText(text)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ActiveNotificationID, notificationBuilder.build());
    }
}