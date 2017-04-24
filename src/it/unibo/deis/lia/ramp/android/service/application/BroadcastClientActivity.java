
package it.unibo.deis.lia.ramp.android.service.application;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import it.unibo.deis.lia.ramp.RampEntryPoint;

import it.unibo.deis.lia.ramp.android.R;
import it.unibo.deis.lia.ramp.android.util.Util;
import it.unibo.deis.lia.ramp.service.application.BroadcastClient;
import it.unibo.deis.lia.ramp.service.management.ServiceResponse;

import java.util.Vector;

/**
 *
 * @author Carlo Giannelli
 */
public class BroadcastClientActivity extends Activity  implements OnClickListener {

    private BroadcastClient bc = null;
    private Vector<ServiceResponse> services = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("BroadcastClientActivity: onCreate savedInstanceState = "+savedInstanceState);
        super.onCreate(savedInstanceState);
        
        //setContentView(R.layout.file_sharing_client);

        //if(savedInstanceState == null){
            //System.out.println("FileSharingClientActivity.onCreate: savedInstanceState = null");

            setContentView(R.layout.broadcast_client);

            findViewById(R.id.discoverServices).setOnClickListener(this);
            findViewById(R.id.getStreamList).setOnClickListener(this);
            findViewById(R.id.getStream).setOnClickListener(this);
            findViewById(R.id.backToManager).setOnClickListener(this);
            //findViewById(R.id.stopFileSharingClient).setOnClickListener(this);
            
            restoreActivityState();
        /*}
        else{
            System.out.println("FileSharingClientActivity.onCreate: savedInstanceState != null");
            this.onRestoreInstanceState(savedInstanceState);
        }*/

        // hide soft keyboard at activity start-up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void onClick(View view) {
        if( RampEntryPoint.isActive() && bc==null ){
            bc = BroadcastClient.getInstance();
        }
        else if( !RampEntryPoint.isActive() && bc!=null ){
            bc.stopClient();
            bc = null;
        }

        if( view.getId() == R.id.backToManager){
        	onBackPressed();
//          System.out.println("FileSharingClientActivity: onClick = R.id.backToManager");
//          Intent clientIntent = new Intent(
//              FileSharingClientActivity.this,
//              RampManagerActivity.class
//          );
//          startActivity(clientIntent);
        }
        else if( bc==null ){
            Util.showShortToast(this, "Activate RAMP via the manager!");
        }
        else{
            switch(view.getId()){
                case R.id.discoverServices:
                    System.out.println("BroadcastClientActivity: onClick = R.id.discoverServices");
                    int ttl = Integer.parseInt(((EditText)findViewById(R.id.ttl)).getText().toString());
                    int timeout = Integer.parseInt(((EditText)findViewById(R.id.timeout)).getText().toString());
                    int serviceAmount = Integer.parseInt(((EditText)findViewById(R.id.serviceAmount)).getText().toString());
                    try {
                        services = bc.findBroadcastService(ttl, timeout, serviceAmount);
                        Spinner remoteServicesSpinner = (Spinner)findViewById(R.id.remoteServices);
                        
                        Util.populateSpinner(this, remoteServicesSpinner, services);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case R.id.getStreamList:
                    System.out.println("BroadcastClientActivity: onClick = R.id.getStreamList");
                    Spinner servicesSpinner = (Spinner)findViewById(R.id.remoteServices);
                    try {
                        if( services!=null && services.size()>0 ){
                            String[] remoteStreams = bc.programList(services.elementAt(servicesSpinner.getSelectedItemPosition()));
                            Spinner remoteStreamsSpinner = (Spinner)findViewById(R.id.remoteStreamsSpinner);
                            
                            Util.populateSpinner(this, remoteStreamsSpinner, remoteStreams);

                            String text = "remote streams:\n";
                            for(int i=0; i<remoteStreams.length; i++){
                                text += remoteStreams[i]+"\n";
                            }
                            
                            TextView remoteStreamsArea = (TextView)findViewById(R.id.remoteStreamsText);
                            remoteStreamsArea.setText(text);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case R.id.getStream:
                    System.out.println("BroadcastClientActivity: onClick = getStream");
                    if(services!=null && services.size()>0){
                        Spinner remoteStreamsSpinner = (Spinner)findViewById(R.id.remoteStreamsSpinner);
                        Spinner spinnerServices = (Spinner)findViewById(R.id.remoteServices);
                        if(remoteStreamsSpinner!=null && remoteStreamsSpinner.getSelectedView()!=null){
                            String requiredStream = remoteStreamsSpinner.getSelectedItem().toString();
                            System.out.println("BroadcastClientActivity: requiring "+requiredStream);
                            try {
                                int mediaPlayerPort = bc.getProgram(
                                    services.elementAt(spinnerServices.getSelectedItemPosition()),
                                    requiredStream
                                );
                                
                                android.widget.VideoView videoView = (android.widget.VideoView) findViewById(R.id.VideoView);
                                android.widget.MediaController mediaController = new android.widget.MediaController(RampEntryPoint.getAndroidContext());
                                mediaController.setAnchorView(videoView);
                                //android.net.Uri videoUri = android.net.Uri.parse("rtsp://127.0.0.1:"+mediaPlayerPort+"/ciao.sdp");
                                //android.net.Uri videoUri = android.net.Uri.parse("rtsp://127.0.0.1:"+mediaPlayerPort+"/ramp.sdp");
                                android.net.Uri videoUri = android.net.Uri.parse("rtp://127.0.0.1:"+mediaPlayerPort);
                                videoView.setMediaController(mediaController);
                                videoView.setVideoURI(videoUri);
                                videoView.start();
                                // TODO http://forum.videolan.org/viewtopic.php?f=4&t=60335
                                
                                System.out.println("BroadcastClientActivity: remote streams "+requiredStream+" received");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    break;
                /*case R.id.getLocalFileList:
                    System.out.println("BroadcastClientActivity: onClick = R.id.getLocalFileList");
                    String[] localFileList = bc.getLocalFileList();
                    if( localFileList == null ){
                    	localFileList = new String[0];
                    }

                    ArrayAdapter<String> adapterLocalFiles = new ArrayAdapter<String>(
                        BroadcastClientActivity.this,
                        android.R.layout.simple_spinner_item,
                        localFileList
                    );
                    Spinner localFilesSpinner = (Spinner)findViewById(R.id.localFilesSpinner);
                    localFilesSpinner.setAdapter(adapterLocalFiles);

                    String text = "local files:\n";
                    for(int i=0; i<localFileList.length; i++){
                        text += localFileList[i]+"\n";
                    }
                    TextView localFileArea = (TextView)findViewById(R.id.localFilesText);
                    localFileArea.setText(text);
                    break;*/
                /*case R.id.sendLocalFile:
                    System.out.println("BroadcastClientActivity: onClick = R.id.sendLocalFile");
                    if(services!=null && services.size()>0){
                        localFilesSpinner = (Spinner)findViewById(R.id.localFilesSpinner);
                        if(localFilesSpinner!=null && localFilesSpinner.getSelectedView()!=null){
                            Spinner spinnerServices = (Spinner)findViewById(R.id.remoteServices);
                            String sendingLocalFile = localFilesSpinner.getSelectedItem().toString();
                            System.out.println("BroadcastClientActivity: sending "+sendingLocalFile);
                            try {
                                bc.sendLocalFile(
                                        services.elementAt(spinnerServices.getSelectedItemPosition()),
                                        sendingLocalFile
                                );
                                System.out.println("BroadcastClientActivity: local file "+sendingLocalFile+" sent");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    break;*/
                /*case R.id.stopBroadcastClient:
                    System.out.println("BroadcastClientActivity: onClick = R.id.stopFileSharingClient");
                    finish();
                    break;*/
            }
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
    		return true;
    		//System.out.println("FileSharingServiceActivity: onClick = R.id.backToManager");
    		//Intent clientIntent = new Intent(
    		//	FileSharingClientActivity.this,
    		//	RampManagerActivity.class
    		//);
    		//startActivity(clientIntent);
    		//return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
    /*@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println("FileSharingClientActivity.onSaveInstanceState: onSaveInstanceState = "+savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is killed and restarted.
        savedInstanceState.putSerializable("services", services);

        int ttl = Integer.parseInt(((EditText)findViewById(R.id.ttl)).getText().toString());
        savedInstanceState.putInt("ttl", ttl);
        int timeout = Integer.parseInt(((EditText)findViewById(R.id.timeout)).getText().toString());
        savedInstanceState.putInt("timeout", timeout);
        int serviceAmount = Integer.parseInt(((EditText)findViewById(R.id.serviceAmount)).getText().toString());
        savedInstanceState.putInt("serviceAmount", serviceAmount);
        
        super.onSaveInstanceState(savedInstanceState);
        System.out.println("FileSharingClientActivity.onSaveInstanceState: savedInstanceState = "+savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        System.out.println("FileSharingClientActivity.onRestoreInstanceState: savedInstanceState = "+savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        services = (Vector<ServiceResponse>)savedInstanceState.getSerializable("services");
        Spinner remoteServicesSpinner = (Spinner)findViewById(R.id.remoteServices);
        String[] servicesString = new String[services.size()];
        for(int i=0; i<services.size(); i++){
            servicesString[i] = services.elementAt(i).toString();
        }
        ArrayAdapter adapterClients = new ArrayAdapter(
                FileSharingClientActivity.this,
                android.R.layout.simple_spinner_item,
                servicesString
        );
        remoteServicesSpinner.setAdapter(adapterClients);

        int ttl = savedInstanceState.getInt("ttl");
        ((EditText)findViewById(R.id.ttl)).setText(""+ttl);
        int timeout = savedInstanceState.getInt("timeout");
        ((EditText)findViewById(R.id.timeout)).setText(""+timeout);
        int serviceAmount = savedInstanceState.getInt("serviceAmount");
        ((EditText)findViewById(R.id.serviceAmount)).setText(""+serviceAmount);
    }*/

	private void saveActivityState(){
		System.out.println("FileSharingClientActivity: saveActivityState");
		// Use shared preferences to save the activity state
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		
		// save parameters
		int ttl = Integer.parseInt(((EditText)findViewById(R.id.ttl)).getText().toString());
        int timeout = Integer.parseInt(((EditText)findViewById(R.id.timeout)).getText().toString());
        int serviceAmount = Integer.parseInt(((EditText)findViewById(R.id.serviceAmount)).getText().toString());
		editor.putInt("ttl", ttl);
		editor.putInt("timeout", timeout);
		editor.putInt("serviceAmount", serviceAmount);
		
		// save remote services (serialized as string)
		Util.saveRemoteServices(editor, services);

		// Commit
		editor.commit();
	}

	private void restoreActivityState(){
		System.out.println("FileSharingClientActivity: restoreActivityState");
		// Use shared preferences to restore the activity state
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		
		// restore parameters
		int ttl = settings.getInt("ttl", 3);
		int timeout = settings.getInt("timeout", 5000);
		int serviceAmount = settings.getInt("serviceAmount", 1);
		((EditText)findViewById(R.id.ttl)).setText(String.valueOf(ttl));
		((EditText)findViewById(R.id.timeout)).setText(String.valueOf(timeout));
		((EditText)findViewById(R.id.serviceAmount)).setText(String.valueOf(serviceAmount));
		
		// restore remote services
		services = Util.restoreRemoteServices(this, settings, (Spinner)findViewById(R.id.remoteServices));
	}
	
	@Override
    public void onBackPressed() {
        System.out.println("BroadcastClientActivity: onBackPressed");
        super.onBackPressed();
//        // in this manner the back key does not finish the activity
//        Intent clientIntent = new Intent(
//            BroadcastClientActivity.this,
//            RampManagerActivity.class
//        );
//        //startActivityForResult(clientIntent,0);
//        startActivity(clientIntent);
    }

    /*@Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        System.out.println("FileSharingClientActivity: onActivityResult, " +
                "requestCode = "+requestCode+" "+
                "resultCode = "+resultCode+" "+
                "data = "+data);
        if(requestCode==0){
            this.finish();
        }
    }*/

    @Override
    protected void onDestroy() {
        System.out.println("BroadcastClientActivity: onDestroy, isFinishing = "+this.isFinishing());
        super.onDestroy();
        saveActivityState();
    }

    @Override
    protected void onPause() {
        System.out.println("BroadcastClientActivity: onPause, isFinishing = "+this.isFinishing());
        super.onPause();
    }

    @Override
    protected void onRestart() {
        System.out.println("BroadcastClientActivity: onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        System.out.println("BroadcastClientActivity: onResume");
        super.onResume();
        bc = BroadcastClient.getInstanceNoForce();
    }

    @Override
    protected void onStart() {
        System.out.println("BroadcastClientActivity: onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        System.out.println("BroadcastClientActivity: onStop");
        super.onStop();
    }


}
