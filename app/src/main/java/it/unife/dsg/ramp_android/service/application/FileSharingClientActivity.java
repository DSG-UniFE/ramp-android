
package it.unife.dsg.ramp_android.service.application;

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

import it.unife.dsg.ramp_android.R;
import it.unife.dsg.ramp_android.util.Util;
import it.unibo.deis.lia.ramp.core.e2e.GenericPacket;
import it.unibo.deis.lia.ramp.service.application.FileSharingClient;
import it.unibo.deis.lia.ramp.service.management.ServiceResponse;

import java.util.Vector;

/**
 *
 * @author Carlo Giannelli
 */
public class FileSharingClientActivity extends Activity  implements OnClickListener {

    private FileSharingClient fsc = null;
    private Vector<ServiceResponse> services = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("FileSharingClientActivity: onCreate savedInstanceState = " + savedInstanceState);
        super.onCreate(savedInstanceState);
        
        //setContentView(R.layout.file_sharing_client);

        //if(savedInstanceState == null){
            //System.out.println("FileSharingClientActivity.onCreate: savedInstanceState = null");

            setContentView(R.layout.file_sharing_client);

            findViewById(R.id.discoverServices).setOnClickListener(this);
            findViewById(R.id.getRemoteFileList).setOnClickListener(this);
            findViewById(R.id.getRemoteFile).setOnClickListener(this);
            findViewById(R.id.getLocalFileList).setOnClickListener(this);
            findViewById(R.id.sendLocalFile).setOnClickListener(this);
            findViewById(R.id.backToManager).setOnClickListener(this);
            
            
            if(RampEntryPoint.isActive() && RampEntryPoint.getInstance(false, null).isContinuityManagerActive())
            {	
            	(findViewById(R.id.expiryRemoteTableLayout)).setVisibility(View.VISIBLE);
            	(findViewById(R.id.expiryLocalTableLayout)).setVisibility(View.VISIBLE);
            }
            else
            {
            	(findViewById(R.id.expiryRemoteTableLayout)).setVisibility(View.GONE);
            	(findViewById(R.id.expiryLocalTableLayout)).setVisibility(View.GONE);
            }
            
            restoreActivityState();
            
            //findViewById(R.id.stopFileSharingClient).setOnClickListener(this);
        /*}
        else{
            System.out.println("FileSharingClientActivity.onCreate: savedInstanceState != null");
            this.onRestoreInstanceState(savedInstanceState);
        }*/

        // hide soft keyboard at activity start-up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void onClick(View view) {
        if( RampEntryPoint.isActive() && fsc==null ){
            fsc = FileSharingClient.getInstance();
        }
        else if( !RampEntryPoint.isActive() && fsc!=null ){
            fsc.stopClient();
            fsc = null;
        }

        if( view.getId() == R.id.backToManager){
        	onBackPressed();
        }
        else if( fsc==null ){
            Util.showShortToast(this, "Activate RAMP via the manager!");
        }
        else{
            switch(view.getId()){
                case R.id.discoverServices:
                    System.out.println("FileSharingClientActivity: onClick = R.id.discoverServices");
                    int ttl = Integer.parseInt(((EditText)findViewById(R.id.ttl)).getText().toString());
                    int timeout = Integer.parseInt(((EditText)findViewById(R.id.timeout)).getText().toString());
                    int serviceAmount = Integer.parseInt(((EditText)findViewById(R.id.serviceAmount)).getText().toString());
                    try {
                        services = fsc.findFileSharingService(ttl, timeout, serviceAmount);
                        Spinner remoteServicesSpinner = (Spinner)findViewById(R.id.remoteServices);
                        
                        Util.populateSpinner(this, remoteServicesSpinner, services);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case R.id.getRemoteFileList:
                    System.out.println("FileSharingClientActivity: onClick = R.id.getRemoteFileList");
                    Spinner servicesSpinner = (Spinner)findViewById(R.id.remoteServices);
                    try {
                        if( services!=null && services.size()>0 ){
                            String[] remoteFiles = fsc.getRemoteFileList(services.elementAt(servicesSpinner.getSelectedItemPosition()));
                            Spinner remoteFilesSpinner = (Spinner)findViewById(R.id.remoteFilesSpinner);
                            
                            Util.populateSpinner(this, remoteFilesSpinner, remoteFiles);

                            String text = "remote files:\n";
                            for(int i=0; i<remoteFiles.length; i++){
                                text += remoteFiles[i]+"\n";
                            }
                            
                            TextView remoteFileArea = (TextView)findViewById(R.id.remoteFilesText);
                            remoteFileArea.setText(text);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case R.id.getRemoteFile:
                    System.out.println("FileSharingClientActivity: onClick = R.id.getRemoteFile");
                    if(services!=null && services.size()>0){
                        Spinner remoteFilesSpinner = (Spinner)findViewById(R.id.remoteFilesSpinner);
                        Spinner spinnerServices = (Spinner)findViewById(R.id.remoteServices);
                        if(remoteFilesSpinner!=null && remoteFilesSpinner.getSelectedView()!=null){
                            String requiredRemoteFile = remoteFilesSpinner.getSelectedItem().toString();
                            System.out.println("FileSharingClientActivity: requiring "+requiredRemoteFile);
                            try {         	
                            	//Get expiry value
                            	int expiry = GenericPacket.UNUSED_FIELD;
                            	if(RampEntryPoint.isActive() && RampEntryPoint.getInstance(false, null).isContinuityManagerActive())
                                {
                            		expiry = Integer.parseInt(((EditText)findViewById(R.id.expiryValueRemoteFile)).getText().toString());
                            		System.out.println("FileSharingClientActivity: remote file request expires after "+expiry+" seconds");
                                }
                            	
                                fsc.getRemoteFile(
                                        services.elementAt(spinnerServices.getSelectedItemPosition()),
                                        requiredRemoteFile, expiry
                                );
                                System.out.println("FileSharingClientActivity: remote file "+requiredRemoteFile+" received");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Util.showShortToast(this, "Cannot get the requested remote file");
                            }
                        }
                    }
                    break;
                case R.id.getLocalFileList:
                    System.out.println("FileSharingClientActivity: onClick = R.id.getLocalFileList");
                    String[] localFileList = fsc.getLocalFileList();
                    if( localFileList == null ){
                    	localFileList = new String[0];
                    }
                    
                    Spinner localFilesSpinner = (Spinner)findViewById(R.id.localFilesSpinner);
                    Util.populateSpinner(this, localFilesSpinner, localFileList);
                    
                    String text = "local files:\n";
                    for(int i=0; i<localFileList.length; i++){
                        text += localFileList[i]+"\n";
                    }
                    TextView localFileArea = (TextView)findViewById(R.id.localFilesText);
                    localFileArea.setText(text);
                    break;
                case R.id.sendLocalFile:
                    System.out.println("FileSharingClientActivity: onClick = R.id.sendLocalFile");
                    if(services!=null && services.size()>0){
                        localFilesSpinner = (Spinner)findViewById(R.id.localFilesSpinner);
                        if(localFilesSpinner!=null && localFilesSpinner.getSelectedView()!=null){
                            Spinner spinnerServices = (Spinner)findViewById(R.id.remoteServices);
                            String sendingLocalFile = localFilesSpinner.getSelectedItem().toString();
                            System.out.println("FileSharingClientActivity: sending "+sendingLocalFile);
                            try {
                            	//Get expiry value
                            	int expiry = GenericPacket.UNUSED_FIELD;
                            	if(RampEntryPoint.isActive() && RampEntryPoint.getInstance(false, null).isContinuityManagerActive())
                                {
                            		expiry = Integer.parseInt(((EditText)findViewById(R.id.expiryValueLocalFile)).getText().toString());
                            		System.out.println("FileSharingClientActivity: send local file operation expires after "+expiry+" seconds");
                                }
                            	
                                fsc.sendLocalFile(
                                        services.elementAt(spinnerServices.getSelectedItemPosition()),
                                        sendingLocalFile, expiry
                                );
                                System.out.println("FileSharingClientActivity: local file "+sendingLocalFile+" sent");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    break;
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
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection
    	switch (item.getItemId()) {
	    	case R.id.menuBack:
	    		onBackPressed();
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }

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
		int ttl = settings.getInt("ttl", 5);
		int timeout = settings.getInt("timeout", 2500);
		int serviceAmount = settings.getInt("serviceAmount", 3);
		((EditText)findViewById(R.id.ttl)).setText(String.valueOf(ttl));
		((EditText)findViewById(R.id.timeout)).setText(String.valueOf(timeout));
		((EditText)findViewById(R.id.serviceAmount)).setText(String.valueOf(serviceAmount));
		
		// restore remote services
		services = Util.restoreRemoteServices(this, settings, (Spinner)findViewById(R.id.remoteServices));
	}

    @Override
    public void onBackPressed() {
        System.out.println("FileSharingClientActivity: onBackPressed");
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        System.out.println("FileSharingClientActivity: onDestroy, isFinishing = " + this.isFinishing());
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        System.out.println("FileSharingClientActivity: onPause, isFinishing = " + this.isFinishing());
        super.onPause();
        saveActivityState();
    }

    @Override
    protected void onRestart() {
        System.out.println("FileSharingClientActivity: onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        System.out.println("FileSharingClientActivity: onResume");
        super.onResume();
    }

    @Override
    protected void onStart() {
        System.out.println("FileSharingClientActivity: onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        System.out.println("FileSharingClientActivity: onStop");
        super.onStop();
    }


}
