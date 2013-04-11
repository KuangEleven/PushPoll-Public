package k11.pushpull.Data;

import org.json.JSONException;
import org.json.JSONObject;

import k11.pushpull.HTTPTask;
import k11.pushpull.PushPullApplication;
import k11.pushpull.Callbacks.HTTPCallback;
import k11.pushpull.Callbacks.TokenCallback;
import k11.pushpull.Callbacks.UserCallback;
import k11.pushpull.Callbacks.VoidCallback;

public class User {
	private Integer id;
	private String email;
	private String name;
	
	private void setID(Integer id) {
		this.id = id;
	}
	
	public Integer getID() {
		return id;
	}	
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static void getToken(PushPullApplication application, String email, String password, final TokenCallback callback) {
		String url = application.getURL() + "/tokens.json";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("email", email);
			jsonObject.put("password", password);
			HTTPTask httpTask = new HTTPTask("POST", url, "");
			httpTask.setInput(jsonObject.toString().trim());
			//Log.d("URL",url);
			//Log.d("json",jsonObject.toString().trim());
			httpTask.execute(new HTTPCallback () {

				@Override
				public void success(String output) {
					//JSON extraction
					JSONObject jsonObject;
					try {
						jsonObject = new JSONObject(output);
						callback.success(jsonObject.getString("token"));
					} catch (JSONException e) {
						callback.failure(e);
					}
				}

				@Override
				public void failure(Exception e) {
					callback.failure(e);
				}
				
			});
		} 
		catch (JSONException e) {
				callback.failure(e);
		}
	}
	
	private void parseJSON(JSONObject jsonObject) throws JSONException {
		setID(jsonObject.getInt("id"));
		setEmail(jsonObject.getString("email"));
		setName(jsonObject.getString("name"));
	}
	
	public static void getCurrentUserOld(PushPullApplication application, final UserCallback callback) {
		//I don't think this is neccesary anymore
		String url = application.getURL() + "/users/sign_in.json";
		//new HTTPTask("POST",url,application.getToken()).execute(new HTTPCallback() { //This call cannot use the standard header token, as it is baked into a module in the backend. Using the backup json approach instead, as no other input is used for this call.
		HTTPTask httpTask = new HTTPTask("POST",url,"");
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("auth_token", application.getToken());
		} catch (JSONException e) {
			e.printStackTrace();
			callback.failure(e);
		}
		httpTask.setInput(jsonObject.toString());
		httpTask.execute(new HTTPCallback() {
			@Override
			public void success(String output) {
				try {
					User user = new User();
					JSONObject jsonObject = new JSONObject(output);
					user.parseJSON(jsonObject);
					callback.success(user);
				} catch (JSONException e) {
					callback.failure(e);
				}
				
			}

			@Override
			public void failure(Exception e) {
				callback.failure(e);
			}
			
		});
	}
	
	public static void getCurrentUser(PushPullApplication application, final UserCallback callback) {
		String url = application.getURL() + "/users/info.json";
		HTTPTask httpTask = new HTTPTask("GET",url,application.getToken());
		httpTask.execute(new HTTPCallback() {
			@Override
			public void success(String output) {
				try {
					User user = new User();
					JSONObject jsonObject = new JSONObject(output);
					user.parseJSON(jsonObject);
					callback.success(user);
				} catch (JSONException e) {
					callback.failure(e);
				}
				
			}

			@Override
			public void failure(Exception e) {
				callback.failure(e);
			}
			
		});
	}
	
	public void post(PushPullApplication application, String password, final UserCallback callback) {
		String url = application.getURL() + "/users.json";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("email",email);
			jsonObject.put("name",name);
			jsonObject.put("password",password);
			jsonObject.put("password_confirmation",password); //Handle confirmation at UI level
			JSONObject jsonWrapper = new JSONObject();
			jsonWrapper.put("user", jsonObject);
			
			HTTPTask httpTask = new HTTPTask("POST",url,"");
			httpTask.setInput(jsonWrapper.toString());
			httpTask.execute(new HTTPCallback() {
				@Override
				public void success(String output) {
					try {
						JSONObject jsonObject = new JSONObject(output);
						parseJSON(jsonObject);
						callback.success(User.this);
					} catch (JSONException e) {
						callback.failure(e);
					}
				}
				@Override
				public void failure(Exception e) {
					callback.failure(e);
				}
			});
		} catch (JSONException e) {
			callback.failure(e);
		}
		
	}
	
	/*
	public void put(PushPullApplication application, final VoidCallback callback) {
		String url = application.getURL() + "/users/" + getID() + ".json?auth_token=" + application.getToken();
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", id);
			jsonObject.put("email", email);
			jsonObject.put("name", name);
			
			HTTPTask httpTask = new HTTPTask("PUT", url);
			httpTask.setInput(jsonObject.toString().trim());
			
			httpTask.execute(new HTTPCallback() {
				@Override
				public void success(String output) {
					callback.success();
				}
				@Override
				public void failure(Exception e) {
					callback.failure(e);
				}
			});
		}
		catch (JSONException e) {
			callback.failure(e);
		}
	}
	*/
	
	public void updateCurrentName(PushPullApplication application, final VoidCallback callback) {
		String url = application.getURL() + "/users/update_name.json";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("name", name);
			
			HTTPTask httpTask = new HTTPTask("PUT", url, application.getToken());
			httpTask.setInput(jsonObject.toString().trim());
			
			httpTask.execute(new HTTPCallback() {
				@Override
				public void success(String output) {
					callback.success();
				}
				@Override
				public void failure(Exception e) {
					callback.failure(e);
				}
			});
		}
		catch (JSONException e) {
			callback.failure(e);
		}
	}

}
