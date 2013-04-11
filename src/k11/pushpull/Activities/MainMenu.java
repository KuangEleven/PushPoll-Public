package k11.pushpull.Activities;

import k11.pushpull.Keys;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Callbacks.IntegerCallback;
import k11.pushpull.Callbacks.UserCallback;
import k11.pushpull.Data.Response;
import k11.pushpull.Data.User;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.gcm.GCMRegistrar;

public class MainMenu extends Activity implements OnClickListener {
	GoogleAnalyticsTracker tracker;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        tracker = GoogleAnalyticsTracker.getInstance();

        // Start the tracker in manual dispatch mode...
        tracker.startNewSession(Keys.ANALYTICS_KEY, this);
        
        //Click Listeners
        findViewById(R.id.responses_button).setOnClickListener(this);
        findViewById(R.id.polls_button).setOnClickListener(this);
        findViewById(R.id.options_button).setOnClickListener(this);
        findViewById(R.id.friends_button).setOnClickListener(this);
        findViewById(R.id.website_button).setOnClickListener(this);
        findViewById(R.id.logout_button).setOnClickListener(this);
    }
    
    @Override
    public void onDestroy() {
        GCMRegistrar.onDestroy(this);
        tracker.stopSession();
        super.onDestroy();
    }
    
    @Override
    public void onStop() {
    	tracker.dispatch();
    	super.onStop();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	tracker.trackPageView("/");
    	final PushPullApplication application = (PushPullApplication) getApplication();
        if (application.getCurrentUser() != null) {
    		resume();
        }
        else if (application.getToken() != null) {
        	final ProgressDialog dialog = ProgressDialog.show(this, "","Loading. Please wait...", true);
        	User.getCurrentUser(application, new UserCallback() {
				@Override
				public void success(User user) {
					application.setCurrentUser(user);
					dialog.dismiss();
					resume();
				}
				@Override
				public void failure(Exception e) {
					Toast.makeText(application, "LOGIN FAILURE", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					e.printStackTrace();
				}
        		
        	});
        }
        else {
    		Intent i = new Intent(this, Login.class);
    		startActivity(i);
        }
        
		Response.getPendingCount(application, new IntegerCallback() {
			@Override
			public void success(Integer data) {
				if (data > 0) {
					Button responsesButton = (Button) findViewById(R.id.responses_button);
					responsesButton.setText(getResources().getString(R.string.main_responses_title) + " (" + data + ")");
					//responsesButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
					responsesButton.getBackground().setColorFilter(Color.argb(255, 255, 128, 128),PorterDuff.Mode.MULTIPLY);
				}
			}
			@Override
			public void failure(Exception e) {
				//Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		});
    }
    
    public void onClick(View v) {
    	Intent i;
    	final PushPullApplication application = (PushPullApplication) getApplication();
    	switch (v.getId()) {
    		case R.id.responses_button:
    			i = new Intent(this, ResponseList.class);
        		startActivity(i);
    			break;
    		case R.id.options_button:
    			i = new Intent(this, Preferences.class);
        		startActivity(i);
    			break;
    		case R.id.logout_button:
    			application.setToken(null);
    			application.setCurrentUser(null);
    			GCMRegistrar.unregister(this);
        		TextView userTextView = (TextView) findViewById(R.id.user_text);
        		userTextView.setText("");
    			i = new Intent(this, Login.class);
        		startActivity(i);
        		break;
    		case R.id.polls_button:
    			i = new Intent(this, PollList.class);
        		startActivity(i);
    			break;
    		case R.id.friends_button:
    			i = new Intent(this, Friends.class);
        		startActivity(i);
    			break;
    		case R.id.website_button:
    			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://push-poll.com/"));
    			startActivity(browserIntent);
    			break;
    	}
    }
    
    private void resume() {
    	PushPullApplication application = (PushPullApplication) getApplication();
		TextView userTextView = (TextView) findViewById(R.id.user_text);
		userTextView.setText(application.getCurrentUser().getName());
		
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        //GCMRegistrar.unregister(this);
        if (regId.equals("")) {
        	GCMRegistrar.register(this, "291151327344");
        } 
        else {
        	Log.v("GCM", "Already registered");
        }
    }
}