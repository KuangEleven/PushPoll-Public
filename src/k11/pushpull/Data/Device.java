package k11.pushpull.Data;

import k11.pushpull.HTTPTask;
import k11.pushpull.PushPullApplication;
import k11.pushpull.Callbacks.HTTPCallback;
import k11.pushpull.Callbacks.VoidCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class Device {
	private String deviceID;
	private String name;
	private boolean pushEnabled;
	
	public Device() {
		pushEnabled = true;
	}
	
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	
	public String getDeviceID() {
		return deviceID;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setPushEnabled(boolean pushEnabled) {
		this.pushEnabled = pushEnabled;
	}
	
	public boolean getPushEnabled() {
		return pushEnabled;
	}
	
	private void parseJSON(JSONObject jsonObject) throws JSONException {
		setDeviceID(jsonObject.getString("device_id"));
		setName(jsonObject.getString("name"));
		setPushEnabled(jsonObject.getBoolean("push_enabled"));
	}
	
	public void post(PushPullApplication application, final VoidCallback callback) {
		String url = application.getURL() + "/devices.json";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("device_id", deviceID);
			jsonObject.put("name", name);
			jsonObject.put("push_enabled", pushEnabled);
			
			HTTPTask httpTask = new HTTPTask("POST", url, application.getToken());
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
}
