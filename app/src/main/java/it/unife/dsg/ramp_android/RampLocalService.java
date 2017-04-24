
package it.unife.dsg.ramp_android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;

import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;

import it.unibo.deis.lia.ramp.RampEntryPoint;


/**
 *
 * @author Carlo Giannelli
 */
public class RampLocalService extends Service {

    private static RampEntryPoint ramp = null;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    static private final int RampActiveNotification = it.unife.dsg.ramp_android.R.string.ramp_active_notification;
    //static private final int RampStoppedNotification = 2;

    public class RAMPAndroidServiceBinder extends Binder{
        public RampEntryPoint getRampEntryPoint() {
        	System.out.println("RampLocalService: getRampEntryPoint");
        	return ramp;
        }
    }

    private final IBinder serviceBinder = new RAMPAndroidServiceBinder();

    @Override
    public void onCreate() {
        System.out.println("RampLocalService: onCreate");
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
                        .setSmallIcon(it.unife.dsg.ramp_android.R.drawable.ramp_logo)
                        .setContentTitle("RAMP")
                        .setTicker("RAMP started")
                        .setWhen(System.currentTimeMillis()).setContentText("RAMP is running")
                        .setContentIntent(contentIntent)
                        .setOngoing(true);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // notifyID allows you to update the notification later on.
        notificationManager.notify(RampActiveNotification, notificationBuilder.build());

        startForeground(RampActiveNotification, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("RampLocalService: onStartCommand");
        //super.onStart(intent, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        System.out.println("RampLocalService: onDestroy");
        super.onDestroy();
        
        ramp.stopRamp();
        ramp = null;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        System.out.println("RampLocalService: onBind");
        return serviceBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        System.out.println("RampLocalService: onRebind");
        super.onRebind(intent);
    }

//    @Override
//    public void onStart(Intent intent, int startId) {
//        System.out.println("RampLocalService: onStart");
//        super.onStart(intent, startId);
//    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("RampLocalService: onUnbind");
        return super.onUnbind(intent);
    }

}
