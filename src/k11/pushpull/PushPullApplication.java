package k11.pushpull;

import k11.pushpull.Data.User;
import android.app.Application;
import android.content.SharedPreferences;

public class PushPullApplication extends Application { //Apparently I shouldn't extend Application, according to the Android API
	private User currentUser;
	//private final static String URL = "http://push-poll.com";
	private final static String URL = "https://pushpoll-beta.herokuapp.com";
	
	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}
	
	public User getCurrentUser() {
		return currentUser;
	}
	
	public String getURL() {
		return URL;
	}
	
	public void setToken(String token) {
		//this.token = token;
        SharedPreferences.Editor ed = getSharedPreferences("PushPoll",MODE_PRIVATE).edit(); //TODO Shove into an options class at some point
        ed.putString("APIKey", token);
        ed.commit();
	}
	
	public String getToken() {
		//return token;
		return getSharedPreferences("PushPoll",MODE_PRIVATE).getString("APIKey", null);
	}
}
