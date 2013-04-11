package k11.pushpull.Data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import k11.pushpull.HTTPTask;
import k11.pushpull.PushPullApplication;
import k11.pushpull.Callbacks.HTTPCallback;
import k11.pushpull.Callbacks.IntegerCallback;
import k11.pushpull.Callbacks.ResponseCallback;
import k11.pushpull.Callbacks.ResponseListCallback;
import k11.pushpull.Callbacks.ResponsePollListCallback;
import k11.pushpull.Callbacks.VoidCallback;

public class Response {
	private Integer id;
	private Integer poll_id;
	private Integer poll_option_id;
	private String short_text;
	private String user_email;
	private Integer user_id;
	
	private void setID(Integer id) {
		this.id = id;
	}
	
	public Integer getID() {
		return id;
	}
	
	public void setPollID(Integer pollID) {
		this.poll_id = pollID;
	}
	
	public Integer getPollID() {
		return poll_id;
	}
	
	public void setPollOptionID(Integer pollOptionID) {
		this.poll_option_id = pollOptionID;
	}
	
	public Integer getPollOptionID() {
		return poll_option_id;
	}
	
	public void setShortText(String shortText) {
		if (shortText == "null") {
			this.short_text = null;
		}
		else {
			this.short_text = shortText;	
		}
	}
	
	public String getShortText() {
		return short_text;
	}
	
	public void setUserEmail(String userEmail) {
		this.user_email = userEmail;
	}
	
	public String getUserEmail() {
		return user_email;
	}
	
	private void setUserID(Integer userID) {
		this.user_id = userID;
	}
	
	public Integer getUserID() {
		return user_id;
	}
	
	private void parseJSON(JSONObject jsonObject) throws JSONException {
		setID(jsonObject.getInt("id"));
		setPollID(jsonObject.getInt("poll_id"));
		setPollOptionID(jsonObject.optInt("poll_option_id",0));
		setShortText(jsonObject.getString("short_text"));
		setUserEmail(jsonObject.getString("user_email"));
		setUserID(jsonObject.optInt("user_id"));
	}
	
	public static void get(PushPullApplication application,int id,final ResponseCallback callback) {
		String url = application.getURL() + "/responses/" + id + ".json";
		new HTTPTask("GET", url,application.getToken()).execute(new HTTPCallback () {

			@Override
			public void success(String output) {
				Response response = new Response();
				try {
					JSONObject jsonObject = new JSONObject(output);
					response.parseJSON(jsonObject);
					callback.success(response);
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
	
	public static void getPending(PushPullApplication application, final ResponsePollListCallback callback) {
		String url = application.getURL() + "/responses/pending.json";
		getPairedList(application, callback, url);
	}
	
	public static void getCompleted(PushPullApplication application, Integer limit, Integer offset, final ResponsePollListCallback callback) {
		String url = application.getURL() + "/responses/completed.json?limit=" + limit + "&offset=" + offset;
		getPairedList(application, callback, url);
	}
	
	private static void getPairedList(PushPullApplication application, final ResponsePollListCallback callback, String url) {
		new HTTPTask("GET", url,application.getToken()).execute(new HTTPCallback () {

			@Override
			public void success(String output) {
				ArrayList<Response> responseList = new ArrayList<Response>();
				ArrayList<Poll> pollList = new ArrayList<Poll>();
				try {
					JSONArray jsonArray = new JSONArray(output);
					int length = jsonArray.length();
					for (int i=0; i<length; i++) { //Outer array holds the different responses
						Response response = new Response();
						Poll poll = new Poll();
						response.parseJSON(jsonArray.getJSONObject(i));
						responseList.add(response);
						poll.parseJSON(jsonArray.getJSONObject(i).getJSONObject("poll"));
						pollList.add(poll);
					}
					callback.success(responseList, pollList);
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
	
	public static void getList(PushPullApplication application, final ResponseListCallback callback) { //TODO DEPREC
		String url = application.getURL() + "/responses.json";
		
		new HTTPTask("GET", url, application.getToken()).execute(new HTTPCallback () {

			@Override
			public void success(String output) {
				ArrayList<Response> responseList = new ArrayList<Response>();
				try {
					JSONArray jsonArray = new JSONArray(output);
					int length = jsonArray.length();
					for (int i=0; i<length; i++) {
						Response response = new Response();
						response.parseJSON(jsonArray.getJSONObject(i));
						responseList.add(response);
					}
					callback.success(responseList);
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
	
	public void put(PushPullApplication application, final VoidCallback callback) {
		String url = application.getURL() + "/responses/" + getID() + ".json";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", getID());
			jsonObject.put("poll_id",getPollID());
			if (getPollOptionID() == 0) {
				jsonObject.put("poll_option_id", JSONObject.NULL);
			}
			else {
				jsonObject.put("poll_option_id", getPollOptionID());
			}
			jsonObject.put("response_value", JSONObject.NULL);
			if (getShortText() == null) {
				jsonObject.put("short_text", JSONObject.NULL);
			}
			else {
				jsonObject.put("short_text", getShortText());
			}
			jsonObject.put("user_email", getUserEmail());
			if (getUserID() != 0) {
				jsonObject.put("user_id", getUserID());
			}
			
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
		} catch (JSONException e) {
			callback.failure(e);
		}
		
	}
	
	public void post(PushPullApplication application, final ResponseCallback callback) {
		String url = application.getURL() + "/polls/" + getPollID() + "/responses.json";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("poll_id",getPollID());
			jsonObject.put("user_email", getUserEmail());
			JSONObject jsonWrapper = new JSONObject();
			jsonWrapper.put("response", jsonObject);
			
			HTTPTask httpTask = new HTTPTask("POST",url,application.getToken());
			httpTask.setInput(jsonWrapper.toString());
			httpTask.execute(new HTTPCallback() {
				@Override
				public void success(String output) {
					try {
						JSONObject jsonObject = new JSONObject(output);
						parseJSON(jsonObject);
						callback.success(Response.this);
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
		catch (JSONException e) {
			callback.failure(e);
		}
	}
	
	public static void getPendingCount(PushPullApplication application, final IntegerCallback callback) {
		String url = application.getURL() + "/responses/pending_count.json";
		HTTPTask httpTask = new HTTPTask("GET",url,application.getToken());
		httpTask.execute(new HTTPCallback() {
			@Override
			public void success(String output) {
				try {
					callback.success(Integer.valueOf(output));
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
	
	public static void delete(PushPullApplication application, Integer responseID, final VoidCallback callback) {
		String url = application.getURL() + "/responses/" + responseID + ".json";
		HTTPTask httpTask = new HTTPTask("DELETE",url,application.getToken());
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
}
