package it.unife.dsg.ramp_android.util;

import it.unibo.deis.lia.ramp.service.management.ServiceResponse;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class Util {
	
	
	// Save and restore discovered remote services
	
	public static void saveRemoteServices(SharedPreferences.Editor editor, List<ServiceResponse> services) {
		if(services != null && services.size() > 0){
			editor.putInt("servicesSize", services.size());
			Iterator<ServiceResponse> iterator = services.iterator();
			int i = 0;
			
			while(iterator.hasNext()){
				ServiceResponse sr = iterator.next();
				int serverNodeId = sr.getServerNodeId();
				int serverPort = sr.getServerPort();
				int serverProtocol = sr.getProtocol();
				String serverName = sr.getServiceName();
				String serverDestAddr = TextUtils.join(",", sr.getServerDest());
				String serverQos = sr.getQos();
				editor.putInt("services_" + i + "_serverNodeId", serverNodeId);
				editor.putInt("services_" + i + "_serverPort", serverPort);
				editor.putInt("services_" + i + "_serverProtocol", serverProtocol);
				editor.putString("services_" + i + "_serverName", serverName);
				editor.putString("services_" + i + "_serverDestAddr", serverDestAddr);
				if(serverQos != null)
					editor.putString("services_" + i + "_serverQos", serverQos);
				i++;
			}
			
		}
	}
	
	public static Vector<ServiceResponse> restoreRemoteServices(Context context, SharedPreferences settings, Spinner remoteServicesSpinner) {
		int servicesSize = settings.getInt("servicesSize", 0);
		if(servicesSize > 0){
			Vector<ServiceResponse> services = new Vector<ServiceResponse>(servicesSize);
			for(int i = 0; i < servicesSize; i++){
				int serverNodeId = settings.getInt("services_" + i + "_serverNodeId", -1);
				int serverPort = settings.getInt("services_" + i + "_serverPort", -1);
				int serverProtocol = settings.getInt("services_" + i + "_serverProtocol", -1);
				String serverName = settings.getString("services_" + i + "_serverName", null);
				String serverDestAddr = settings.getString("services_" + i + "_serverDestAddr", null);
				String serverQos = settings.getString("services_" + i + "_serverQos", null);
				ServiceResponse sr = new ServiceResponse(serverName, serverPort, serverProtocol, serverQos);
				if(serverDestAddr != null)
					sr.setServerDest(serverDestAddr.split(","));
				sr.setServerNodeId(serverNodeId);
				services.add(sr);
			}
			populateSpinner(context, remoteServicesSpinner, services);
            return services;
		}
		return null;
	}

	// Populate spinners
	
	public static void populateSpinner(Context context, Spinner spinner, List<?> elements){
    	String[] elementsString = new String[elements.size()];
    	Iterator<?> elementsIterator = elements.iterator();
    	int i = 0;
    	
    	while(elementsIterator.hasNext()){
    		elementsString[i++] = elementsIterator.next().toString();
    	}
    	
        populateSpinner(context, spinner, elementsString);
    }
    
    public static void populateSpinner(Context context, Spinner spinner, String[] elements){
    	ArrayAdapter<String> adapterServices = new ArrayAdapter<String>(
                context,
                android.R.layout.simple_spinner_item,
                elements
        );
        spinner.setAdapter(adapterServices);
    }
    
    // Show Toast
    public static void showShortToast(Context context, String message) {
    	Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    public static void showLongToast(Context context, String message) {
    	Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
	
}
