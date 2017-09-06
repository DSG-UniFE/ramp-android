package it.unife.dsg.ramp_android.helper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import it.unibo.deis.lia.ramp.util.Stats;


public class StatsService extends Service {
    private Stats statsThread = null;

    // Binder given to clients
    private final IBinder mBinder = new StatsBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class StatsBinder extends Binder {
        public StatsService getService() {
            // Return this instance of LocalService so clients can call public methods
            return StatsService.this;
        }
    }

    @Override
    public void onCreate() {
        System.out.println("StatsService: onCreate()");
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        // The PendingIntent to launch our activity if the user selects this notification
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("StatsService: onStartCommand()");
        // Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("StatsService: onBind()");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        System.out.println("StatsService: onRebind()");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        System.out.println("StatsService: onDestroy()");
        super.onDestroy();
        stopStatsThread();
        // Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("StatsService: onUnbind()");
        return super.onUnbind(intent);
    }

    public boolean statsThreadIsActive() {
        if (statsThread == null)
            return false;
        else
            return true;
    }

    public void startStatsThread(Context context, String packageName) {
        System.out.println("StatsService: startStatsThread()");
        if (statsThread == null) {
            try {
                statsThread = new Stats(context, packageName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopStatsThread() {
        if (statsThread != null) {
            statsThread.deactivate();
            statsThread = null;
        }

    }

    public long getRxAppBytes() {
        return statsThread.getRxAppBytes();
    }

    public long getTxAppBytes() { return statsThread.getTxAppBytes(); }

    public long getRxTotalBytes() { return statsThread.getRxTotalBytes(); }

    public long getTxTotalBytes() { return statsThread.getTxTotalBytes(); }

    public String getPackageName() { return statsThread.getPackageName(); }

}
