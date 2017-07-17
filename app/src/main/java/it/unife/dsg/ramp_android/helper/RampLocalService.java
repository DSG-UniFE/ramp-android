
package it.unife.dsg.ramp_android.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import it.unibo.deis.lia.ramp.RampEntryPoint;
import it.unife.dsg.ramp_android.R;
import it.unife.dsg.ramp_android.RampManagerActivity;
import it.unife.dsg.ramp_android.util.Constants;


/**
 *
 * @author Carlo Giannelli
 */
public class RampLocalService extends Service {

    private static RampEntryPoint ramp = null;
    private static String TAG = "RampLocalService";

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    static private final int ActiveNotificationID = R.string.ramp_active_notification;
    //static private final int RampStoppedNotification = 2;

    public class RAMPAndroidServiceBinder extends Binder{
        public RampEntryPoint getRampEntryPoint() {
        	System.out.println("RampLocalService: getRampEntryPoint");
        	return ramp;
        }
    }

    private final IBinder serviceBinder = new RAMPAndroidServiceBinder();

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "Constants.RAMP_INTENT_ACTION" is broadcasted.
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("RampLocalService, broadcastReceiver:  onReceive");
            System.out.println("RampLocalService, intent Action:" + intent.getAction());
            if (intent.getAction().equals(Constants.RAMP_INTENT_ACTION)) {
                // Get extra data included in the Intent
                int value = intent.getIntExtra("data", -1);
                Log.d(TAG, "broadcastReceiver, got message: " + value);
                if (value > 0)
                    ramp.sentNotifyToOpportunisticNetworkingManager();
            }
        }
    };

    @Override
    public void onCreate() {
        System.out.println("RampLocalService: onCreate()");
        super.onCreate();

        // credits: http://stackoverflow.com/questions/6343166/android-os-networkonmainthreadexception
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // create ramp
        if(ramp == null){
            ramp = RampEntryPoint.getInstance(true, getApplicationContext());
        }

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, RampManagerActivity.class), 0);
        // initialize the Notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ramp_logo)
                        .setContentTitle("RAMP")
                        .setTicker("RAMP started")
                        .setWhen(System.currentTimeMillis()).setContentText("RAMP is running")
                        .setContentIntent(contentIntent)
                        .setOngoing(true);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // notifyID allows you to update the notification later on.
        notificationManager.notify(ActiveNotificationID, notificationBuilder.build());

        startForeground(ActiveNotificationID, notificationBuilder.build());

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "Constants.RAMP_INTENT_ACTION".
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.RAMP_INTENT_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("RampLocalService: onStartCommand()");
        //super.onStart(intent, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        System.out.println("RampLocalService: onDestroy()");
        super.onDestroy();
        
        ramp.stopRamp();
        ramp = null;

        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        System.out.println("RampLocalService: onBind()");
        return serviceBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        System.out.println("RampLocalService: onRebind()");
        super.onRebind(intent);
    }

//    @Override
//    public void onStart(Intent intent, int startId) {
//        System.out.println("RampLocalService: onStart");
//        super.onStart(intent, startId);
//    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("RampLocalService: onUnbind()");
        return super.onUnbind(intent);
    }

}
