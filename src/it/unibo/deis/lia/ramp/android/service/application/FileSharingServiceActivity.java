
package it.unibo.deis.lia.ramp.android.service.application;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import it.unibo.deis.lia.ramp.RampEntryPoint;
import it.unibo.deis.lia.ramp.android.R;
import it.unibo.deis.lia.ramp.android.RampLocalService;
import it.unibo.deis.lia.ramp.android.util.Util;
import it.unibo.deis.lia.ramp.core.e2e.GenericPacket;
import it.unibo.deis.lia.ramp.service.application.ChatCommunicationSupport;
import it.unibo.deis.lia.ramp.service.application.ChatServiceRequest;
import it.unibo.deis.lia.ramp.service.application.FileSharingClient;
import it.unibo.deis.lia.ramp.service.application.FileSharingService;

import java.lang.reflect.Method;

/**
 *
 * @author Carlo Giannelli
 */
public class FileSharingServiceActivity extends Activity implements OnCheckedChangeListener, OnClickListener {
	
	private ServiceConnection sc = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			System.out.println("FileSharingServiceActivity: onServiceConnected");
			ramp = ((RampLocalService.RAMPAndroidServiceBinder) service).getRampEntryPoint();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			System.out.println("FileSharingServiceActivity: onServiceDisconnected");
			ramp = null;
		}
	};
	
	private RampEntryPoint ramp = null;
	private static FileSharingServiceActivity filesharingServiceInstance = null;
	
	public static FileSharingServiceActivity getInstance() {
		return filesharingServiceInstance;
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        System.out.println("FileSharingServiceActivity: onCreate");
		filesharingServiceInstance = FileSharingServiceActivity.this;
        super.onCreate(icicle);

        setContentView(R.layout.file_sharing_service);

        CheckBox fssActive = (CheckBox)findViewById(R.id.fileSharingServiceActive);
        fssActive.setOnCheckedChangeListener(this);
        
        findViewById(R.id.backToManager).setOnClickListener(this);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	bindService(new Intent(this, RampLocalService.class), sc, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	unbindService(sc);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch(buttonView.getId()){
            case R.id.fileSharingServiceActive:
                System.out.println("FileSharingServiceActivity: onCheckedChanged = R.id.fileSharingServiceActive "+isChecked);
                if( isChecked && !FileSharingService.isActive() ){
                    System.out.println("FileSharingServiceActivity: onCheckedChanged = activating...");
                    if( ramp != null ){
                    	ramp.startService("FileSharingService");
                    }
                    else{
                        CheckBox fssActive = (CheckBox)findViewById(R.id.fileSharingServiceActive);
                        fssActive.setChecked(FileSharingService.isActive());
                        Util.showShortToast(this, "Activate RAMP via the manager!");
                    }
                }
                else if(!isChecked && FileSharingService.isActive()){
                    try{
                        System.out.println("FileSharingServiceActivity: onCheckedChanged = deactivating...");
                        Class<?> c = Class.forName("it.unibo.deis.lia.ramp.service.application.FileSharingService");
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
		PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

		String contentText = text;

		// Build notification
		Notification notification = new Notification();
		notification.icon = R.drawable.ramp_logo;
		notification.tickerText = contentTitle +":" +contentText;
		notification.defaults = Notification.DEFAULT_ALL;
		//notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, PendingIntent.getActivity(getApplicationContext(), 0, intent, 0));
		notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, pIntent);
        
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// hide the notification after its selected
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(0, notification);
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
        System.out.println("FileSharingServiceActivity: FileSharingService.isActive() = " + FileSharingService.isActive());
        fssActive.setChecked(FileSharingService.isActive());
    }

    @Override
    protected void onDestroy() {
        System.out.println("FileSharingServiceActivity: onDestroy, isFinishing = " + this.isFinishing());
        super.onDestroy();
    }
}
