package k11.pushpull.Data;

import org.json.JSONException;
import org.json.JSONObject;

public class ResponseShort {
	private Integer id;
	private String userEmail;
	private String cleanOutput;
	private String userName;
	private Integer pollOptionID;
	
	public void setID(Integer id) {
		this.id = id;
	}
	
	public Integer getID() {
		return id;
	}
	
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	
	public String getUserEmail() {
		return userEmail;
	}
	
	public void setCleanOutput(String cleanOutput) {
		if (cleanOutput == "null") {
			this.cleanOutput = null;
		}
		else {
			this.cleanOutput = cleanOutput;	
		}
	}
	
	public String getCleanOutput() {
		return cleanOutput;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setPollOptionID(Integer pollOptionID) {
		this.pollOptionID = pollOptionID;
	}
	
	public Integer getPollOptionID() {
		return pollOptionID;
	}
	
	public void parseJSON(JSONObject jsonObject) throws JSONException {
		setID(jsonObject.getInt("id"));
		setUserEmail(jsonObject.getString("user_email"));
		setCleanOutput(jsonObject.getString("clean_output"));
		setUserName(jsonObject.getString("user_name"));
		setPollOptionID(jsonObject.optInt("poll_option_id",0));
	}
}
