package k11.pushpull.Activities;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Callbacks.UserCallback;
import k11.pushpull.Data.User;
import k11.pushpull.Fragments.NewUserDialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class NewUser extends SherlockFragmentActivity implements OnClickListener {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newuser);
        
        //Click Listeners
        findViewById(R.id.create_button).setOnClickListener(this);
    }
    
    public void onClick(View v) {
    	switch (v.getId()) {
    		case R.id.create_button:
    			final PushPullApplication application = (PushPullApplication) getApplication();
    			
    			EditText nameView = (EditText) findViewById(R.id.name_text);
    			EditText emailView = (EditText) findViewById(R.id.email_text);
    			EditText passwordView = (EditText) findViewById(R.id.password_text);
    			EditText passwordVerifyView = (EditText) findViewById(R.id.passwordverify_text);
    			
    			if (!(passwordView.getText().toString().equals(passwordVerifyView.getText().toString()))) {
    				Toast.makeText(application, "Password verification does not match password", Toast.LENGTH_SHORT).show();
    				passwordView.setText("");
    				passwordVerifyView.setText("");
    				return;
    			}
    			
    			if (passwordView.getText().length() < 6) {
    				Toast.makeText(application, "Password must be at least six characters", Toast.LENGTH_SHORT).show();
    				passwordView.setText("");
    				passwordVerifyView.setText("");
    				return;
    			}
    			
    			final ProgressDialog dialog = ProgressDialog.show(this, "","Loading. Please wait...", true);
				User user = new User();
				user.setName(nameView.getText().toString());
				user.setEmail(emailView.getText().toString());
				user.post(application, passwordView.getText().toString(), new UserCallback() {
					@Override
					public void success(User user) {
						//Toast.makeText(application, "User Successfully Created", Toast.LENGTH_SHORT).show();
						dialog.dismiss();
						DialogFragment confirmDialog = new NewUserDialogFragment();
						confirmDialog.show(NewUser.this.getSupportFragmentManager(), "NewUserDialogFragment");
						//finish();
					}
					@Override
					public void failure(Exception e) {
						Toast.makeText(application, "User Creation Failed", Toast.LENGTH_SHORT).show();
						dialog.dismiss();
						e.printStackTrace();
					}
				});
    			break;
    	}
    }

}
