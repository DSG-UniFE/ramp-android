
package it.unibo.deis.lia.ramp.android.service.application;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import it.unibo.deis.lia.ramp.RampEntryPoint;

import it.unibo.deis.lia.ramp.android.R;
import it.unibo.deis.lia.ramp.android.util.Util;
import it.unibo.deis.lia.ramp.core.e2e.GenericPacket;
import it.unibo.deis.lia.ramp.service.application.ChatCommunicationSupport;
import it.unibo.deis.lia.ramp.service.application.ChatServiceMessage;
import it.unibo.deis.lia.ramp.service.application.ChatServiceON;
import it.unibo.deis.lia.ramp.service.application.ChatServiceUserProfile;
import it.unibo.deis.lia.ramp.service.application.FileSharingClient;
import it.unibo.deis.lia.ramp.service.management.ServiceResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
*
* @author Stefano Lanzone
*/
public class ChatOpenActivity extends Activity implements OnClickListener {

	private ChatServiceON ch=null;
	private ListView openedChatList;
	Hashtable<Integer, ChatCommunicationSupport> openedChat;
	Hashtable<String, Integer> openedChatListContent;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("ChatOpenActivity: onCreate savedInstanceState = " + savedInstanceState);
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.chat_open);
        
        openedChatList =(ListView) findViewById(R.id.listOpenChat);
        openedChatList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        findViewById(R.id.closeChat).setOnClickListener(this);
        findViewById(R.id.openChat).setOnClickListener(this);
        findViewById(R.id.backToChatService).setOnClickListener(this);
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    
    @Override
    protected void onStart() {
        System.out.println("ChatOpenActivity: onStart");
        super.onStart();
        
        checkRampState();
        
        //Set openedChatList
        openedChat = ch.getOpenedChat();
        if(openedChat != null && openedChat.size() > 0)
        {
        	//We have opened chat
        	loadOpenChatList(); 
        }
    }

	private void loadOpenChatList() {
		List<String> contentList = new ArrayList<String>();
		List<Integer> chatKeys = Collections.list(openedChat.keys());
		openedChatListContent = new Hashtable<String, Integer>();
		
		for (Iterator<Integer> iterator = chatKeys.iterator(); iterator.hasNext();) {
			Integer key = iterator.next();
			ChatCommunicationSupport support = openedChat.get(key);
			String chat = "";
			if(support.isGroupCommunication())
			{	
				chat = "Chat with group: " +key.toString();
			}
			else
			{
				chat = "Chat with: " +key.toString();
			}
			Hashtable<Integer, ChatServiceMessage> destInfo = support.getDestInfo();
			Enumeration<Integer> nodeId = destInfo.keys();
			while(nodeId.hasMoreElements()) {
				 chat = chat +"\n";
				 int destNodeId = (Integer) nodeId.nextElement();
				 ChatServiceUserProfile userProfile = destInfo.get(destNodeId).getUserProfile();
				 
				 String firstName = userProfile.getFirstName();
			     String lastName = userProfile.getLastName();
				 String user = firstName + " "+lastName;
				 chat = chat + user;
			}
			openedChatListContent.put(chat, key);
		}
		
		contentList = Collections.list(openedChatListContent.keys());
		ArrayAdapter<String> adapterChatList = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_multiple_choice, contentList) {
			@Override
			 public View getView(int position, View convertView, ViewGroup parent){
			 View view = super.getView(position, convertView, parent);
			 TextView textview = (TextView) view.findViewById(android.R.id.text1);
		     
			 //Set your Font Size Here.
			 textview.setTextSize(16);

			 return view;
			 }
		};
		
		openedChatList.setAdapter(adapterChatList);
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
	public void onClick(View v) {
    	checkRampState();
    	
    	if( ch==null ){
            Util.showShortToast(this, "Activate RAMP via the manager!");
            return;
        }
    	
		 switch(v.getId()){
	         case R.id.backToChatService:
	        	 System.out.println("ChatOpenActivity: onClick = R.id.backToChatService");
	         	 onBackPressed();
			 break;
	         case R.id.openChat:
	        	 System.out.println("ChatOpenActivity: onClick = R.id.openChat");
	        	 if(openedChat != null && openedChat.size() > 0)
	             {
	        		 List<Integer> supports = getChatSessionSelect();
	             
	        		 if(supports.size() > 0)
	            	 {
	        			 for (Iterator<Integer> iterator = supports.iterator(); iterator.hasNext();) {
							Integer chatSessionId = (Integer) iterator
									.next();
							
							try{
								Class<?> supportActivity= Class.forName("it.unibo.deis.lia.ramp.android.service.application.ChatComunicationSupportActivity");
								
					            Intent clientIntent = new Intent(
					            		this,supportActivity
					            );
					             
					            clientIntent.putExtra("user", chatSessionId);
					            startActivity(clientIntent);
					        }
					         catch(Exception e){
					             e.printStackTrace();
					         }
						}
	            	 }
	        		 else
	        		 {
	        			 Util.showShortToast(this, "First select a chat session!");
	        		 }
	             }
	        	 else
	        	 {
	        		 Util.showShortToast(this, "There aren't chat sessions!");
	        	 }
	        	 break; 
	         case R.id.closeChat:
	        	 System.out.println("ChatOpenActivity: onClick = R.id.closeChat");
	        	 
	        	 if(openedChat != null && openedChat.size() > 0)
	             {
	        		 List<Integer> supports = getChatSessionSelect();
	        		 
	        		 if(supports.size() > 0)
	            	 {
	        			 for (Iterator<Integer> iterator = supports.iterator(); iterator.hasNext();) {
							Integer chatSessionId = (Integer) iterator
									.next();
							ch.closeChatSupport(chatSessionId);
						}
	        			 
	        			//Refresh open chat list
	        			 loadOpenChatList();
	            	 }
	        		 else
	        		 {
	        			 Util.showShortToast(this, "First select a chat session!");
	        		 }
	             }
	        	 else
	        	 {
	        		 Util.showShortToast(this, "There aren't chat sessions!");
	        	 }

	        	 break;
		 }
    }

	private List<Integer> getChatSessionSelect() {
		List<Integer> supports = new ArrayList<Integer>();
		 int cntChoice = openedChatList.getCount();
		 SparseBooleanArray sparseBooleanArray = openedChatList.getCheckedItemPositions();

		 for(int i = 0; i < cntChoice; i++){
			 if(sparseBooleanArray.get(i)) {
				 Integer chatSessionId = openedChatListContent.get((openedChatList.getItemAtPosition(i).toString()));
				 supports.add(chatSessionId);
			 }
		 }
		return supports;
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


    @Override
    public void onBackPressed() {
        System.out.println("ChatOpenActivity: onBackPressed");
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        System.out.println("ChatOpenActivity: onDestroy, isFinishing = " + this.isFinishing());
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        System.out.println("ChatOpenActivity: onPause, isFinishing = " + this.isFinishing());
        super.onPause();
    }

    @Override
    protected void onRestart() {
        System.out.println("ChatOpenActivity: onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        System.out.println("ChatOpenActivity: onResume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        System.out.println("ChatOpenActivity: onStop");
        super.onStop();
    }


}
