package k11.pushpull.Data;

import java.text.ParseException;

import k11.pushpull.HTTPTask;
import k11.pushpull.PushPullApplication;
import k11.pushpull.Callbacks.HTTPCallback;
import k11.pushpull.Callbacks.PollOptionCallback;
import k11.pushpull.Callbacks.VoidCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class PollOption {
	private Integer id;
	private String label;
	private Integer timesChosen;
	private Integer pollID;
	
	private void setID(Integer id) {
		this.id = id;
	}
	
	public Integer getID() {
		return id;
	}
	
	public void setLabel(String label)	{
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	
	private void setTimesChosen(Integer timesChosen) {
		this.timesChosen = timesChosen;
	}
	
	public Integer getTimesChosen() {
		return timesChosen;
	}
	
	public void setPollID(Integer pollID) {
		this.pollID = pollID;
	}
	
	public Integer getPollID() {
		return pollID;
	}
	
	public void parseJSON(JSONObject jsonObject) throws JSONException, ParseException {
		setID(jsonObject.getInt("id"));
		setLabel(jsonObject.getString("label"));
		setTimesChosen(jsonObject.optInt("times_chosen",0));
		setPollID(jsonObject.getInt("poll_id"));
	}
	
	public void parseJSONPoll(JSONObject jsonObject) throws JSONException, ParseException { //For parsing from poll json, where poll id is not included
		setID(jsonObject.getInt("id"));
		setLabel(jsonObject.getString("label"));
		setTimesChosen(jsonObject.getInt("times_chosen"));
	}
	
	public void post(PushPullApplication application, final PollOptionCallback callback) {
		String url = application.getURL() + "/polls/" + pollID + "/poll_options.json";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("poll_id",getPollID());
			jsonObject.put("label", getLabel());
			JSONObject jsonWrapper = new JSONObject();
			jsonWrapper.put("poll_option", jsonObject);
			
			HTTPTask httpTask = new HTTPTask("POST",url,application.getToken());
			httpTask.setInput(jsonWrapper.toString());
			httpTask.execute(new HTTPCallback() {
				@Override
				public void success(String output) {
					try {
						JSONObject jsonObject = new JSONObject(output);
						parseJSON(jsonObject);
						callback.success(PollOption.this);
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
	
	public void delete(PushPullApplication application, final VoidCallback callback) {
		String url = application.getURL() + "/polls/" + pollID + "/poll_options/" + id + ".json";
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
	
	public static void delete(PushPullApplication application, Integer optionID, Integer pollID, final VoidCallback callback) {
		String url = application.getURL() + "/polls/" + pollID + "/poll_options/" + optionID + ".json";
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
