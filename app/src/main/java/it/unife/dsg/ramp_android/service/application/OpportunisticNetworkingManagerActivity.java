
package it.unife.dsg.ramp_android.service.application;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

import it.unibo.deis.lia.ramp.RampEntryPoint;

import it.unife.dsg.ramp_android.R;
import it.unife.dsg.ramp_android.util.Util;
import it.unibo.deis.lia.ramp.core.internode.OpportunisticNetworkingManager;
import it.unibo.deis.lia.ramp.core.internode.OpportunisticNetworkingManager.ReplacePackets;

/**
 *
 * @author Stefano Lanzone
 */
public class OpportunisticNetworkingManagerActivity extends AppCompatActivity implements OnClickListener, OnCheckedChangeListener {

    private OpportunisticNetworkingManager onm = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("OpportunisticNetworkingManagerActivity: onCreate savedInstanceState = " + savedInstanceState);
        super.onCreate(savedInstanceState);

        //if(savedInstanceState == null){
            //System.out.println("OpportunisticNetworkingManagerActivity.onCreate: savedInstanceState = null");

            setContentView(R.layout.opportunistic_networking_manager);

            Spinner spinnerNumberOfOneHopDestinations = (Spinner)findViewById(R.id.numberOfOneHopDestinationsSpinner);
            System.out.println("OpportunisticNetworkingManagerActivity: spinnerNumberOfOneHopDestinations = "+spinnerNumberOfOneHopDestinations);
            String[] numberOfOneHopDestinations = new String[] { "2/4/6","3/5/7","4/6/8" };
            Util.populateSpinner(this, spinnerNumberOfOneHopDestinations, numberOfOneHopDestinations);
            
            Spinner spinnerReplacePackets = (Spinner)findViewById(R.id.replacePacketsSpinner);
            System.out.println("OpportunisticNetworkingManagerActivity: spinnerReplacePackets = "+spinnerReplacePackets);
            String[] replacePackets = new String[] { ReplacePackets.OLD.toString(),ReplacePackets.SMALL.toString(),ReplacePackets.HUGE.toString()};
            Util.populateSpinner(this, spinnerReplacePackets, replacePackets);

            findViewById(R.id.backToManager).setOnClickListener(this);
            
            CheckBox persistPackets = (CheckBox)findViewById(R.id.persistPackets);
            persistPackets.setOnCheckedChangeListener(this);
            
            CheckBox removePacketAfterSend = (CheckBox)findViewById(R.id.removePacketAfterSend);
            removePacketAfterSend.setOnCheckedChangeListener(this);
            
            restoreActivityState();
            
        /*}
        else{
            System.out.println("OpportunisticNetworkingManagerActivity.onCreate: savedInstanceState != null");
            this.onRestoreInstanceState(savedInstanceState);
        }*/

        // hide soft keyboard at activity start-up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void onClick(View view) {
        checkRampState();

        if( view.getId() == R.id.backToManager){
        	onBackPressed();
        }
        else if( onm==null ){
            Util.showShortToast(this, "Activate Continuity Manager via the RAMP Manager!");
        }
        else{
            //switch ...
        }
    }

	private void checkRampState() {
		if( RampEntryPoint.isActive() && onm==null ){
        	onm = OpportunisticNetworkingManager.getInstance(true);
        }
        else if( !RampEntryPoint.isActive() && onm!=null ){
        	onm.deactivate(true);
        	onm = null;
        }
	}
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	
//    	checkRampState();

    	final int id = buttonView.getId();
    	switch(id){
    	case R.id.persistPackets:
    		System.out.println("OpportunisticNetworkingManagerActivity: onCheckedChanged = R.id.persistPackets " + isChecked);

//    			if( onm == null)
//    			{	
//    				Util.showShortToast(this, "Activate Continuity Manager via the RAMP Manager!");
//    				CheckBox persistPacketsCB = (CheckBox)findViewById(R.id.persistPackets);
//    				persistPacketsCB.setChecked(!isChecked);
//    			}
//    			else
//    			{
////    				onm.setPersistPackets(isChecked);
//    			}

    			break;
    	case R.id.removePacketAfterSend:
    		System.out.println("OpportunisticNetworkingManagerActivity: onCheckedChanged = R.id.removePacketAfterSend " + isChecked);
        	 
//    			if( onm == null)
//    			{	
//    				Util.showShortToast(this, "Activate Continuity Manager via the RAMP Manager!");
//    				CheckBox removePacketAfterSendCB = (CheckBox)findViewById(R.id.removePacketAfterSend);
//    				removePacketAfterSendCB.setChecked(!isChecked);
//    			}
//    			else
//    			{
////    				onm.setRemovePacketAfterSend(isChecked);
//    			}
    			break;
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
		System.out.println("OpportunisticNetworkingManagerActivity: saveActivityState");
		// Use shared preferences to save the activity state
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		
		// save parameters
	    int sendPacketsPeriod = Integer.parseInt(((EditText)findViewById(R.id.sendPacketsPeriodValue)).getText().toString());
	    int expirationTimeManagedPackets = Integer.parseInt(((EditText)findViewById(R.id.expirationTimeManagedPacketsValue)).getText().toString());
		int availableStorage = Integer.parseInt(((EditText)findViewById(R.id.availableStorageValue)).getText().toString()); 
		boolean persistPackets = ((CheckBox)findViewById(R.id.persistPackets)).isChecked();
	    boolean removePacketAfterSend = ((CheckBox)findViewById(R.id.removePacketAfterSend)).isChecked();
		String[] numberOfOneHopDestinationsValues =  ((Spinner)findViewById(R.id.numberOfOneHopDestinationsSpinner)).getSelectedItem().toString().split("/");
		int numberOfOneHopDestinations = Integer.parseInt(numberOfOneHopDestinationsValues[1]);
		int minNumberOfOneHopDestination = Integer.parseInt(numberOfOneHopDestinationsValues[0]);
		int maxNumberOfOneHopDestination = Integer.parseInt(numberOfOneHopDestinationsValues[2]);
		int packetSizeThresholdHigher = Integer.parseInt(((EditText)findViewById(R.id.packetSizeThresholdHigherValue)).getText().toString());
		int packetSizeThresholdLower = Integer.parseInt(((EditText)findViewById(R.id.packetSizeThresholdLowerValue)).getText().toString());
		ReplacePackets rp = ReplacePackets.valueOf(((Spinner)findViewById(R.id.replacePacketsSpinner)).getSelectedItem().toString());

		editor.putBoolean("persistPackets", persistPackets);
		editor.putBoolean("removePacketAfterSend", removePacketAfterSend);
		editor.putInt("sendPacketsPeriod", sendPacketsPeriod);
		editor.putInt("expirationTimeManagedPackets", expirationTimeManagedPackets);
		editor.putInt("availableStorage", availableStorage);
		editor.putString("numberOfOneHopDestinations", minNumberOfOneHopDestination+"/"+numberOfOneHopDestinations+"/"+maxNumberOfOneHopDestination);
		editor.putInt("packetSizeThresholdHigher", packetSizeThresholdHigher);
		editor.putInt("packetSizeThresholdLower", packetSizeThresholdLower);
		editor.putString("replacePackets", rp.toString());
		
		// save remote services (serialized as string)
//		Util.saveRemoteServices(editor, services);

		// Commit
		editor.commit();
		
		if(onm != null)
		{
			onm.setSendPacketsPeriod(sendPacketsPeriod);
			onm.setExpirationTimeManagedPackets(expirationTimeManagedPackets);
			onm.setPersistPackets(persistPackets);
			onm.setRemovePacketAfterSend(removePacketAfterSend);
			onm.setAvailableStorage(availableStorage);
			onm.setMinNumberOfOneHopDestinations(minNumberOfOneHopDestination);
			onm.setNumberOfOneHopDestinations(numberOfOneHopDestinations);
			onm.setMaxNumberOfOneHopDestinations(maxNumberOfOneHopDestination);
			onm.setPacketSizeThresholdHigher(packetSizeThresholdHigher);
			onm.setPacketSizeThresholdLower(packetSizeThresholdLower);
			onm.setReplacePackets(rp);
			onm.serializeSettings();
			//onm = null;
		}
		
		checkRampState(); //Stefano Lanzone
	}

	private void restoreActivityState(){
		System.out.println("OpportunisticNetworkingManagerActivity: restoreActivityState");
		// Use shared preferences to restore the activity state
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		
		checkRampState(); //Stefano Lanzone
		boolean persistPackets = true;
	    boolean removePacketAfterSend = true;
	    int sendPacketsPeriod = 600;
	    int expirationTimeManagedPackets = 720;
		int availableStorage = 100;
		int numberOfOneHopDestinations = 4;
		int minNumberOfOneHopDestination = 2;
		int maxNumberOfOneHopDestination = 6; 
		int packetSizeThresholdHigher = 100;
		int packetSizeThresholdLower = 50;
		ReplacePackets rp = ReplacePackets.OLD;
		if(onm != null)
		{
			persistPackets = onm.isPersistPackets();
			removePacketAfterSend = onm.isRemovePacketAfterSend();
		    sendPacketsPeriod = onm.getSendPacketsPeriod();
		    expirationTimeManagedPackets = onm.getExpirationTimeManagedPackets();
			availableStorage = onm.getAvailableStorage();
			numberOfOneHopDestinations = onm.getNumberOfOneHopDestinations();
			minNumberOfOneHopDestination = onm.getMinNumberOfOneHopDestinations();
			maxNumberOfOneHopDestination = onm.getMaxNumberOfOneHopDestinations(); 
			packetSizeThresholdHigher = onm.getPacketSizeThresholdHigher();
			packetSizeThresholdLower = onm.getPacketSizeThresholdLower();
			rp = onm.getReplacePackets();
			
			//onm.deactivate(false);
		}
		
		// restore parameters
		persistPackets = settings.getBoolean("persistPackets", persistPackets);
	    removePacketAfterSend = settings.getBoolean("removePacketAfterSend", removePacketAfterSend);
	    sendPacketsPeriod = settings.getInt("sendPacketsPeriod", sendPacketsPeriod);
	    expirationTimeManagedPackets = settings.getInt("expirationTimeManagedPackets", expirationTimeManagedPackets);
		availableStorage = settings.getInt("availableStorage", availableStorage);
		String[] numberOfOneHopDestinationsValues = settings.getString("numberOfOneHopDestinations", minNumberOfOneHopDestination +"/"+numberOfOneHopDestinations+"/"+maxNumberOfOneHopDestination).split("/");
		numberOfOneHopDestinations = Integer.parseInt(numberOfOneHopDestinationsValues[1]);
		minNumberOfOneHopDestination = Integer.parseInt(numberOfOneHopDestinationsValues[0]);
		maxNumberOfOneHopDestination = Integer.parseInt(numberOfOneHopDestinationsValues[2]);
		packetSizeThresholdHigher = settings.getInt("packetSizeThresholdHigher", packetSizeThresholdHigher);
		packetSizeThresholdLower = settings.getInt("packetSizeThresholdLower", packetSizeThresholdLower);
		rp = ReplacePackets.valueOf(settings.getString("replacePackets", rp.toString()));
		
		((CheckBox)findViewById(R.id.persistPackets)).setChecked(persistPackets);
		((CheckBox)findViewById(R.id.removePacketAfterSend)).setChecked(removePacketAfterSend);
		((EditText)findViewById(R.id.sendPacketsPeriodValue)).setText(String.valueOf(sendPacketsPeriod));
		((EditText)findViewById(R.id.expirationTimeManagedPacketsValue)).setText(String.valueOf(expirationTimeManagedPackets));
		((EditText)findViewById(R.id.availableStorageValue)).setText(String.valueOf(availableStorage));
		Spinner numberOfOneHopDestinationsSpinner = ((Spinner)findViewById(R.id.numberOfOneHopDestinationsSpinner));
		numberOfOneHopDestinationsSpinner.setSelection(getSpinnerIndex(numberOfOneHopDestinationsSpinner, minNumberOfOneHopDestination +"/"+numberOfOneHopDestinations+"/"+maxNumberOfOneHopDestination));
		((EditText)findViewById(R.id.packetSizeThresholdLowerValue)).setText(String.valueOf(packetSizeThresholdLower));
		((EditText)findViewById(R.id.packetSizeThresholdHigherValue)).setText(String.valueOf(packetSizeThresholdHigher));
		Spinner replacePacketsSpinner = ((Spinner)findViewById(R.id.replacePacketsSpinner));
		replacePacketsSpinner.setSelection(getSpinnerIndex(replacePacketsSpinner, rp.toString()));
		
		// restore remote services
//		services = Util.restoreRemoteServices(this, settings, (Spinner)findViewById(R.id.remoteServices));
	}
	

	 private int getSpinnerIndex(Spinner spinner, String myString)
	 {
	  int index = 0;

	  for (int i=0;i<spinner.getCount();i++){
	   if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
	    index = i;
	    break;
	   }
	  }
	  return index;
	 } 

    @Override
    public void onBackPressed() {
        System.out.println("OpportunisticNetworkingManagerActivity: onBackPressed");
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        System.out.println("OpportunisticNetworkingManagerActivity: onDestroy, isFinishing = " + this.isFinishing());
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        System.out.println("OpportunisticNetworkingManagerActivity: onPause, isFinishing = " + this.isFinishing());
        super.onPause();
        saveActivityState();
    }

    @Override
    protected void onRestart() {
        System.out.println("OpportunisticNetworkingManagerActivity: onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        System.out.println("OpportunisticNetworkingManagerActivity: onResume");
        super.onResume();
    }

    @Override
    protected void onStart() {
        System.out.println("OpportunisticNetworkingManagerActivity: onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        System.out.println("OpportunisticNetworkingManagerActivity: onStop");
        super.onStop();
    }


}
