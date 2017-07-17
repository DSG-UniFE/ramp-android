package it.unife.dsg.ramp_android;

//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.Date;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.facebook.android.*;
//import com.facebook.android.Facebook.*;

import java.util.Vector;

import it.unibo.deis.lia.ramp.RampEntryPoint;
//import it.unibo.deis.lia.ramp.core.ern.SecureJoinEntrypoint;
//import it.unibo.deis.lia.ramp.core.social.SocialObserver;
//import it.unibo.deis.lia.ramp.core.social.SocialObserverFacebook;
//import it.unibo.deis.lia.ramp.service.upnp.UpnpProxyEntrypoint;
import it.unibo.deis.lia.ramp.core.e2e.GenericPacket;
import it.unibo.deis.lia.ramp.core.internode.Resolver;
import it.unibo.deis.lia.ramp.core.internode.ResolverPath;
import it.unife.dsg.ramp_android.helper.RampLocalService;
import it.unife.dsg.ramp_android.util.Util;

import it.unibo.deis.lia.ramp.core.e2e.E2EComm;

/**
 *
 * @author Carlo Giannelli
 */
public class RampManagerActivity extends AppCompatActivity implements OnClickListener, OnCheckedChangeListener {
    
    private static RampEntryPoint ramp = null;
    private static RampManagerActivity managerActivity=null;
    private Handler uiHandler;

//    private Facebook facebook;
//    private SocialObserverFacebook socialObserverFacebook;
    
    private ServiceConnection sc = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("RampManagerActivity: onServiceConnected()");
            String text = null;

            ramp = ((RampLocalService.RAMPAndroidServiceBinder) service).getRampEntryPoint();
            ((CheckBox) findViewById(R.id.rampActive)).setChecked(true);
            
            // setup the handler for UI interaction from other threads
            HandlerThread uiThread = new HandlerThread("UIHandler");
            uiThread.start();
            uiHandler = new UIHandler(uiThread.getLooper(), getApplicationContext());
            RampEntryPoint.setAndroidUIHandler(uiHandler);

            text = getString(R.string.node_id) + " " + ramp.getNodeIdString();
            ((TextView)findViewById(R.id.nodeId)).setText(text);

            String[] neighbors = ramp.getCurrentNeighbors();
            text = "Neighbors:\n";
            for(int i=0; i<neighbors.length; i++){
                text += neighbors[i];
                if(i<neighbors.length-1){
                    text += "\n";
                }
            }
            TextView area = (TextView)findViewById(R.id.neighborsArea);
            area.setText(text);
        }
        
        public void onServiceDisconnected(ComponentName arg0) {
            System.out.println("RampManagerActivity: onServiceDisconnected()");
            ramp = null;
            ((CheckBox) findViewById(R.id.rampActive)).setChecked(false);
        }
    };

    //private MulticastLock wifiMulticastLock;
    
    public synchronized static RampManagerActivity getInstance(){
	   return managerActivity;
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        System.out.println("RAMPManagerActivity: onCreate()");
        super.onCreate(icicle);
        managerActivity = RampManagerActivity.this;
        setContentView(R.layout.ramp_manager);

        Spinner spinnerServices = (Spinner) findViewById(R.id.serviceSpinner);
        String[] services = new String[]{"FileSharingService", "ChatService"};
        Util.populateSpinner(this, spinnerServices, services);

        Spinner spinnerClients = (Spinner) findViewById(R.id.clientSpinner);
        String[] clients = new String[]{"FileSharingClient", "BroadcastClient"};
        Util.populateSpinner(this, spinnerClients, clients);

        findViewById(R.id.goToClient).setOnClickListener(this);
        findViewById(R.id.goToService).setOnClickListener(this);
        findViewById(R.id.refreshNeighbors).setOnClickListener(this);
//        findViewById(R.id.setNodeId).setOnClickListener(this);
//        findViewById(R.id.connectToEsmn).setOnClickListener(this);
//        findViewById(R.id.loginWithFacebookButton).setOnClickListener(this);
//        findViewById(R.id.logoutFacebook).setOnClickListener(this);
        findViewById(R.id.opportunisticNetworkingManagerButton).setOnClickListener(this);
        findViewById(R.id.sendMessageBench).setOnClickListener(this);

        ((TextView) findViewById(R.id.buildTime)).setText("build time = " + RampEntryPoint.releaseDate);

        CheckBox rampActive = (CheckBox) findViewById(R.id.rampActive);
        rampActive.setOnCheckedChangeListener(this);

//        CheckBox upnpActive = (CheckBox)findViewById(R.id.upnpActive);
//        upnpActive.setOnCheckedChangeListener(this);
//        CheckBox socialConnect = (CheckBox)findViewById(R.id.socialConnect);
//        socialConnect.setOnCheckedChangeListener(this);

        //Stefano Lanzone
        CheckBox cmConnect = (CheckBox) findViewById(R.id.cmConnect);
        cmConnect.setOnCheckedChangeListener(this);

        // hide soft keyboard at activity start-up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void onClick(View view) {
        // CheckBox socialConnect;
    	if( ! RampEntryPoint.isActive() ) {
            Toast.makeText(getApplicationContext(), "Before start Continuity Manager you have to " +
                    "activate RAMP!", Toast.LENGTH_LONG).show();
    		Util.showLongToast(getApplicationContext(), "Before start Continuity Manager you " +
                    "have to activate RAMP!");
        } else {
        	switch(view.getId()){
            case R.id.goToClient:
                System.out.println("RAMPManagerActivity: onClick = R.id.goToClient");

                // start client
                Spinner spinner = (Spinner)findViewById(R.id.clientSpinner);
                
                // start Activity for this client
                try{
                    Class<?> activityClass = Class.forName("it.unife.dsg.ramp_android.service.application." +
                            spinner.getSelectedItem().toString() + "Activity");
                    Intent clientIntent = new Intent(
                        RampManagerActivity.this,
                        activityClass
                    );
                    startActivity(clientIntent);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.goToService:
                System.out.println("RAMPManagerActivity: onClick = R.id.goToService");
                spinner = (Spinner)findViewById(R.id.serviceSpinner);

                // start Activity for this service
                try {
                    Class<?> activityClass = Class.forName("it.unife.dsg.ramp_android.service.application." +
                            spinner.getSelectedItem().toString() + "Activity");
                    Intent clientIntent = new Intent(
                        RampManagerActivity.this,
                        activityClass
                    );
                    startActivity(clientIntent);
                } catch(Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.refreshNeighbors:
                System.out.println("RAMPManagerActivity: onClick = R.id.refreshNeighbors");
                ramp.forceNeighborsUpdate();
                String[] neighbors = ramp.getCurrentNeighbors();
                String text = "Neighbors:\n";
                for(int i=0; i<neighbors.length; i++){
                    text += neighbors[i];
                    if(i<neighbors.length-1){
                        text += "\n";
                    }
                }
                TextView area = (TextView)findViewById(R.id.neighborsArea);
                area.setText(text);
                break;
            case R.id.opportunisticNetworkingManagerButton:
            	//Stefano Lanzone
            	System.out.println("RAMPManagerActivity: onClick = R.id.opportunisticNetworkingManagerButton");
            	CheckBox cmConnect = (CheckBox)findViewById(R.id.cmConnect);
            	
        		if( ! cmConnect.isChecked() ){
        			Util.showShortToast(this, "Activate Continuity Manager!");
                }
        		else{
        			//start activity Opportunistic Networking Manager
        			try{
                        Class<?> activityClass = Class.forName("it.unife.dsg." +
                                "ramp_android.service.application.OpportunisticNetworkingManagerActivity");
                        Intent clientIntent = new Intent(
                            RampManagerActivity.this,
                            activityClass
                        );
                        startActivity(clientIntent);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
        		}
            	break;

            case R.id.sendMessageBench:
                System.out.println("RAMPManagerActivity: onClick = R.id.sendMessageBench");

                if (RampEntryPoint.isActive()) {
                    System.out.println("RAMPManagerActivity: onClick = R.id.sendMessageBench TRUE");
                    try {
                        Integer nodeId = Integer.parseInt(((EditText) findViewById(R.id.idBench)).getText().toString());
                        int port =  Integer.parseInt(((EditText) findViewById(R.id.portBench)).getText().toString());
                        Vector<ResolverPath> paths = Resolver.getInstance(true).resolveBlocking(nodeId, 5*1000);
                        E2EComm.sendUnicast(paths.firstElement().getPath(),
                                nodeId,
                                port,
                                E2EComm.TCP,
                                false,
                                GenericPacket.UNUSED_FIELD,
                                E2EComm.DEFAULT_BUFFERSIZE,
                                5, // timeWait
                                60, // expiry
                                GenericPacket.UNUSED_FIELD,
                                E2EComm.serialize("")
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("RAMPManagerActivity: onClick = R.id.sendMessageBench FALSE");
                    Toast.makeText(getApplicationContext(), "Before send message you have to " +
                            "activate RAMP!", Toast.LENGTH_LONG).show();
                }

                break;
            /*case R.id.connectToEsmn:
            	
			     LayoutInflater factory = LayoutInflater.from(this);
			     final View textEntryView = factory.inflate(R.layout.ramp_internet_node, null);
			     
			     AlertDialog.Builder alert = new AlertDialog.Builder(this);                 
			     alert.setTitle("Connect to");  
			     alert.setView(textEntryView);
			     
			     alert.setPositiveButton("Connect", new DialogInterface.OnClickListener() { 
			    	 public void onClick(DialogInterface dialog, int whichButton) {
    			         EditText usernameEditText = (EditText) textEntryView.findViewById(R.id.txt_username);
    			    	 EditText passwordEditText = (EditText) textEntryView.findViewById(R.id.txt_password);
    			    	 EditText ernEditText = (EditText) textEntryView.findViewById(R.id.txt_ern);
    			    	 EditText ipEditText = (EditText) textEntryView.findViewById(R.id.txt_ip);
		               
    			    	 // the getText() gets the current value of the text box
		                 // the toString() converts the value to String data type
    			    	 // then assigns it to a variable of type String
    			    	 String sUsername = usernameEditText.getText().toString();
    			    	 String sPassword = passwordEditText.getText().toString();
    			    	 String sErnName = ernEditText.getText().toString();
    			    	 String sIpAddress = ipEditText.getText().toString();
			         
    			    	 try{
    			    		 //boolean connected = 
			    				 SecureJoinEntrypoint.connectToErn(
    			        			 sErnName, 
    			        			 sIpAddress, 
    			        			 sUsername, 
    			        			 GeneralUtils.computeHash(sPassword) // ,
    			        			 //null,
    			        			 //null
			        			 );
    			    	 }
    			    	 catch (Exception e) {
    			        	e.printStackTrace();
							Context context = RampEntryPoint.getAndroidContext().getApplicationContext();
							CharSequence text = "RAMPManagerActivity: connect to ERN error: "+e;
							int duration = Toast.LENGTH_LONG;
							Toast.makeText(context, text, duration).show();
    			    	 }
    			    	 return;                  
    			     } // end onClick
			     });  // end setPositiveButton

			     alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			         public void onClick(DialogInterface dialog, int which) {
			             return;   
			         }
			     });
			     
	             alert.show();
	             break;*/
	             
//            case R.id.loginWithFacebookButton:
//            	socialConnect = (CheckBox)findViewById(R.id.socialConnect);
//            	if( ! socialConnect.isChecked() ){
//            		Util.showShortToast(this, "Activate social connect!");
//            	}
//            	else{
//	            	boolean forceWebLogin = ((CheckBox)findViewById(R.id.forceWebLoginFacebook)).isChecked();
//	            	System.out.println("RAMPManagerActivity: onClick = R.id.loginWithFacebookButton forceWebLogin=" + forceWebLogin);
//	            	facebook = new Facebook("304608866286651");
//	            	// FORCE_DIALOG_AUTH force the use of the native web browser to login
//	            	// Otherwise, if present, Facebook native app is used
//	            	int activityCode = forceWebLogin ? Facebook.FORCE_DIALOG_AUTH : 0; 
//	                
//	            	facebook.authorize(this, new String[]{"user_notes", "friends_notes"}, activityCode,
//	            			new DialogListener() {
//			            		@Override
//			            		public void onComplete(Bundle values) {
//			            			System.out.println("RAMPManagerActivity: facebook.authorize onComplete");
//			            			System.out.println("RAMPManagerActivity: facebook.authorize onComplete: token=" + facebook.getAccessToken() +
//			            					" expires=" + facebook.getAccessExpires());
//			
//			            			// persist token and expiration
////			            			socialObserverFacebook.persistFacebookToken(facebook.getAccessToken(), new Date(facebook.getAccessExpires()));
//			            		}
//			            		@Override
//			            		public void onFacebookError(FacebookError error) {
//			            			System.out.println("RAMPManagerActivity: facebook.authorize onFacebookError: " + error.getErrorType());
//			            		}
//			            		@Override
//			            		public void onError(DialogError e) {
//			            			System.out.println("RAMPManagerActivity: facebook.authorize onError: " + e.getMessage());
//			            		}
//			            		@Override
//			            		public void onCancel() {
//			            			System.out.println("RAMPManagerActivity: facebook.authorize onCancel");
//			            		}
//	            			}
//	            	);
//            	}
//            	break;
//            case R.id.logoutFacebook:
//            	socialConnect = (CheckBox)findViewById(R.id.socialConnect);
//            	if( ! socialConnect.isChecked() ){
//            		Util.showShortToast(this, "Activate social connect!");
//            	}
//            	else{
//	            	System.out.println("RAMPManagerActivity: onClick = R.id.logoutFacebook");
////	            	socialObserverFacebook.logoutFacebook();
//	            	showFacebookLoginButton();
//            	}
//            	break;
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
//    	facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
    @Override
    public void onBackPressed() {
        System.out.println("RAMPManagerActivity: onBackPressed()");
        // go back to the phone home display
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(i);
    }

    @Override
    protected void onDestroy() {
        System.out.println("RAMPManagerActivity: onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final int id = buttonView.getId();
        switch (id) {
            case R.id.rampActive:
                System.out.println("RAMPManagerActivity: onCheckedChanged = R.id.rampActive " + isChecked);
                if (isChecked) {
                    if (ramp == null) {
                        Intent serviceIntent = new Intent(this, RampLocalService.class);
                        startService(serviceIntent);
                        bindService(serviceIntent, sc, Context.BIND_AUTO_CREATE);
                    }
                } else {
                    if (ramp != null) {
                        //stop upnp
//                        CheckBox upnpActive = (CheckBox)findViewById(R.id.upnpActive);
//                        upnpActive.setChecked(false);

                        // stop social connect
//                        CheckBox socialConnect = (CheckBox)findViewById(R.id.socialConnect);
//                        socialConnect.setChecked(false);

                        // stop continuity manager
                        CheckBox cmConnect = (CheckBox) findViewById(R.id.cmConnect);
                        cmConnect.setChecked(false);

                        // Stop RAMP
                        ramp.stopRamp();

                        // stop RampLocalService
                        unbindService(sc);
                        Intent serviceIntent = new Intent(RampManagerActivity.this,
                                RampLocalService.class);
                        stopService(serviceIntent);

                        // restore facebook login button
//                        showFacebookLoginButton();

                        ramp = null;
                    }
                }
                break;

//            case R.id.upnpActive:
//            	System.out.println("RAMPManagerActivity: onCheckedChanged = R.id.uPnPActive "+isChecked);
//            	if(isChecked){
//            		if( ! RampEntryPoint.isActive() ){
//            			Util.showShortToast(this, "Activate RAMP!");
//                        CheckBox upnpActive = (CheckBox)findViewById(R.id.upnpActive);
//                        upnpActive.setChecked(false);
//                    }
//            		else{
//            			ramp.startUpnpProxyEntrypoint();
//            		}
//            	}
//            	else {
//            		ramp.stopUpnpProxyEntrypoint();
//            	}
//            	break;

            case R.id.cmConnect:
                System.out.println("RAMPManagerActivity: onCheckedChanged = R.id.cmConnect");
                if (isChecked) {
                    if (!RampEntryPoint.isActive()) {
                        Util.showShortToast(this, "Activate RAMP!");
                        CheckBox cmConnect = (CheckBox) findViewById(R.id.cmConnect);
                        cmConnect.setChecked(false);
                    } else {
//            			ramp.startSecureJoinEntrypoint(null); // can be null on android (never use username and pass)
//    					ramp.startSocialObserver(null, null); // as above
//            			socialObserverFacebook = SocialObserver.getInstance().getSocialObserverFacebook();
//            			checkFacebookTokenAsync();
//            			showFacebookInfo("Fetching Facebook information...", null, null);

                        ramp.startContinuityManager(); //Stefano Lanzone
                    }
                } else {
//        			ramp.stopSecureJoinEntrypoint();
//        			ramp.stopSocialObserver();

                    ramp.stopContinuityManager(); //Stefano Lanzone
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        System.out.println("RAMPManagerActivity: onPause()");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        System.out.println("RAMPManagerActivity: onRestart()");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        System.out.println("RAMPManagerActivity: onResume()");
        super.onResume();
        
//        CheckBox upnpActive = (CheckBox)findViewById(R.id.upnpActive);
        //System.out.println("RAMPManagerActivity: UpnpProxyEntrypoint.isActive() = " + UpnpProxyEntrypoint.isActive());
//        upnpActive.setChecked(UpnpProxyEntrypoint.isActive());
        
//        CheckBox socialConnect = (CheckBox)findViewById(R.id.socialConnect);
//        socialConnect.setChecked(SecureJoinEntrypoint.isActive());
    }

    @Override
    protected void onStart() {
        System.out.println("RAMPManagerActivity: onStart()");
        super.onStart();
        
        if (RampEntryPoint.isActive()) {
        	bindService(new Intent(this, RampLocalService.class), sc, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        System.out.println("RAMPManagerActivity: onStop()");
        super.onStop();
        if (ramp != null) {
        	unbindService(sc);
        }
    }
    

    private static class UIHandler extends Handler {
    	// this handler is used to popup messages, 
    	// sent from other threads, as the main app thread 
    	// (otherwise RuntimeException)
    	
    	private static final int DISPLAY_UI_TOAST = 0;
    	private Context context;

        public UIHandler(Looper looper, Context context){
            super(looper);
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
            case UIHandler.DISPLAY_UI_TOAST:
            	Util.showLongToast(context, (String) msg.obj);
                break;
            default:
                break;
            }
        }
    }
    
    
    // ------------------------
    // facebook related methods 
    // ------------------------
    
//    private void showFacebookLoginButton(){
//    	findViewById(R.id.facebookLoginLayout).setVisibility(View.VISIBLE); // show the facebook login layout
//    	findViewById(R.id.facebookInfoLayout).setVisibility(View.GONE); // hide the info layout
//    }
    
//    private void showFacebookInfo(String message, String ernManagerStringMessage, Bitmap userImage){
//    	findViewById(R.id.facebookLoginLayout).setVisibility(View.GONE); // hide the login layout
//    	
//    	TextView tv = (TextView) findViewById(R.id.facebookConnectedAs);
//    	tv.setText(message); // show text instead
//    	
//    	TextView ernTv = (TextView) findViewById(R.id.facebookERNManager);
//    	if(ernManagerStringMessage != null){
//    		ernTv.setText(ernManagerStringMessage);
//    	}else{
//    		ernTv.setText("");
//    	}
//    	
//    	ImageView iv = (ImageView) findViewById(R.id.facebookUserImage);
//    	if(userImage != null){
//    		iv.setImageBitmap(userImage);
//    	}else{
//    		iv.setImageResource(R.drawable.icon);
//    	}
//    	
//    	LinearLayout ll = (LinearLayout) findViewById(R.id.facebookInfoLayout);
//    	ll.setVisibility(View.VISIBLE);
//    }
//    
//    private void checkFacebookTokenAsync(){
//    	System.out.println("RAMPManagerActivity: checkFacebookTokenAsync");
//    	new AsyncFacebookFetcher().execute((Object) null); // check the token validity asynchronously
//    }

//    private class AsyncFacebookFetcher extends AsyncTask<Object, Boolean, Integer> {
//
//    	@Override
//		protected Integer doInBackground(Object... params) {
//    		System.out.println("RAMPManagerActivity: doInBackground");
////			if(socialObserverFacebook.isConnectedToFacebook()){ // if the token was valid it is still available -> isConnectedToFacebook() == true
////				publishProgress(true);
////			}else{
//				publishProgress(false);
////			}
//			return 0;
//		}
//		
//		@Override
//		protected void onProgressUpdate(Boolean... values) {
//			boolean success = values[0];
//			System.out.println("RAMPManagerActivity: onProgressUpdate " + success);
//			if(success){
//				// show facebook user info
////				Bitmap bitmap = null;
//				try {
////					HttpURLConnection urlConn = (HttpURLConnection) new URL(socialObserverFacebook.getFacebookUserPictureURL()).openConnection();
//					HttpURLConnection.setFollowRedirects(true);
////					bitmap = BitmapFactory.decodeStream(urlConn.getInputStream());
//				} catch (Exception e) {e.printStackTrace();}
////				showFacebookInfo("You are connected to Facebook as " + socialObserverFacebook.getFacebookUserName(), 
////								 "You have joined your ERN Manager at " + SocialObserver.getInstance().getSocialMergerPublicAddress(), 
////								 bitmap);
//			}else{
//				// show facebook login button
//				showFacebookLoginButton();
//			}
//		}
//    	
//    }
    
}
