
package it.unife.dsg.ramp_android.service.application;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import it.unife.dsg.ramp_android.R;
import it.unibo.deis.lia.ramp.service.application.ChatServiceUserProfile;

import java.util.Date;

/**
*
* @author Stefano Lanzone
*/
public class ChatUserMoreInfoActivity extends Activity implements OnClickListener {

	private TextView userFirstName, userLastName, userBirthdate;
	private ImageView userProfilePicture;
	private EditText bxMessage;
	private ChatServiceUserProfile userProfile;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("ChatUserMoreInfoActivity: onCreate savedInstanceState = " + savedInstanceState);
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.chat_user_more_info);
        
        userFirstName =(TextView) findViewById(R.id.chatUserFirstName);
        userLastName =(TextView) findViewById(R.id.chatUserLastName);
        userBirthdate =(TextView) findViewById(R.id.chatUserBirthdate);
        userProfilePicture = (ImageView)findViewById(R.id.chatUserPicture);
        bxMessage = (EditText)findViewById(R.id.chatUserBxMessage);

        findViewById(R.id.backToChat).setOnClickListener(this);
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    
    @Override
    protected void onStart() {
        System.out.println("ChatUserMoreInfoActivity: onStart");
        super.onStart();
        
        userProfile = (ChatServiceUserProfile)getIntent().getSerializableExtra("UserMoreInfo");
    
        if(userProfile != null)
        {
        	userFirstName.setText(userProfile.getFirstName());
        	userLastName.setText(userProfile.getLastName());
        	userBirthdate.setText(userProfile.getBirthdate());
        	
        	byte[] imageByteArray = userProfile.getProfilePicture();
        	if(imageByteArray != null)
        	{
        		BitmapFactory.Options opt = new BitmapFactory.Options();
        		opt.inDither = true;
        		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        		Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length, opt);
        		userProfilePicture.setImageBitmap(bitmap);
        	}
        	
        	String message = userProfile.getBxMessage();
        	
        	if(message.equals(""))
        	{
        		bxMessage.setVisibility(View.GONE);
        		this.setTitle("Chat User More Info");
        	}
        	else
        	{
        		Date date=new Date(System.currentTimeMillis());
        		String messageBoard = date.toLocaleString() +": "+message;
        		bxMessage.setText(messageBoard);
        		bxMessage.setVisibility(View.VISIBLE);
        		
        		this.setTitle("Chat Broadcast Message");
        	}
        }
    }
    
    @Override
	public void onClick(View v) {
		
		 switch(v.getId()){
	         case R.id.backToChat:
	        	 System.out.println("ChatUserMoreInfoActivity: onClick = R.id.backToChat");
	         	onBackPressed();
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


    @Override
    public void onBackPressed() {
        System.out.println("ChatUserMoreInfoActivity: onBackPressed");
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        System.out.println("ChatUserMoreInfoActivity: onDestroy, isFinishing = " + this.isFinishing());
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        System.out.println("ChatUserMoreInfoActivity: onPause, isFinishing = " + this.isFinishing());
        super.onPause();
    }

    @Override
    protected void onRestart() {
        System.out.println("ChatUserMoreInfoActivity: onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        System.out.println("ChatUserMoreInfoActivity: onResume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        System.out.println("ChatUserMoreInfoActivity: onStop");
        super.onStop();
    }


}
