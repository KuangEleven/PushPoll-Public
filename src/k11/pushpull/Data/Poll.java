package k11.pushpull.Data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import k11.pushpull.HTTPTask;
import k11.pushpull.PushPullApplication;
import k11.pushpull.Callbacks.HTTPCallback;
import k11.pushpull.Callbacks.PollCallback;
import k11.pushpull.Callbacks.PollListCallback;
import k11.pushpull.Callbacks.VoidCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Poll {
	private Date endDate;
	private Integer id;
	private Boolean isActive;
	private Integer pollType;
	private String question;
	private Boolean shareResults;
	private Integer userID;
	private ArrayList<PollOption> pollSummary;
	private ArrayList<ResponseShort> shortResponses;
	
	public static final int RSVP = 1;
	public static final int SELECT_ONE = 2;
	public static final int SELECT_MANY = 3;
	public static final int SHORT_TEXT = 4;
	
	public static final int NEW = 1;
	public static final int DRAFT = 2;
	public static final int ACTIVE = 3;
	public static final int PAST = 4;
	
	public Poll() {
		pollSummary = new ArrayList<PollOption>();
		shortResponses = new ArrayList<ResponseShort>();
		id = 0;
		pollType = RSVP;
		question = "";
		shareResults = false;
		userID = 0;
		isActive = false;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.HOUR,24);
		endDate = calendar.getTime();
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	private void setID(Integer id) {
		this.id = id;
	}
	
	public Integer getID() {
		return id;
	}
	
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	
	public Boolean getIsActive() {
		return isActive;
	}
	
	public void setPollType(Integer pollType) {
		this.pollType = pollType;
	}
	
	public Integer getPollType() {
		return pollType;
	}
	
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public void setShareResults(Boolean shareResults) {
		this.shareResults = shareResults;
	}
	
	public Boolean getShareResults() {
		return shareResults;
	}
	
	private void setUserID(Integer userID) {
		this.userID = userID;
	}
	
	public Integer getUserID() {
		return userID;
	}
	
	private void addPollSummary(PollOption pollOption) {
		pollSummary.add(pollOption);
	}
	
	public ArrayList<PollOption> getPollSummary() {
		return pollSummary;
	}
	
	private void addShortResponse(ResponseShort responseShort) {
		shortResponses.add(responseShort);
	}
	
	public ArrayList<ResponseShort> getShortResponses() {
		return shortResponses;
	}
	
	public boolean isCurrent() {
		return (endDate.after(new Date()));
	}
	
	public boolean isOwner(Integer currentUserID) {
		return (userID == currentUserID || userID == 0);
	}
	
	public Integer getState() {
		if (id == 0) {
			return NEW;
		}
		else if (!(isActive)) {
			return DRAFT;
		}
		else if (isCurrent()) {
			return ACTIVE;
		}
		else {
			return PAST;
		}
	}
	
	public void parseJSON(JSONObject jsonObject) throws JSONException, ParseException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
		String dateString = jsonObject.getString("end_date").replace("Z", "GMT+00:00");
		setEndDate(dateFormat.parse(dateString));
		setID(jsonObject.getInt("id"));
		setIsActive(jsonObject.getBoolean("is_active"));
		//setName(jsonObject.getString("name"));
		setPollType(jsonObject.getInt("poll_type"));
		setQuestion(jsonObject.getString("question"));
		setShareResults(jsonObject.getBoolean("share_results"));
		setUserID(jsonObject.getInt("user_id"));
		JSONArray jsonPollSummary = jsonObject.optJSONArray("poll_summary");
		if (jsonPollSummary != null) {
			Integer arraySize = jsonPollSummary.length();
			for (Integer i = 0; i < arraySize; ++i) {
				PollOption pollOption = new PollOption();
				pollOption.parseJSONPoll(jsonPollSummary.getJSONObject(i));
				pollOption.setPollID(getID());
				addPollSummary(pollOption);
			}
		}
		JSONArray jsonResponses = jsonObject.optJSONArray("responses");
		if (jsonResponses != null) {
			Integer arraySize = jsonResponses.length();
			for (Integer i = 0; i < arraySize; ++i) {
				ResponseShort responseShort = new ResponseShort();
				responseShort.parseJSON(jsonResponses.getJSONObject(i));
				addShortResponse(responseShort);
			}
		}
	}
	
	public static void get(PushPullApplication application,int id,final PollCallback callback) {
		String url = application.getURL() + "/polls/" + id + ".json";
		new HTTPTask("GET", url, application.getToken()).execute(new HTTPCallback () {

			@Override
			public void success(String output) {
				Poll poll = new Poll();
				try {
					JSONObject jsonObject = new JSONObject(output);
					poll.parseJSON(jsonObject);
					callback.success(poll);
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
	
	public static void getDraft(PushPullApplication application, PollListCallback callback) {
		String url = application.getURL() + "/polls/draft.json";
		getList(application, callback, url);
	}
	
	public static void getActive(PushPullApplication application, PollListCallback callback) {
		String url = application.getURL() + "/polls/active.json";
		getList(application, callback, url);
	}
	
	public static void getCompleted(PushPullApplication application, Integer limit, Integer offset, PollListCallback callback) {
		String url = application.getURL() + "/polls/completed.json?limit=" + limit + "&offset=" + offset;
		getList(application, callback, url);
	}
	
	private static void getList(PushPullApplication application, final PollListCallback callback, String url) {
		new HTTPTask("GET", url, application.getToken()).execute(new HTTPCallback () {

			@Override
			public void success(String output) {
				ArrayList<Poll> pollList = new ArrayList<Poll>();
				try {
					JSONArray jsonArray = new JSONArray(output);
					int length = jsonArray.length();
					for (int i=0; i<length; i++) {
						Poll poll = new Poll();
						poll.parseJSON(jsonArray.getJSONObject(i));
						pollList.add(poll);
					}
					callback.success(pollList);
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
	
	public void post(PushPullApplication application, final PollCallback callback) {
		String url = application.getURL() + "/polls.json";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("question", question);
			jsonObject.put("poll_type", pollType);
			jsonObject.put("share_results", shareResults);
			JSONObject jsonWrapper = new JSONObject();
			jsonWrapper.put("poll", jsonObject);
			
			HTTPTask httpTask = new HTTPTask("POST",url,application.getToken());
			httpTask.setInput(jsonWrapper.toString());
			httpTask.execute(new HTTPCallback() {
				@Override
				public void success(String output) {
					try {
						JSONObject jsonObject = new JSONObject(output);
						parseJSON(jsonObject);
						callback.success(Poll.this);
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
	
	public void put(PushPullApplication application, final VoidCallback callback) {
		String url = application.getURL() + "/polls/" + id + ".json";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("question", question);
			jsonObject.put("poll_type", pollType);
			jsonObject.put("share_results", shareResults);
			JSONObject jsonWrapper = new JSONObject();
			jsonWrapper.put("poll", jsonObject);
			
			HTTPTask httpTask = new HTTPTask("PUT",url,application.getToken());
			httpTask.setInput(jsonWrapper.toString());
			httpTask.execute(new HTTPCallback() {
				@Override
				public void success(String output) {
					try {
						callback.success();
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
	
	public void activate(PushPullApplication application, final VoidCallback callback) {
		String url = application.getURL() + "/polls/" + id + "/activate.json";
		
		HTTPTask httpTask = new HTTPTask("PUT",url,application.getToken());
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
	
	public void delete(PushPullApplication application, final VoidCallback callback) {
		String url = application.getURL() + "/polls/" + id + ".json";
		
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
	
	public void addGroup(PushPullApplication application, Integer groupID, final VoidCallback callback) {
		String url = application.getURL() + "/polls/" + id + "/responses/create_from_group.json";
		
		HTTPTask httpTask = new HTTPTask("POST",url,application.getToken());
		JSONObject json = new JSONObject();
		try {
			json.put("group_id",groupID);
		} catch (JSONException e) {
			callback.failure(e);
		}
		httpTask.setInput(json.toString());
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
