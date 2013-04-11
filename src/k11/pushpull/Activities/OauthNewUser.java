package k11.pushpull.Activities;

import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Callbacks.UserCallback;
import k11.pushpull.Callbacks.VoidCallback;
import k11.pushpull.Data.User;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class OauthNewUser extends Activity implements OnClickListener {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauthnewuser);
        
        //Click Listeners
        findViewById(R.id.create_button).setOnClickListener(this);
    }
	
	@Override
	public void onClick(View v) {
    	switch (v.getId()) {
			case R.id.create_button:
				final PushPullApplication application = (PushPullApplication) getApplication();
				final EditText nameView = (EditText) findViewById(R.id.name_text);
				
				final ProgressDialog dialog = ProgressDialog.show(this, "","Loading. Please wait...", true);
				
				User.getCurrentUser(application, new UserCallback() {
					@Override
					public void success(User user) {
						application.setCurrentUser(user);
						user.setName(nameView.getText().toString());
						user.updateCurrentName(application, new VoidCallback() {
							@Override
							public void success() {
								dialog.dismiss();
								finish();
							}
							@Override
							public void failure(Exception e) {
								Toast.makeText(application, "Network Error", Toast.LENGTH_SHORT).show();
								dialog.dismiss();
								e.printStackTrace();
							}
						});
						
						//dialog.dismiss();
						//finish();
					}
					@Override
					public void failure(Exception e) {
						Toast.makeText(application, "Network Error", Toast.LENGTH_SHORT).show();
						dialog.dismiss();
						e.printStackTrace();
					}
				});
				break;
    	}
	}
}
