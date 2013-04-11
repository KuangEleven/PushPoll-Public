package k11.pushpull.Data;

import java.util.ArrayList;
import java.util.List;

import k11.pushpull.HTTPTask;
import k11.pushpull.PushPullApplication;
import k11.pushpull.Callbacks.GroupCallback;
import k11.pushpull.Callbacks.GroupListCallback;
import k11.pushpull.Callbacks.HTTPCallback;
import k11.pushpull.Callbacks.VoidCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Group {
	private Integer id;
	private String name;
	private Integer size;
	private List<GroupMember> members;
	
	public Group(){
		members = new ArrayList<GroupMember>();
		id = 0;
		name = "";
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getId() {
		return id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public Integer getSize() {
		return size;
	}
	public void setMembers(List<GroupMember> members) {
		this.members = members;
	}
	public List<GroupMember> getMembers() {
		return members;
	}
	
	private void parseJSON(JSONObject jsonObject) throws JSONException {
		id = jsonObject.getInt("id");
		name = jsonObject.getString("name");
		if (jsonObject.optInt("size",0) > 0) {
			size = jsonObject.getInt("size");
		}
		else {
			JSONArray jsonMembers = jsonObject.optJSONArray("members");
			if (jsonMembers != null) {
				size = jsonMembers.length();
				for (Integer i = 0; i < size; ++i) {
					GroupMember member = new GroupMember();
					jsonMembers.getJSONObject(i).put("group_id",getId()); //The json as returned from the server does not include the group id.
					member.parseJSON(jsonMembers.getJSONObject(i));
					members.add(member);
				}
			}
		}
	}
	
	public static void get(PushPullApplication application,int id,final GroupCallback callback) {
		String url = application.getURL() + "/groups/" + id + ".json";
		new HTTPTask("GET", url, application.getToken()).execute(new HTTPCallback () {

			@Override
			public void success(String output) {
				Group group = new Group();
				try {
					JSONObject jsonObject = new JSONObject(output);
					group.parseJSON(jsonObject);
					callback.success(group);
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
	
	public static void getGroups(PushPullApplication application,final GroupListCallback callback) {
		String url = application.getURL() + "/groups.json";
		new HTTPTask("GET", url, application.getToken()).execute(new HTTPCallback () {
			@Override
			public void success(String output) {
				try {
					ArrayList<Group> groupList = new ArrayList<Group>();
					try {
						JSONArray jsonArray = new JSONArray(output);
						int length = jsonArray.length();
						for (int i=0; i<length; i++) {
							Group group = new Group();
							group.parseJSON(jsonArray.getJSONObject(i));
							groupList.add(group);
						}
						callback.success(groupList);
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
	
	public void put(PushPullApplication application, final VoidCallback callback) {
		String url = application.getURL() + "/groups/" + getId() + ".json";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", getId());
			jsonObject.put("name",getName());
			
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
	
	public void delete(PushPullApplication application, final VoidCallback callback) {
		String url = application.getURL() + "/groups/" + id + ".json";
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
	
	public void post(PushPullApplication application, final GroupCallback callback) {
		String url = application.getURL() + "/groups.json";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("name",getName());
			
			HTTPTask httpTask = new HTTPTask("POST", url, application.getToken());
			httpTask.setInput(jsonObject.toString().trim());
			
			httpTask.execute(new HTTPCallback() {
				@Override
				public void success(String output) {
					try {
						JSONObject jsonObject = new JSONObject(output);
						parseJSON(jsonObject);
						callback.success(Group.this);
					} catch (Exception e) {
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
}
