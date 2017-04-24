
package it.unife.dsg.ramp_android.service.application;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.util.Base64;
import android.util.SparseBooleanArray;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;

import it.unife.dsg.ramp_android.R;
import it.unibo.deis.lia.ramp.RampEntryPoint;
import it.unife.dsg.ramp_android.util.Util;
import it.unibo.deis.lia.ramp.core.e2e.GenericPacket;
import it.unibo.deis.lia.ramp.core.internode.Dispatcher;
import it.unibo.deis.lia.ramp.service.application.ChatCommunicationSupport;
import it.unibo.deis.lia.ramp.service.application.ChatServiceMessage;
import it.unibo.deis.lia.ramp.service.application.ChatServiceON;
import it.unibo.deis.lia.ramp.service.application.ChatServiceRequest;
import it.unibo.deis.lia.ramp.service.application.ChatServiceUserProfile;
import it.unibo.deis.lia.ramp.service.management.ServiceResponse;

/**
*
* @author Stefano Lanzone
*/

public class ChatServiceActivity extends Activity implements
		OnClickListener {

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    static private final int RampActiveNotification = R.string.ramp_active_notification;

	private ChatServiceON ch=null;
	private Hashtable<String, ServiceResponse> contacts = null;
	
	private ImageView profilePicture;
	private EditText firstName, lastName, birthdate;
	private EditText bxMessage, bxMessageSent;
	
	private ListView remoteContactsList;
	
	private static ChatServiceActivity chatServiceInstance = null;

	public static ChatServiceActivity getInstance() {
		return chatServiceInstance;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		System.out.println("ChatServiceActivity: onCreate");
		chatServiceInstance = ChatServiceActivity.this;
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.chat_service);
		
    	profilePicture = (ImageView) findViewById(R.id.imageViewPicture);
		firstName = (EditText)findViewById(R.id.userFirstName);
		lastName = (EditText)findViewById(R.id.userLastName);
		birthdate = (EditText)findViewById(R.id.userBirthdate);
		
		remoteContactsList = (ListView)findViewById(R.id.listContacts);
		remoteContactsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		//remoteContactsSpinner = new Spinner(this);
		
		bxMessage = (EditText)findViewById(R.id.bxChatMessage);
		bxMessageSent = (EditText)findViewById(R.id.bxChatMessageSent);
		
		findViewById(R.id.takePictureFromCamera).setOnClickListener(this);
		findViewById(R.id.takePictureFromGallery).setOnClickListener(this);
		findViewById(R.id.discoverContacts).setOnClickListener(this);
		findViewById(R.id.startchat).setOnClickListener(this);
		findViewById(R.id.sendbxchatmessage).setOnClickListener(this);
		findViewById(R.id.openedChat).setOnClickListener(this);
		findViewById(R.id.clearBxChatMessageSent).setOnClickListener(this);
		findViewById(R.id.backToManager).setOnClickListener(this);
		
		if(RampEntryPoint.isActive() && RampEntryPoint.getInstance(false, null).isContinuityManagerActive())
        {	
        	(findViewById(R.id.expiryChatValue)).setVisibility(View.VISIBLE);
        	(findViewById(R.id.expiryChatTitle)).setVisibility(View.VISIBLE);
        }
        else
        {
        	(findViewById(R.id.expiryChatValue)).setVisibility(View.GONE);
        	(findViewById(R.id.expiryChatTitle)).setVisibility(View.GONE);
        }
		
		checkRampState();
		restoreActivityState();
		
		// hide soft keyboard at activity start-up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	private void restoreActivityState(){
		System.out.println("ChatServiceActivity: restoreActivityState");
		// Use shared preferences to restore the activity state
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		
		String name = "";
		String surname = "";
		String birth="";
		int ttl = 3;
		int timeout = 5000;
		int expiry = 300;
		int contactAmount = 0;
		
		// restore parameters
		name = settings.getString("firstName", name);
		surname = settings.getString("lastName", surname);
		birth = settings.getString("birthdate", birth);
		ttl = settings.getInt("ttlchat", ttl);
		timeout = settings.getInt("timeoutchat", timeout);
		expiry = settings.getInt("expirychat", expiry);
		contactAmount = settings.getInt("contactAmountchat", contactAmount);

		firstName.setText(name);
		lastName.setText(surname);
		birthdate.setText(birth);
		((EditText)findViewById(R.id.ttlChatValue)).setText(String.valueOf(ttl));
		((EditText)findViewById(R.id.timeoutChatValue)).setText(String.valueOf(timeout));
		((EditText)findViewById(R.id.contactChatAmountValue)).setText(String.valueOf(contactAmount));
		((EditText)findViewById(R.id.expiryChatValue)).setText(String.valueOf(expiry));
		
		String previouslyEncodedImage = settings.getString("picture", "");
		if( !previouslyEncodedImage.equalsIgnoreCase("") ){
			byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
		    Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
		    profilePicture.setImageBitmap(bitmap);
		    
		    ChatServiceUserProfile csuf = ch.getChatUserProfile();
		    csuf.setProfilePicture(b);
		}

		 // restore remote services
//		 contactsFound = Util.restoreRemoteServices(this, settings, remoteContactsSpinner);
//		 if(contactsFound == null)
//			 contactsFound = new Vector<ServiceResponse>();
//		 contactsFound = discoverContacts(ttl, timeout, contactAmount, contactsFound, false);
		
		String bxChatMessageSent = "";
		bxChatMessageSent = settings.getString("bxChatMessageSent", bxChatMessageSent);
		bxMessageSent.setText(bxChatMessageSent);
	}
	
	private void saveActivityState(){
		
		System.out.println("ChatServiceActivity: saveActivityState");
		// Use shared preferences to save the activity state
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		
		// save parameters
		int ttl = Integer.parseInt(((EditText)findViewById(R.id.ttlChatValue)).getText().toString());
	    int timeout = Integer.parseInt(((EditText)findViewById(R.id.timeoutChatValue)).getText().toString());
		int contactAmount = Integer.parseInt(((EditText)findViewById(R.id.contactChatAmountValue)).getText().toString()); 
		int expiry = Integer.parseInt(((EditText)findViewById(R.id.expiryChatValue)).getText().toString()); 
		String name = firstName.getText().toString();
		String surname = lastName.getText().toString();
		String birth= birthdate.getText().toString();
		
		editor.putInt("ttlchat", ttl);
		editor.putInt("timeoutchat", timeout);
		editor.putInt("expirychat", expiry);
		editor.putInt("contactAmountchat", contactAmount);
		editor.putString("firstName", name);
		editor.putString("lastName", surname);
		editor.putString("birthdate", birth);
		
		ChatServiceUserProfile csuf = ch.getChatUserProfile();
		if(csuf.getProfilePicture() != null)
		{
			byte[] imageByteArray = csuf.getProfilePicture();
			String encodedImage = Base64.encodeToString(imageByteArray, Base64.DEFAULT);
			editor.putString("picture", encodedImage);
		}
		// save remote chat services (serialized as string)
		//Util.saveRemoteServices(editor, contactsFound);
		
		String bxChatMessageSent = bxMessageSent.getText().toString();
		editor.putString("bxChatMessageSent", bxChatMessageSent);
		
		// Commit
		editor.commit();
	}
	
	public void onStart() {
		System.out.println("ChatServiceActivity: onStart");
		super.onStart();
		
		checkRampState();
	}

	@Override
	public void onClick(View view) {
		checkRampState();
		
		if( view.getId() == R.id.backToManager){
        	onBackPressed();
		}
		else if( ch==null ){
	            Util.showShortToast(this, "Activate RAMP via the manager!");
	        }
		else{
			int timeout = 0;
			
            switch(view.getId()){
            case R.id.takePictureFromCamera:
            	System.out.println("ChatServiceActivity: onClick = R.id.takePictureFromCamera");
            	
            	Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            	startActivityForResult(takePicture, 0);//zero can be replaced with any action code
            	 break;
            case R.id.takePictureFromGallery:
            	System.out.println("ChatServiceActivity: onClick = R.id.takePictureFromGallery");
            	
            	Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            	startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
           	 break;
            case R.id.discoverContacts:
            	System.out.println("ChatServiceActivity: onClick = R.id.discoverContacts");
            	
        		int ttl = Integer.parseInt(((EditText)findViewById(R.id.ttlChatValue)).getText().toString());
        		timeout = Integer.parseInt(((EditText)findViewById(R.id.timeoutChatValue)).getText().toString());
        		int serviceAmount = Integer.parseInt(((EditText)findViewById(R.id.contactChatAmountValue)).getText().toString());
//        		contactsFound = new Vector<ServiceResponse>();
        		try
        		{
        			long pre = System.currentTimeMillis();
        			Vector<ServiceResponse> contactsFound = ch.findContactChatService(ttl, timeout, serviceAmount);
        			long post = System.currentTimeMillis();
        			float elapsed = (post-pre)/(float)1000;
        			timeout -= elapsed;
        			discoverContacts(ttl, timeout, serviceAmount, contactsFound, true);
        		}
        		catch(Exception e){
        		 e.printStackTrace();
        		}
              	 break;
            case R.id.startchat:
             System.out.println("ChatServiceActivity: onClick = R.id.startchat");
             String alertMessage = "";
             
             if(contacts != null && contacts.size() > 0)
        	 {
            	 List<ServiceResponse> selectedContacts = new ArrayList<ServiceResponse>();
            	 int cntChoice = remoteContactsList.getCount();
            	 SparseBooleanArray sparseBooleanArray = remoteContactsList.getCheckedItemPositions();

            	 for(int i = 0; i < cntChoice; i++){
            		 if(sparseBooleanArray.get(i)) {
            			 selectedContacts.add(contacts.get((remoteContactsList.getItemAtPosition(i).toString())));
            		 }
            	 } 
            	 
            	 if(selectedContacts.size() > 0)
            	 {
           				timeout = Integer.parseInt(((EditText)findViewById(R.id.timeoutChatValue)).getText().toString());
           				
           				if(timeout <= 0)
           					alertMessage = "Timeout must be greater than 0!";
           				else
           				{
           					int expiry = GenericPacket.UNUSED_FIELD;
           					int id = -1;
           					if(RampEntryPoint.isActive() && RampEntryPoint.getInstance(false, null).isContinuityManagerActive())
           					{
           						expiry = Integer.parseInt(((EditText)findViewById(R.id.expiryChatValue)).getText().toString());
           						System.out.println("ChatServiceActivity: chat message expires after "+expiry+" seconds");
           					}
           				    
           					try
           					{
           						setChatUserProfile();
           						id = ch.startCommunication(selectedContacts, ch.getChatUserProfile());
           					}
           					catch(Exception e){
           	                 e.printStackTrace();
           					}
           					
           					if(id != -1)
           						this.startChat(id);
           					else
           					{ 
           						alertMessage = "User not reached!";
           						if(selectedContacts.size() != 1)
           							alertMessage = "Group not reached!";
           					}
           				}
            	}
           		else
           			alertMessage = "First select a contact!";
        	}
           	else
           		alertMessage = "First find contact!";
             
            if(!alertMessage.equals(""))
            	Util.showShortToast(this, alertMessage);
             	
            break;
            case R.id.openedChat:
            	System.out.println("ChatServiceActivity: onClick = R.id.openedChat");
            	
            	if(ch.getOpenedChat() == null ||  ch.getOpenedChat().size() == 0)
            	{
            		Util.showShortToast(this, "There aren't open chat sessions!");
            	}
            	else
            	{
            		try{
            			Class<?> supportActivity= Class.forName("ChatOpenActivity");
            			
                        Intent clientIntent = new Intent(
                        		this,supportActivity
                        );

                        startActivity(clientIntent);
                    }
                     catch(Exception e){
                         e.printStackTrace();
                     }
            	}
            	
            	break;
            case R.id.sendbxchatmessage:
                System.out.println("ChatServiceActivity: onClick = R.id.sendbxchatmessage");
                
                String message = bxMessage.getText().toString();
                if(!message.equals(""))
                {
                	setChatUserProfile();
                	boolean sent = ch.sendBroadcastMessage(ch.getChatUserProfile());
                	if(!sent)
                		Util.showShortToast(this, "Bx message not sent!");
                	else
                	{
                		Date date=new Date(System.currentTimeMillis());
                		String messageBoard = date.toLocaleString() +": "+message;
                		String lastMessages = bxMessageSent.getText().toString();
                		if(!lastMessages.equals(""))
                			lastMessages = lastMessages + "\n";
                		messageBoard = lastMessages + messageBoard;
                		bxMessageSent.setText(messageBoard);
                		
                		message = "";
                    	bxMessage.setText(message);
                	}
                }
                break;
            case R.id.clearBxChatMessageSent:
            	System.out.println("ChatServiceActivity: onClick = R.id.clearBxChatMessageSent");
            	bxMessageSent.setText("");
                break;
            }
		}
	 }

	private void discoverContacts(int ttl, int timeout, int serviceAmount, Vector<ServiceResponse> contactsFound, boolean showMessage) throws Exception {

		if(timeout <= 0)
		{
			if(showMessage)
				Util.showShortToast(this, "Timeout, contacts not found!");
			return;
		}
		
		if(serviceAmount != 0)
			serviceAmount++; //per escludere il servizio locale!

		contacts = new Hashtable<String, ServiceResponse>();
		//Util.populateSpinner(this, remoteContactsSpinner, contactsFound);
		    
		List<String> listRemoteContactsContent = new ArrayList<String>();
		List<ServiceResponse> filterContactsFound = new ArrayList<ServiceResponse>();
		    
		int nodeId = Dispatcher.getLocalRampId();
		for (int i = 0; i < contactsFound.size(); i++)
		{
			if(contactsFound.get(i).getServerNodeId() != nodeId)
		    {	
//		    	String contact = contactsFound.get(i).toString();
//		    	contacts.put(contact, contactsFound.get(i));
//		    	listRemoteContactsContent.add(contact);
		    	filterContactsFound.add(contactsFound.get(i));
		    }
		}
		
		if(filterContactsFound.size() > 0)
		{	
			contacts = ch.getContactInfo(filterContactsFound, timeout);
			
//			if(contacts != null)
//			{
//				listRemoteContactsContent = Collections.list(contacts.keys());
//			    ArrayAdapter<String> adapterRemoteContactsList = new ArrayAdapter<String>(this, 
//							android.R.layout.simple_list_item_multiple_choice, listRemoteContactsContent) {
//				    	@Override
//				    	 public View getView(int position, View convertView, ViewGroup parent){
//				    	 View view = super.getView(position, convertView, parent);
//				    	 TextView textview = (TextView) view.findViewById(android.R.id.text1);
//
//				    	 //Set your Font Size Here.
//				    	 textview.setTextSize(16);
//
//				    	 return view;
//				    	 }
//				 };
//				    
//				 remoteContactsList.setAdapter(adapterRemoteContactsList); 
//			}
//			if(contacts == null && showMessage)
//			{
//				Util.showShortToast(this, "Contacts info not found!");
//			}
		}
		

	    listRemoteContactsContent = Collections.list(contacts.keys());
		
	    ArrayAdapter<String> adapterRemoteContactsList = new ArrayAdapter<String>(this, 
					android.R.layout.simple_list_item_multiple_choice, listRemoteContactsContent) {
		    	@Override
		    	 public View getView(int position, View convertView, ViewGroup parent){
		    	 View view = super.getView(position, convertView, parent);
		    	 TextView textview = (TextView) view.findViewById(android.R.id.text1);

		    	 //Set your Font Size Here.
		    	 textview.setTextSize(16);

		    	 return view;
		    	 }
		 };
		    
		 remoteContactsList.setAdapter(adapterRemoteContactsList); 
		 
		 if(contacts.size() == 0 && showMessage)
		    	Util.showShortToast(this, "Contacts not found!");
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 
		switch(requestCode) {
		case 0:
		    if(resultCode == RESULT_OK){  
		        setProfilePicture(imageReturnedIntent);
		    }

		break; 
		case 1:
		    if(resultCode == RESULT_OK){  
		        setProfilePicture(imageReturnedIntent);
		    }
		break;
		}
	}

	private void setProfilePicture(Intent imageReturnedIntent) {
		Uri selectedImage = imageReturnedIntent.getData();
		Bitmap photo = null;
		
		if(selectedImage == null)
		{
			photo = (Bitmap) imageReturnedIntent.getExtras().get("data"); 
			profilePicture.setImageBitmap(photo);
		}
		else
		{
			profilePicture.setImageURI(selectedImage);

			Matrix matrix=new Matrix();
			profilePicture.setScaleType(ScaleType.MATRIX);   //required
			matrix.postRotate(270f, profilePicture.getDrawable().getBounds().width()/2, profilePicture.getDrawable().getBounds().height()/2);
			profilePicture.setImageMatrix(matrix);
			
			photo = ((BitmapDrawable)profilePicture.getDrawable()).getBitmap();
		}
		
		ChatServiceUserProfile csuf = ch.getChatUserProfile();
		//Compress and convert bitmap to ByteArray
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		photo.compress(Bitmap.CompressFormat.JPEG, 5, stream);
		csuf.setProfilePicture(stream.toByteArray());
	}
	
	private void checkRampState() {
		if( RampEntryPoint.isActive() && ch==null ){
			ch = ChatServiceON.getInstance();
        }
        else if( !RampEntryPoint.isActive() && ch!=null ){
        	ch.stopService();
        	ch = null;
        }
	}
	
    @Override
	public void onBackPressed() {
	        System.out.println("ChatServiceActivity: onBackPressed");
	        super.onBackPressed();
	 }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menuchat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		switch (item.getItemId()) {
			case R.id.menuBack:
				onBackPressed();
				return true;
			case R.id.menuExit:
				try {
					System.out.println("ChatServiceActivity:  deactivating...");
					return true;
				} 
				catch (Exception e) {
					e.printStackTrace();
					System.out.println("ChatServiceActivity stop: ChatService");
					return true;
				}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStop() {
		System.out.println("ChatServiceActivity: onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		System.out.println("ChatServiceActivity: onDestroy");

		super.onDestroy();
	}
	
	@Override
    protected void onPause() {
        System.out.println("ChatServiceActivity: onPause, isFinishing = " + this.isFinishing());
        super.onPause();
        saveActivityState();
    }
	
	 @Override
	    protected void onRestart() {
	        System.out.println("ChatServiceActivity: onRestart");
	        super.onRestart();
	 }
	
	 @Override
	    protected void onResume() {
	        System.out.println("ChatServiceActivity: onResume");
	        super.onResume();
	}
	 
	public void createNotification(String type, int user)
	{
		String contentTitle = "Chat Service";
		
		if(user != -1)
		{
			ChatCommunicationSupport ccs = ch.getChatSupport(user);
			// Prepare intent which is triggered if the
			// notification is selected
			Intent intent = new Intent(this, ChatComunicationSupportActivity.class);
			intent.putExtra("user", user);
			PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

			ChatServiceMessage csm = (ChatServiceMessage)ccs.getDestInfo().values().toArray()[0];
			String contentText = "Chat ";
			if(ccs.isGroupCommunication())
				contentText = contentText +"group ";
    
			contentText = contentText +type +" from "+csm.getNodeId();
			String firstName = csm.getUserProfile().getFirstName();
			String lastName = csm.getUserProfile().getLastName();
			if(!firstName.equals("") && !lastName.equals(""))
				contentText = contentText +": " +firstName +" " +lastName;

            // Build notification
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setTicker(contentTitle + ":" + contentText)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.ramp_logo)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL);

            notificationBuilder.setContentIntent(pIntent);
            // Because the ID remains unchanged, the existing notification is updated.
            notificationManager.notify(RampActiveNotification, notificationBuilder.build());
		}
		else
		{
			//Notify Bx Message
			ChatServiceRequest cs_request = ch.getLastBxMessage();
			if(cs_request != null)
			{
				ChatServiceUserProfile userProfile = cs_request.getUserProfile();
				
				// Prepare intent which is triggered if the
				// notification is selected
				Intent intent = new Intent(this, ChatUserMoreInfoActivity.class);
				intent.putExtra("UserMoreInfo", userProfile);
				PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
				
				String contentText = "Alert message from "+cs_request.getNodeId();
				String firstName = userProfile.getFirstName();
				String lastName = userProfile.getLastName();
				if(!firstName.equals("") && !lastName.equals(""))
					contentText = contentText +": " +firstName +" " +lastName;

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
                notificationManager.notify(RampActiveNotification, notificationBuilder.build());

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
		}
	}
	
	public void startChat(int user){
		 
		try{
			Class<?> supportActivity= Class.forName("ChatComunicationSupportActivity");
			
            Intent clientIntent = new Intent(
            		this,supportActivity
            );
             
            clientIntent.putExtra("user", user);
            startActivity(clientIntent);
        }
         catch(Exception e){
             e.printStackTrace();
         }
	}
	
	public void setChatUserProfile()
	{
		ChatServiceUserProfile csuf = ch.getChatUserProfile();
		csuf.setFirstName(firstName.getText().toString());
		csuf.setLastName(lastName.getText().toString());
		csuf.setBirthdate(birthdate.getText().toString());
	    int expiry = GenericPacket.UNUSED_FIELD;
		if(RampEntryPoint.isActive() && RampEntryPoint.getInstance(false, null).isContinuityManagerActive())
			expiry = Integer.parseInt(((EditText)findViewById(R.id.expiryChatValue)).getText().toString());
		
		csuf.setExpiry(expiry);
		
		int ttl = Integer.parseInt(((EditText)findViewById(R.id.ttlChatValue)).getText().toString());
        int timeout = Integer.parseInt(((EditText)findViewById(R.id.timeoutChatValue)).getText().toString());
        int serviceAmount = Integer.parseInt(((EditText)findViewById(R.id.contactChatAmountValue)).getText().toString());
        
		csuf.setTTL(ttl);
		csuf.setTimeout(timeout);
		csuf.setServiceAmount(serviceAmount);
		csuf.setBxMessage(bxMessage.getText().toString());
	}
}
