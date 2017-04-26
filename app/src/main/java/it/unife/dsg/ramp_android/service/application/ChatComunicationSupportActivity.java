
package it.unife.dsg.ramp_android.service.application;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import it.unife.dsg.ramp_android.R;
import it.unibo.deis.lia.ramp.service.application.ChatCommunicationSupport;
import it.unibo.deis.lia.ramp.service.application.ChatServiceMessage;
import it.unibo.deis.lia.ramp.service.application.ChatServiceON;
import it.unibo.deis.lia.ramp.service.application.ChatServiceUserProfile;

/**
*
* @author Stefano Lanzone
*/
public class ChatComunicationSupportActivity extends AppCompatActivity implements OnClickListener{

	private static ChatComunicationSupportActivity chatComSupAct=null;
	private int chatComId= -1;
	private ChatCommunicationSupport support=null;
	
	private TextView titleChatTextView;
	private EditText chatBoard;
	private EditText chatMessage;
	private TableLayout userInfoTable;
	private UpdateMessageHandler messageHandler;
	   
	   
	public static ChatComunicationSupportActivity getInstance() {
		return chatComSupAct;
	} 
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		System.out.println("ChatComunicationSupportActivity: onCreate savedInstanceState = " + savedInstanceState);
        
		super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_communication_support);
        
        chatComSupAct = ChatComunicationSupportActivity.this;
        
        titleChatTextView =(TextView) findViewById(R.id.userTitleChat);
        chatBoard=(EditText) findViewById(R.id.chatText);
        chatMessage=(EditText) findViewById(R.id.message);   
        userInfoTable = (TableLayout) findViewById(R.id.userInfoTable);

        findViewById(R.id.sendButton).setOnClickListener(this);
        
        initializeLayout();
        
     // hide soft keyboard at activity start-up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
	
	@Override
	public void onStart(){
		System.out.println("ChatComunicationSupportActivity: onStart");
		
		super.onStart();
		
		initializeLayout();

		messageHandler=new UpdateMessageHandler();
		messageHandler.start();
	}

	private void initializeLayout() {
		chatComId = getIntent().getIntExtra("user", -1);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		startChatSupport();
		support.setChatComIdVisible(chatComId);
		
		String headerChat = "Chatting with";
		if(support.isGroupCommunication())
			headerChat = headerChat + " group: "+chatComId;
		else
		{
			headerChat = headerChat + ": "+chatComId;
		}
		titleChatTextView.setText(headerChat);
		
		Hashtable<Integer, ChatServiceMessage> destInfo = support.getDestInfo();
		Enumeration<Integer> nodeId = destInfo.keys();
		int userId = 0;
		userInfoTable.removeAllViews();
		while(nodeId.hasMoreElements()) {
			 int destNodeId = (Integer) nodeId.nextElement();
			 ChatServiceUserProfile userProfile = destInfo.get(destNodeId).getUserProfile();
			 
			 String firstName = userProfile.getFirstName();
		     String lastName = userProfile.getLastName();
			 String user = firstName + " "+lastName;
			 
			 //Add TableRow to userInfoTable
			 /* Create a new row to be added. */
			 TableRow tr = new TableRow(this);
			 tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.FILL_PARENT, 1));
			 tr.setOrientation(TableRow.VERTICAL);
			 /* Create a TextView to be the row-content. */
			 TextView userTextView = new TextView(this);
			 userTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.FILL_PARENT, 10));
			 userTextView.setText(user);
			 /* Add TextView to row. */
			 tr.addView(userTextView);
			 /* Create a Button to be the row-content. */
			 Button moreInfo = new Button(this);
			 moreInfo.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.FILL_PARENT, 1));
			 moreInfo.setTag(userProfile);
			 moreInfo.setId(userId);
			 moreInfo.setText("More info");
			 moreInfo.setOnClickListener(this);
			 /* Add Button to row. */
			 tr.addView(moreInfo);
			 /* Add row to TableLayout. */
			 userInfoTable.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT));
			 
			 userId++;
		}
	}
	
	@Override
    protected void onRestart() {
        System.out.println("ChatComunicationSupportActivity: onRestart");
        super.onRestart();
        
        startChatSupport();
   }
	
	@Override
    protected void onResume() {
        System.out.println("ChatComunicationSupportActivity: onResume");
        super.onResume();
        support.setSupportVisible(true);
    }
	
	@Override
	protected void onStop() {
		System.out.println("ChatComunicationSupportActivity: onStop");
		super.onStop();
	}
	
	@Override
    protected void onPause() {
        System.out.println("ChatComunicationSupportActivity: onPause");
        super.onPause();
        support.setSupportVisible(false);
    }
	
	@Override
	protected void onDestroy() {
		System.out.println("ChatComunicationSupportActivity: onDestroy");

		super.onDestroy();
	}

	private void startChatSupport() {
		//if(support == null)
        	support= ChatServiceON.getInstance().getChatSupport(chatComId);
        
        if(!support.isActive())
        	support.start();
	}
	
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			chatBoard.setText(messageHandler.getMes());
			
		}
	};
	
	
	private class UpdateMessageHandler extends Thread {
		private String mes="";
		public String getMes() {
			return mes;
		}
		public void run() {
			Vector<String> messages;
			while(true){
				messages=support.getReceivedMessages();
				mes="";
				for(int i=0;i<messages.size();i++){
					mes+=messages.elementAt(i)+"\n";
				}
				handler.sendEmptyMessage(0);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


	@Override
	public void onClick(View v) {
		
		 switch(v.getId()){
	         case R.id.sendButton:
	         
	         if(!chatMessage.getText().toString().equals(""))
	         {
	        	 support.sendMessage(chatMessage.getText().toString());
	        	 chatMessage.setText("");
	         }
			 break;
//	         case R.id.moreInfoButton:
				 
//	        	 if(!support.isGroupCommunication())
//	        	 {
//	        		 ChatServiceMessage csm = support.getDestInfo().get(chatComId);
//
//	        		 try{
//	        				Class<?> supportActivity= Class.forName("ChatUserMoreInfoActivity");
//	        				
//	        	            Intent clientIntent = new Intent(
//	        	            		this,supportActivity
//	        	            );
//	        	             
//	        	            clientIntent.putExtra("UserMoreInfo", csm.getUserProfile());
//	        	            startActivity(clientIntent);
//	        	        }
//	        	         catch(Exception e){
//	        	             e.printStackTrace();
//	        	         }
//	        	 }

//				 break;
			default:
				//More Info Button
				System.out.println("ChatServiceActivity: onClick = "+v.getId());
				Button moreInfoButton = ((Button) v);
				ChatServiceUserProfile userProfile =(ChatServiceUserProfile) moreInfoButton.getTag();
				
//       		 Util.showShortToast(this, "First name: "+userProfile.getFirstName() +
//   			 "\nLast name: "+userProfile.getLastName() +
//   			 "\nBirthdate: "+userProfile.getBirthdate() );
				
				try{
    				Class<?> supportActivity= Class.forName("ChatUserMoreInfoActivity");
    				
    	            Intent clientIntent = new Intent(
    	            		this,supportActivity
    	            );
    	             
    	            clientIntent.putExtra("UserMoreInfo", userProfile);
    	            startActivity(clientIntent);
    	        }
    	         catch(Exception e){
    	             e.printStackTrace();
    	         }
				
				break; 
		 }
	}
	
}
