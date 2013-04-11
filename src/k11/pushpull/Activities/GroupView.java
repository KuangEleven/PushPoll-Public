package k11.pushpull.Activities;

import java.util.ArrayList;
import java.util.List;

import k11.pushpull.Keys;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Callbacks.FriendListCallback;
import k11.pushpull.Callbacks.GroupCallback;
import k11.pushpull.Callbacks.GroupMemberCallback;
import k11.pushpull.Callbacks.VoidCallback;
import k11.pushpull.Data.Friend;
import k11.pushpull.Data.Group;
import k11.pushpull.Data.GroupMember;
import k11.pushpull.Fragments.DeleteDialogFragment;
import k11.pushpull.Fragments.NameDialogFragment;
import k11.pushpull.Fragments.DeleteDialogFragment.DeleteDialogListener;
import k11.pushpull.Fragments.NameDialogFragment.NameDialogListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts.Entity;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.gcm.GCMRegistrar;

public class GroupView extends SherlockFragmentActivity implements OnClickListener, NameDialogListener, DeleteDialogListener {
	GoogleAnalyticsTracker tracker;
	Group group;
	ArrayList<AutoCompleteListItem> autoCompleteList = new ArrayList<AutoCompleteListItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group);
        
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.startNewSession(Keys.ANALYTICS_KEY, this);
        
	    ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayShowTitleEnabled(true);
	    actionBar.setDisplayHomeAsUpEnabled(true);
        
        findViewById(R.id.addperson_button).setOnClickListener(this);
        findViewById(R.id.title_button).setOnClickListener(this);
        
        final Bundle bundle = getIntent().getExtras();
        if (savedInstanceState != null) { //If we are resuming from a saved instance (usually orientation change), attempt to stuff in group ID if it exists
        	bundle.putInt("GroupID", savedInstanceState.getInt("GroupID", 0));
        }
        
        if ((bundle.getInt("GroupID", 0) == 0)) {
        	group = new Group();
			DialogFragment dialog = new NameDialogFragment();
			dialog.show(getSupportFragmentManager(), "NameDialogFragment");
        }
        else {
        	getGroup(bundle.getInt("GroupID"));
        }
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	//Google Contacts
    	Uri entityUri = RawContactsEntity.CONTENT_URI;
    	String selection = Entity.MIMETYPE + " in ('" + StructuredName.CONTENT_ITEM_TYPE + "','" + Email.CONTENT_ITEM_TYPE + "') AND " + RawContacts.ACCOUNT_TYPE + " = 'com.google'";
    	Cursor cursor = getContentResolver().query(entityUri, 
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
    	
    	final PushPullApplication application = (PushPullApplication) getApplication();
    	Friend.getDomesticFriends(application, new FriendListCallback(){
			@Override
			public void success(List<Friend> output) {
				for (Friend friend : output) {
					autoCompleteList.add(new AutoCompleteListItem(0,friend.getName(),friend.getEmail(), AutoCompleteListItem.PUSHPOLLCONTACT));
				}
				AutoCompleteAdapter adapter = new AutoCompleteAdapter(GroupView.this,R.layout.autosuggestitem,autoCompleteList);
				final AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.addperson_text);
				textView.setAdapter(adapter);
			}
			@Override
			public void failure(Exception e) {
				Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
    	});
    }
    
    @Override
    public void onDestroy() {
        GCMRegistrar.onDestroy(this);
        tracker.stopSession();
        super.onDestroy();
    }
    
    @Override
    public void onStop() {
    	tracker.dispatch();
    	super.onStop();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	tracker.trackPageView("/group");
    }

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.title_button:
			DialogFragment dialog = new NameDialogFragment();
			dialog.show(getSupportFragmentManager(), "NameDialogFragment");
			break;
		case R.id.addperson_button:
			GroupMember member = new GroupMember();
			member.setGroupID(group.getId());
			final EditText personView = (EditText) findViewById(R.id.addperson_text);
			addPerson(personView.getText().toString());
			personView.setText("");
			break;
		}
	}
	
	private void addPerson(String email) {
		GroupMember member = new GroupMember();
		member.setGroupID(group.getId());
		member.setEmail(email);
		final PushPullApplication application = (PushPullApplication) this.getApplication();
		member.post(application, new GroupMemberCallback() {
			@Override
			public void success(GroupMember output) {
				getGroup(group.getId());
			}
			@Override
			public void failure(Exception e) {
				Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		});
	}
	
	private void getGroup(Integer groupID) {
		final PushPullApplication application = (PushPullApplication) this.getApplication();
    	Group.get(application, groupID, new GroupCallback(){
			@Override
			public void success(Group output) {
				GroupView.this.group = output;
				refresh();
			}
			@Override
			public void failure(Exception e) {
				Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
    	});
	}
	
	private void refresh() {
		TextView title = (TextView) findViewById(R.id.group_title);
		title.setText(group.getName());
		
		ListView lv = (ListView) findViewById(R.id.list);
        GroupAdapter adapter = new GroupAdapter(this,R.layout.friendlistitem,group.getMembers());
		lv.setAdapter(adapter);
	}
	
	private class GroupAdapter extends ArrayAdapter<GroupMember> {
		private int textViewResouceId;
		private LayoutInflater layoutInflater;
		public GroupAdapter(Context context, int textViewResourceId,List<GroupMember> objects) {
			super(context, textViewResourceId, objects);
			textViewResourceId = this.textViewResouceId;
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView (int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = layoutInflater.inflate(R.layout.friendlistitem, null);
			}
			
			TextView textView = (TextView) view.findViewById(R.id.caption);
			final GroupMember member = getItem(position);
        	if (member.getName().length() > 0) {
        		textView.setText(member.getName());
        	}
        	else {
        		textView.setText(member.getEmail());
        	}
			
			ImageView delete = (ImageView) view.findViewById(R.id.delete);
			final PushPullApplication application = (PushPullApplication) GroupView.this.getApplication();
			delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					member.delete(application, new VoidCallback(){
						@Override
						public void success() {
							getGroup(group.getId());
						}
						@Override
						public void failure(Exception e) {
							Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
					});
				}
			});
			
			return view;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, Friends.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.menu_delete:
				DialogFragment dialog = new DeleteDialogFragment();
				dialog.show(getSupportFragmentManager(), "DeleteDialogFragment");
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onNameDialogPositiveClick(DialogFragment dialog, String name) {
		group.setName(name);
		final ProgressDialog progressDialog = ProgressDialog.show(this, "","Loading. Please wait...", true);
		final PushPullApplication application = (PushPullApplication) this.getApplication();
		if (group.getId() == 0) {
			group.post(application, new GroupCallback() {
				@Override
				public void success(Group group) {
					GroupView.this.group = group;
					refresh();
					progressDialog.dismiss();
				}
				@Override
				public void failure(Exception e) {
					Toast.makeText(application, "Network Error", Toast.LENGTH_SHORT).show();
					progressDialog.dismiss();
					e.printStackTrace();
				}
			});
		}
		else {
			group.put(application, new VoidCallback() {
				@Override
				public void success() {
					getGroup(group.getId());
					progressDialog.dismiss();
				}
				@Override
				public void failure(Exception e) {
					Toast.makeText(application, "Network Error", Toast.LENGTH_SHORT).show();
					progressDialog.dismiss();
					e.printStackTrace();
				}
			});
		}
	}
	
	@Override
	public void onNameDialogNegativeClick(DialogFragment dialog) {
		if (group.getId() == 0) {
			finish();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.groupview, menu);
	    return true;
	}

	@Override
	public void onDeleteDialogPositiveClick(DialogFragment dialog) {
		final PushPullApplication application = (PushPullApplication) this.getApplication();
		group.delete(application, new VoidCallback() {
			@Override
			public void success() {
				finish();
			}
			@Override
			public void failure(Exception e) {
				Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		});
	}

	@Override
	public void onDeleteDialogNegativeClick(DialogFragment dialog) {
		//Do Nothing
	}
	
    @Override
    public void onSaveInstanceState (Bundle outState) {
    	if (group.getId() != 0) {
    		outState.putInt("GroupID", group.getId());
    	}
	    super.onSaveInstanceState(outState);
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
			imageView.setImageResource(R.drawable.onefriend);
			
			final AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.addperson_text);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					switch (autoCompleteList.get(position).contactType) {
					case AutoCompleteListItem.ANDROIDCONATCT:
						addPerson(getItem(position).email);
						textView.setText("");
						break;
					case AutoCompleteListItem.PUSHPOLLCONTACT:
						addPerson(getItem(position).email);
						textView.setText("");
						break;
					case AutoCompleteListItem.PUSHPOLLGROUP:
						break;
				}
				}
			});
			return view;
		}
	}
}
