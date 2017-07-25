
package it.unife.dsg.ramp_android.service.application;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import it.unibo.deis.lia.ramp.RampEntryPoint;
import it.unife.dsg.ramp_android.R;
import it.unife.dsg.ramp_android.helper.RampLocalService;
import it.unife.dsg.ramp_android.util.Util;
import it.unibo.deis.lia.ramp.service.application.FileSharingService;

import java.lang.reflect.Method;

/**
 *
 * @author Carlo Giannelli
 */
public class FileSharingServiceActivity extends AppCompatActivity implements
        OnCheckedChangeListener, OnClickListener {
	
	private ServiceConnection sc = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			System.out.println("FileSharingServiceActivity: onServiceConnected()");
			ramp = ((RampLocalService.RAMPAndroidServiceBinder) service).getRampEntryPoint();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			System.out.println("FileSharingServiceActivity: onServiceDisconnected()");
			ramp = null;
		}
	};

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    static private final int ActiveNotificationID = R.string.ramp_active_notification;

	private RampEntryPoint ramp = null;
	private static FileSharingServiceActivity filesharingServiceInstance = null;
	
	public static FileSharingServiceActivity getInstance() {
		return filesharingServiceInstance;
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        System.out.println("FileSharingServiceActivity: onCreate()");
        super.onCreate(icicle);

        filesharingServiceInstance = FileSharingServiceActivity.this;

        setContentView(R.layout.file_sharing_service);

        CheckBox fssActive = (CheckBox)findViewById(R.id.fileSharingServiceActive);
        fssActive.setOnCheckedChangeListener(this);
        
        findViewById(R.id.backToManager).setOnClickListener(this);
    }
    
    @Override
    public void onStart() {
        System.out.println("FileSharingServiceActivity: onStart()");
        super.onStart();
        if (RampEntryPoint.isActive()) {
            bindService(new Intent(this, RampLocalService.class), sc, Context.BIND_AUTO_CREATE);
        }
    }
    
    @Override
    public void onStop() {
        System.out.println("FileSharingServiceActivity: onStop()");
    	super.onStop();
        if (ramp != null) {
            unbindService(sc);
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch(buttonView.getId()){
            case R.id.fileSharingServiceActive:
                System.out.println("FileSharingServiceActivity: onCheckedChanged = " +
                        "R.id.fileSharingServiceActive " + isChecked);
                System.out.println("FileSharingServiceActivity: FileSharingService.isActive() = " +
                        FileSharingService.isActive());
                if (isChecked && !FileSharingService.isActive()) {
                    System.out.println("FileSharingServiceActivity: onCheckedChanged = " +
                            "activating...");
                    if (ramp != null) {
                    	ramp.startService("FileSharingService");
                    } else {
                        CheckBox fssActive = (CheckBox)findViewById(R.id.fileSharingServiceActive);
                        fssActive.setChecked(FileSharingService.isActive());
                        Util.showShortToast(this, "Activate RAMP via the manager!");
                    }
                }
                else if(!isChecked && FileSharingService.isActive()){
                    try{
                        System.out.println("FileSharingServiceActivity: onCheckedChanged = " +
                                "deactivating...");
                        Class<?> c = Class.forName("it.unibo.deis.lia.ramp.service.application." +
                                "FileSharingService");
                        Method mI = c.getMethod("getInstance");
                        Method mS = c.getMethod("stopService");
                        mS.invoke(mI.invoke(null, new Object[]{}), new Object[]{});
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        System.out.println("FileSharingServiceActivity stop: FileSharingService");
                    }
                }
                break;
        }
    }
    
    //Stefano Lanzone
    public void createNotification(String text)
	{
		String contentTitle = "File Sharing Service";

		// Prepare intent which is triggered if the
		// notification is selected
		Intent intent = new Intent(this, FileSharingClientActivity.class);
//		intent.putExtra("user", user);
		PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(),
                intent, 0);

		String contentText = text;

        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setTicker(contentTitle + ":" + contentText)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ramp_logo)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        notificationBuilder.setContentIntent(pIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Because the ID remains unchanged, the existing notification is updated.
        notificationManager.notify(ActiveNotificationID, notificationBuilder.build());

		// Build notification
		//Notification notification = new Notification();
        //notification.icon = R.drawable.ramp_logo;
        //notification.tickerText = contentTitle +":" +contentText;
        //notification.defaults = Notification.DEFAULT_ALL;
        //notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, pIntent);

        //NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// hide the notification after its selected
        //notification.flags |= Notification.FLAG_AUTO_CANCEL;

        //notificationManager.notify(0, notification);
	}

    @Override
    public void onBackPressed() {
        System.out.println("FileSharingServiceActivity: onBackPressed");
        super.onBackPressed();
    }

    public void onClick(View view) {
    	switch(view.getId()){
	    	case R.id.backToManager:
	    		onBackPressed();
	    	}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
    
	@Override
	public  boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.menuBack:
		    	onBackPressed();
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}

    @Override
    protected void onResume() {
        System.out.println("FileSharingServiceActivity: onResume");
        super.onResume();
        CheckBox fssActive = (CheckBox)findViewById(R.id.fileSharingServiceActive);
        System.out.println("FileSharingServiceActivity: FileSharingService.isActive() = " +
                FileSharingService.isActive());
        fssActive.setChecked(FileSharingService.isActive());
    }

    @Override
    protected void onDestroy() {
        System.out.println("FileSharingServiceActivity: onDestroy, isFinishing = " + this.isFinishing());
        super.onDestroy();
    }
}
