package k11.pushpull.Data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import k11.pushpull.HTTPTask;
import k11.pushpull.PushPullApplication;
import k11.pushpull.Callbacks.FriendListCallback;
import k11.pushpull.Callbacks.HTTPCallback;

public class Friend {
	private String name;
	private String email;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void parseJSON(JSONObject jsonObject) throws JSONException {
		name = jsonObject.getString("name");
		email = jsonObject.getString("email");
	}
	
	public static void getDomesticFriends(PushPullApplication application,final FriendListCallback callback) {
		String url = application.getURL() + "/users/domestic_friends.json";
		new HTTPTask("GET", url, application.getToken()).execute(new HTTPCallback () {
			@Override
			public void success(String output) {
				try {
					ArrayList<Friend> friendList = new ArrayList<Friend>();
					try {
						JSONArray jsonArray = new JSONArray(output);
						int length = jsonArray.length();
						for (int i=0; i<length; i++) {
							Friend friend = new Friend();
							friend.parseJSON(jsonArray.getJSONObject(i));
							friendList.add(friend);
						}
						callback.success(friendList);
					} catch (Exception e) {
						callback.failure(e);
					}
				} catch (Exception e) {
					callback.failure(e);
				}
			}

			@Override
			public void failure(Exception e) {
				callback.failure(e);
			}
		});
	}
}
