package k11.pushpull.Data;

import k11.pushpull.HTTPTask;
import k11.pushpull.PushPullApplication;
import k11.pushpull.Callbacks.GroupMemberCallback;
import k11.pushpull.Callbacks.HTTPCallback;
import k11.pushpull.Callbacks.VoidCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class GroupMember {
	private Integer id;
	private Integer groupID;
	private Integer userID;
	private String name;
	private String email;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getGroupID() {
		return groupID;
	}
	public void setGroupID(Integer groupID) {
		this.groupID = groupID;
	}
	public Integer getUserID() {
		return userID;
	}
	public void setUserID(Integer userID) {
		this.userID = userID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public void parseJSON(JSONObject jsonObject) throws JSONException {
		setName(blankNullString(jsonObject, "name"));
		setEmail(blankNullString(jsonObject, "email"));
		setId(jsonObject.getInt("id"));
		setGroupID(jsonObject.getInt("group_id"));
		setUserID(jsonObject.optInt("user_id",0));
	}
	
	private String blankNullString(JSONObject json, String name) throws JSONException {
		if (json.isNull(name)) {
			return "";
		}
		else {
			return json.getString(name);
		}
	}
	
	public void post(PushPullApplication application, final GroupMemberCallback callback) {
		String url = application.getURL() + "/group_members.json";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("group_id", groupID);
			jsonObject.put("user_email", email);
			
			HTTPTask httpTask = new HTTPTask("POST",url,application.getToken());
			httpTask.setInput(jsonObject.toString());
			httpTask.execute(new HTTPCallback() {
				@Override
				public void success(String output) {
					try {
						JSONObject jsonObject = new JSONObject(output);
						parseJSON(jsonObject);
						callback.success(GroupMember.this);
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
		String url = application.getURL() + "/group_members/" + id + ".json";
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
