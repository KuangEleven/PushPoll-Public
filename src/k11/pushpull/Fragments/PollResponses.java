package k11.pushpull.Fragments;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import k11.pushpull.HasNextTab;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Updateable;
import k11.pushpull.Callbacks.FriendListCallback;
import k11.pushpull.Callbacks.GroupListCallback;
import k11.pushpull.Callbacks.ResponseCallback;
import k11.pushpull.Callbacks.VoidCallback;
import k11.pushpull.Data.Friend;
import k11.pushpull.Data.Group;
import k11.pushpull.Data.Poll;
import k11.pushpull.Data.Response;
import k11.pushpull.HasPoll;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Entity;
import android.provider.ContactsContract.RawContactsEntity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class PollResponses extends SherlockFragment implements OnClickListener,Updateable {
	ArrayList<AutoCompleteListItem> autoCompleteList = new ArrayList<AutoCompleteListItem>();
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.pollresponses, container, false);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	update();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	if (getSherlockActivity() != null) {
	    	getSherlockActivity().findViewById(R.id.addperson_button).setOnClickListener(this);
	    	
	    	//Google Contacts
	    	Uri entityUri = RawContactsEntity.CONTENT_URI;
	    	String selection = Entity.MIMETYPE + " in ('" + StructuredName.CONTENT_ITEM_TYPE + "','" + Email.CONTENT_ITEM_TYPE + "') AND " + RawContacts.ACCOUNT_TYPE + " = 'com.google'";
	    	Cursor cursor = getSherlockActivity().getContentResolver().query(entityUri, 
	    				new String[]{RawContacts.SOURCE_ID, Entity.MIMETYPE, Entity.DATA1}, 
	    				selection, null, RawContacts.SOURCE_ID + " asc, " + Entity.MIMETYPE + " desc");
	    	
	    	String sourceId = null;
	    	String contactName = null;
	    	 try {
	    	     while (cursor.moveToNext()) {
	    	         if (!cursor.isNull(1)) {
	    	             String mimeType = cursor.getString(1);
	    	             String data = cursor.getString(2);
	    	        	 if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) { //Set contact name for to combine with later email addresses
	    	        		 contactName = data;
	    	        	 }
	    	        	 else if (mimeType.equals(Email.CONTENT_ITEM_TYPE) && cursor.getString(0).equals(sourceId)) { //Check if it is the expected contact
	    	        		 autoCompleteList.add(new AutoCompleteListItem(0,contactName,data, AutoCompleteListItem.ANDROIDCONATCT));
	    	        	 }
	    	        	 //If anything falls through here, it is wrong, so just skip it
	    	        	 sourceId = cursor.getString(0);
	    	         }
	    	     }
	    	 } finally {
	    	     cursor.close();
	    	 }
	    	
	    	final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
	    	Friend.getDomesticFriends(application, new FriendListCallback(){
				@Override
				public void success(List<Friend> output) {
					for (Friend friend : output) {
						autoCompleteList.add(new AutoCompleteListItem(0,friend.getName(),friend.getEmail(), AutoCompleteListItem.PUSHPOLLCONTACT));
					}
					Group.getGroups(application, new GroupListCallback() {
						@Override
						public void success(List<Group> output) {
							for (Group group : output) {
								autoCompleteList.add(new AutoCompleteListItem(group.getId(),group.getName(),"", AutoCompleteListItem.PUSHPOLLGROUP));
							}
							AutoCompleteAdapter adapter = new AutoCompleteAdapter(getSherlockActivity(),R.layout.autosuggestitem,autoCompleteList);
							final AutoCompleteTextView textView = (AutoCompleteTextView) getSherlockActivity().findViewById(R.id.addperson_text);
							textView.setAdapter(adapter);
						}
						@Override
						public void failure(Exception e) {
							//Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
					});
				}
				@Override
				public void failure(Exception e) {
					Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
	    	});
    	}
    }
    
    public void update() {
    	if (getSherlockActivity() != null) {
	    	Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
	    	final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
	    	if ((getView() != null) && (poll != null)) {
	            ListView lv = (ListView) getSherlockActivity().findViewById(R.id.responses_listview);
	            lv.setTextFilterEnabled(true);
	            
	            //ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
	            ArrayList<AdapterTuple> list = new ArrayList<AdapterTuple>();
	    		int length = poll.getShortResponses().size();
	    		for (int i=0; i<length; i++) {
	    	        //HashMap<String, String> maplist;
	    	        //maplist = new HashMap<String, String>();
	    			AdapterTuple tuple = new AdapterTuple("","",poll.getShortResponses().get(i).getID());
	    	        if (poll.getShortResponses().get(i).getUserName().equals("null") && poll.isOwner(application.getCurrentUser().getID())) {
	    	        	tuple.text1 = poll.getShortResponses().get(i).getUserEmail();
	    	        }
	    	        else if (poll.getShortResponses().get(i).getUserName().equals("null") && !poll.isOwner(application.getCurrentUser().getID())) {
	    	        	tuple.text1 = getString(R.string.pollresponses_nulluser);
	    	        }
	    	        else {
	        	        tuple.text1 = poll.getShortResponses().get(i).getUserName();
	    	        }
	    	        switch (poll.getPollType()) {
	    	        	case Poll.SELECT_MANY:
	    					try {
	    		        		if (poll.getShortResponses().get(i).getCleanOutput() == null) {
	    		        			tuple.text2 = getString(R.string.pollresponses_nullresponse);
	    		        		}
	    		        		else
	    		        		{
	    							JSONObject jsonObject = new JSONObject(poll.getShortResponses().get(i).getCleanOutput());
	    							tuple.text2 = jsonObject.getString("poll_option_text");
	    		        		}
	    					} catch (JSONException e) {
	    						e.printStackTrace();
	    					}
	    	        		break;
	    	        	case Poll.RSVP:
			        		if (poll.getShortResponses().get(i).getCleanOutput() == null) {
			        			tuple.text2 = getString(R.string.pollresponses_nullresponse);
			        		}
			        		else if (Integer.valueOf(poll.getShortResponses().get(i).getCleanOutput()) == 0)
			        		{
			        			tuple.text2 = getString(R.string.pollresponses_rsvpnoresponse);
			        		}
			        		else if (Integer.valueOf(poll.getShortResponses().get(i).getCleanOutput()) == 1)
			        		{
			        			tuple.text2 = getString(R.string.pollresponses_rsvpyesresponse);
			        		}
			        		else if (Integer.valueOf(poll.getShortResponses().get(i).getCleanOutput()) == 2)
			        		{
			        			tuple.text2 = getString(R.string.pollresponses_rsvpyesresponse) + " + 1 " + getString(R.string.pollresponses_rsvpguestresponse);
			        		}
			        		else if (Integer.valueOf(poll.getShortResponses().get(i).getCleanOutput()) > 2)
			        		{
			        			tuple.text2 = getString(R.string.pollresponses_rsvpyesresponse) + " + " + (Integer.valueOf(poll.getShortResponses().get(i).getCleanOutput()) - 1) + " " + getString(R.string.pollresponses_rsvpguestsresponse);
			        		}
			        		break;
	    	        	default:
	    	        		if (poll.getShortResponses().get(i).getCleanOutput() == null) {
	    	        			tuple.text2 = getString(R.string.pollresponses_nullresponse);
	    	        		}
	    	        		else
	    	        		{
	    	        			tuple.text2 = poll.getShortResponses().get(i).getCleanOutput();
	    	        		}
	    	        		break;
	    	        }
	    	        list.add(tuple);
	    		}
	    		
	    		//String[] from = { "line1", "line2" };
	    		//int[] to = { android.R.id.text1, android.R.id.text2 };
	    		
	    		//SimpleAdapter adapter = new SimpleAdapter(getSherlockActivity(), list, android.R.layout.simple_list_item_2, from, to);  
	    		//lv.setAdapter(adapter);
	    		
		        ResultsAdapter adapter = new ResultsAdapter(getSherlockActivity(),R.layout.responseslistitem,list);
		        lv.setAdapter(adapter);
	    		
	    		if (!(poll.isOwner(application.getCurrentUser().getID()))) {
	    			LinearLayout addLayout = (LinearLayout) getSherlockActivity().findViewById(R.id.responses_add);
	    			addLayout.setVisibility(View.GONE);
	    		}
	    		
	    		if (poll.getState() > Poll.DRAFT) {
	    			LinearLayout addLayout = (LinearLayout) getSherlockActivity().findViewById(R.id.responses_add);
	    			addLayout.setVisibility(View.GONE);
	    		}
	    		if (poll.getState() < Poll.ACTIVE && poll.getPollType() != Poll.SHORT_TEXT) {
	    			Button nextButton = (Button) getSherlockActivity().findViewById(R.id.next_button);
	    			nextButton.setVisibility(Button.VISIBLE);
	    			nextButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							((HasNextTab) getSherlockActivity()).nextTab();
						}
					});
	    		}
	    	}
    	}
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.addperson_button:
				EditText personText = (EditText) getSherlockActivity().findViewById(R.id.addperson_text);
				
				addPerson(personText.getText().toString());

				personText.setText("");
				break;
		}
	}
	
	private class ResultsAdapter extends ArrayAdapter<AdapterTuple> {
		private int textViewResouceId;
		private LayoutInflater layoutInflater;
		public ResultsAdapter(Context context, int textViewResourceId,List<AdapterTuple> objects) {
			super(context, textViewResourceId, objects);
			textViewResourceId = this.textViewResouceId;
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView (final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = layoutInflater.inflate(R.layout.responseslistitem, null);
			}
			
			TextView text1 = (TextView) view.findViewById(R.id.text1);
			text1.setText(getItem(position).text1);
			
			TextView text2 = (TextView) view.findViewById(R.id.text2);
			text2.setText(getItem(position).text2);
			
			final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
			Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
			if (poll.isOwner(application.getCurrentUser().getID()) && poll.getState() < Poll.ACTIVE) {
				ImageView deleteImage = (ImageView) view.findViewById(R.id.delete);
				deleteImage.setVisibility(ImageView.VISIBLE);
				deleteImage.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						Response.delete(application, getItem(position).responseID, new VoidCallback() {
							@Override
							public void success() {
								Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
								((HasPoll) getSherlockActivity()).updatePoll(poll.getID());
							}
							@Override
							public void failure(Exception e) {
								Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
								e.printStackTrace();
							}
						});
					}
				});
			}
			return view;
		}
	}
	
	private class AdapterTuple {
		public String text1;
		public String text2;
		public Integer responseID;
		
		AdapterTuple(String text1,String text2,Integer responseID) {
			this.text1 = text1;
			this.text2 = text2;
			this.responseID = responseID;
		}
	}
	
	private class AutoCompleteListItem {
		public Integer id;
		public String name;
		public String email;
		public Integer contactType;
		
		public final static int PUSHPOLLCONTACT = 0;
		public final static int PUSHPOLLGROUP = 1;
		public final static int ANDROIDCONATCT = 2;
		
		public AutoCompleteListItem(Integer id,String name,String email,Integer contactType) {
			this.id = id;
			this.name = name;
			this.email = email;
			this.contactType = contactType;
		}
		
		@Override public String toString() { //The hope is that this will let me use the default Filterable implementation of ArrayAdapters
			return name + " " + email;
		}
	}
	
	private void addPerson(String email) {
		final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
		Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
		
		if (poll.getState() == Poll.NEW) {
			Toast.makeText(application, "Poll creation requires a question", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (email.length() == 0) {
			return;
		}
		
		Response response = new Response();
		response.setPollID(poll.getID());
		response.setUserEmail(email);
		response.post(application, new ResponseCallback() {
			@Override
			public void success(Response output) {
				((HasPoll) getSherlockActivity()).updatePoll(output.getPollID());
			}
			@Override
			public void failure(Exception e) {
				Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		});
	}
	
	private void addGroup(Integer groupID) {
		final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
		final Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
		
		if (poll.getState() == Poll.NEW) {
			Toast.makeText(application, "Poll creation requires a question", Toast.LENGTH_SHORT).show();
			return;
		}
		
		poll.addGroup(application, groupID, new VoidCallback() {
			@Override
			public void success() {
				((HasPoll) getSherlockActivity()).updatePoll(poll.getID());
			}
			@Override
			public void failure(Exception e) {
				Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		});
	}
	
	private class AutoCompleteAdapter extends ArrayAdapter<AutoCompleteListItem> implements Filterable {
		private int textViewResouceId;
		private LayoutInflater layoutInflater;
		public AutoCompleteAdapter(Context context, int textViewResourceId,List<AutoCompleteListItem> objects) {
			super(context, textViewResourceId, objects);
			textViewResourceId = this.textViewResouceId;
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView (final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = layoutInflater.inflate(R.layout.autosuggestitem, null);
			}
			
			TextView text1 = (TextView) view.findViewById(R.id.text1);
			text1.setText(getItem(position).name);
			
			TextView text2 = (TextView) view.findViewById(R.id.text2);
			text2.setText(getItem(position).email);
			
			ImageView imageView = (ImageView) view.findViewById(R.id.image);
			if (getItem(position).contactType == AutoCompleteListItem.PUSHPOLLGROUP) {
				imageView.setImageResource(R.drawable.ic_menu_allfriends);
			} else {
				imageView.setImageResource(R.drawable.onefriend);
			}
			
			final AutoCompleteTextView textView = (AutoCompleteTextView) getSherlockActivity().findViewById(R.id.addperson_text);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					switch (getItem(position).contactType) {
					case AutoCompleteListItem.ANDROIDCONATCT:
						addPerson(getItem(position).email);
						textView.setText("");
						break;
					case AutoCompleteListItem.PUSHPOLLCONTACT:
						addPerson(getItem(position).email);
						textView.setText("");
						break;
					case AutoCompleteListItem.PUSHPOLLGROUP:
						addGroup(getItem(position).id);
						textView.setText("");
						break;
				}
				}
			});
			return view;
		}
	}
}
