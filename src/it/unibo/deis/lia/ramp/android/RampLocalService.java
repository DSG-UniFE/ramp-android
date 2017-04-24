
package it.unibo.deis.lia.ramp.android;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;

import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;

import it.unibo.deis.lia.ramp.RampEntryPoint;


/**
 *
 * @author Carlo Giannelli
 */
public class RampLocalService extends Service {

    private static RampEntryPoint ramp = null;

    static private final int RampActiveNotification = 1;
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
        
        // ongoing notification
        //String ns = Context.NOTIFICATION_SERVICE;
        //NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        //mNotificationManager.cancel(RampLocalService.RampStoppedNotification);

        int icon = R.drawable.ramp_logo;                // icon from resources
        CharSequence tickerText = "RAMP started";       // ticker-text
        long when = System.currentTimeMillis();         // notification time
        Context context = getApplicationContext();      // application Context
        CharSequence contentTitle = "RAMP";  			// expanded message title
        CharSequence contentText = "RAMP is running";   // expanded message text

        Intent notificationIntent = new Intent(this, RampManagerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // the next two lines initialize the Notification, using the configurations above
        Notification notification = new Notification(icon, tickerText, when);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        
        startForeground(RampLocalService.RampActiveNotification, notification);
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

    @Override
    public void onStart(Intent intent, int startId) {
        System.out.println("RampLocalService: onStart");
        super.onStart(intent, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("RampLocalService: onUnbind");
        return super.onUnbind(intent);
    }

}
