package k11.pushpull.Activities;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import k11.pushpull.HTTPTask;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Callbacks.HTTPCallback;
import k11.pushpull.Callbacks.TokenCallback;
import k11.pushpull.Callbacks.UserCallback;
import k11.pushpull.Data.User;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity implements OnClickListener {
	static final private int DIALOG_ACCOUNTS = 1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        //Click Listeners
        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.newuser_button).setOnClickListener(this);
        findViewById(R.id.oauth_button).setOnClickListener(this);
    }

    @SuppressWarnings("deprecation") //Because I'm a terrible person
	public void onClick(View v) {
    	Intent i;
    	switch (v.getId()) {
    		case R.id.login_button:
    			EditText emailView = (EditText) findViewById(R.id.email_text);
    			EditText passwordView = (EditText) findViewById(R.id.password_text);
    			final PushPullApplication application = (PushPullApplication) getApplication();
    			final ProgressDialog dialog = ProgressDialog.show(this, "","Loading. Please wait...", true);
    			User.getToken(application, emailView.getText().toString(), passwordView.getText().toString(), new TokenCallback() {
    				@Override 
    				public void success (String token) {
						application.setToken(token);
						User.getCurrentUser(application, new UserCallback() {
							@Override
							public void success(User user) {
								application.setCurrentUser(user);
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
    				}
    				
    				@Override 
    				public void failure (Exception e) {
    					Toast.makeText(application, "Login Failure", Toast.LENGTH_SHORT).show();
    					dialog.dismiss();
    					e.printStackTrace();
    				}
    			});
    			break;
    		case R.id.newuser_button:
        		i = new Intent(this, NewUser.class);
        		startActivity(i);
        		break;
    		case R.id.oauth_button:
    			AccountManager am = AccountManager.get(this);
    			Account[] accounts = am.getAccountsByType("com.google");
    			
    			am.invalidateAuthToken("com.google", null);
    			if (accounts.length > 1) {
    				showDialog(DIALOG_ACCOUNTS);
    			}
    			else {
    				oauthLogin(accounts[0]);
    			}
    			break;
    	}
    }
    
    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            // Get the result of the operation from the AccountManagerFuture.
        	final PushPullApplication application = (PushPullApplication) getApplication();
            Bundle bundle = null;
			try {
				bundle = result.getResult();
			} catch (OperationCanceledException e) {
				Toast.makeText(Login.this, "Operation Canceled", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				Toast.makeText(Login.this, "Authenticator Failure", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(Login.this, "Network Error", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
        
            // The token is a named value in the bundle. The name of the value
            // is stored in the constant AccountManager.KEY_AUTHTOKEN.
			
            String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            
            String url = application.getURL() + "/users/signin_with_google_token.json";
            HTTPTask httpTask = new HTTPTask("POST",url,"");
            JSONObject jsonObject = new JSONObject();
            try {
            	jsonObject.put("token", token);
            }
            catch (JSONException e) {
				Toast.makeText(Login.this, "Authenticator Failure (JSON)", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
            }
            
            final ProgressDialog dialog = ProgressDialog.show(Login.this, "","Loading. Please wait...", true);
            httpTask.setInput(jsonObject.toString());
            httpTask.execute(new HTTPCallback() {
				@Override
				public void success(String output) {
					try {
						JSONObject jsonObject = new JSONObject(output);
						application.setToken(jsonObject.getString("response"));
						final Boolean newUser = jsonObject.optBoolean("new_user",false);
						if (newUser) {
			        		Intent i = new Intent(Login.this, OauthNewUser.class);
			        		startActivity(i);
			        		finish();
						}
						else {
							User.getCurrentUser(application, new UserCallback() {
								@Override
								public void success(User user) {
									application.setCurrentUser(user);
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
						}
					} catch (JSONException e) {
						Toast.makeText(Login.this, "Authenticator Failure (JSON)", Toast.LENGTH_SHORT).show();
						dialog.dismiss();
						e.printStackTrace();
					}
				}
				@Override
				public void failure(Exception e) {
					Toast.makeText(Login.this, "Network Error", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					e.printStackTrace();
				}
            });
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
      switch (id) {
        case DIALOG_ACCOUNTS:
          AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle("Select a Google account");
          final Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
          final int size = accounts.length;
          String[] names = new String[size];
          for (int i = 0; i < size; i++) {
            names[i] = accounts[i].name;
          }
          builder.setItems(names, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              oauthLogin(accounts[which]);
            }
          });
          return builder.create();
      }
      return null;
    }
    
    private void oauthLogin(Account account) {
    	AccountManager am = AccountManager.get(this);
    	Bundle options = new Bundle();
		am.getAuthToken(
			    account,                     // Account retrieved using getAccountsByType()
			    "oauth2:https://www.googleapis.com/auth/userinfo.email",            // Auth scope
			    options,                        // Authenticator-specific options
			    this,                           // Your activity
			    new OnTokenAcquired(),          // Callback called when a token is successfully acquired
			    null);
			    //new Handler(new OnError()));    // Callback called if an error occurs
    }
}
