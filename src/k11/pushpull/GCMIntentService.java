package k11.pushpull;

import k11.pushpull.Activities.ResponseView;
import k11.pushpull.Callbacks.VoidCallback;
import k11.pushpull.Data.Device;
import k11.pushpull.Keys;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {
Handler handler;
	
    public GCMIntentService() {
    	super(Keys.GCM_KEY);
    	handler = new Handler();
    }
    
    @Override
    protected void onRegistered(final Context context, String registrationId) {
    	Log.d("GCM",registrationId);
    	PushPullApplication application = (PushPullApplication) getApplication();
    	Device device = new Device();
    	device.setDeviceID(registrationId);
    	device.setName(Build.MODEL);
    	device.post(application, new VoidCallback() {
			@Override
			public void success() {
				//Do nothing
			}
			@Override
			public void failure(Exception e) {
				e.printStackTrace();
				Toast.makeText(context, "Device Registration Error", Toast.LENGTH_SHORT).show();
			}
    	});
    }
    
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            //Add in device DELETE command
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            //Log.i(TAG, "Ignoring unregister callback");
        }
    }
    
    @Override
    public void onError(Context context, String errorId) {
    	Log.d("GCM", "Error: " + errorId);
    	//Toast.makeText(context, "Device Registration Error", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onRecoverableError(Context context, java.lang.String errorId) {
    	Log.d("GCM", "Recoverable Error: " + errorId);
    	return true;
    }
    
    @Override
    protected void onMessage(Context context, Intent intent) {
    	//Log.d("GCM",intent.getExtras().getString("message"));
    	//Toast.makeText(context, intent.getExtras().getString("message"), Toast.LENGTH_SHORT).show();
    	//showToast(intent.getExtras().getString("message"),context);
    	if (!(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_notifications", true))) {
    		return;
    	}
    	GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();

        // Start the tracker in manual dispatch mode...
        tracker.startNewSession(Keys.ANALYTICS_KEY, this);
        
    	tracker.trackEvent("Notifications","Response","received",0);
    	
    	tracker.dispatch();
    	tracker.stopSession();
    	
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

    	Notification notification = new Notification();
    	notification.when = System.currentTimeMillis();
    	notification.tickerText = "New PushPoll Request";
    	notification.icon = android.R.drawable.ic_menu_send;
    	notification.flags = Notification.FLAG_AUTO_CANCEL;
    	
    	CharSequence contentTitle = intent.getExtras().getString("poll_owner_name") + " asks";
    	CharSequence contentText = intent.getExtras().getString("poll_question");
    	
		final PushPullApplication application = (PushPullApplication) getApplication();
		Intent i = new Intent(application, ResponseView.class);
		Bundle bundle = new Bundle();
		bundle.putInt("ResponseID", Integer.valueOf(intent.getExtras().getString("response_id")));
		Log.d("NOTIFICATION",intent.getExtras().getString("response_id"));
		i.putExtras(bundle);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    	
    	final int MESSAGE_ID = 1;

    	mNotificationManager.notify(MESSAGE_ID, notification);
    }
    
    public void showToast(String message, Context context){
        handler.post(new DisplayToast(message, context));
    }

	private class DisplayToast implements Runnable {
	          String mText;
	          Context mContext;
	
	  public DisplayToast(String text, Context context){
	            mText = text;
	            mContext = context;
	          }
	
	   public void run(){
	            Toast.makeText(mContext, mText, Toast.LENGTH_LONG).show();
	          }
	}

}
