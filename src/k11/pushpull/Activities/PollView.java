package k11.pushpull.Activities;

import k11.pushpull.HasNextTab;
import k11.pushpull.HasPoll;
import k11.pushpull.Keys;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.TabListener;
import k11.pushpull.Updateable;
import k11.pushpull.Callbacks.PollCallback;
import k11.pushpull.Callbacks.UserCallback;
import k11.pushpull.Callbacks.VoidCallback;
import k11.pushpull.Data.Poll;
import k11.pushpull.Data.User;
import k11.pushpull.Fragments.NameDialogFragment;
import k11.pushpull.Fragments.PollDialogFragment;
import k11.pushpull.Fragments.PollDialogFragment.PollDialogListener;
import k11.pushpull.Fragments.PollOptions;
import k11.pushpull.Fragments.PollProperties;
import k11.pushpull.Fragments.PollRSVPSummary;
import k11.pushpull.Fragments.PollResponses;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class PollView extends SherlockFragmentActivity implements HasPoll,HasNextTab,PollDialogListener {
	GoogleAnalyticsTracker tracker;
	private Poll poll;
	private Tab propertiesTab;
	private Tab responsesTab;
	private Tab optionsTab;
	private Tab rsvpSummaryTab;
	//private Set<String> updateSet;
	private static final int DIALOG_DELETE_ID = 0;
	private Integer savedTabIndex = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    if (getSupportFragmentManager().findFragmentById(android.R.id.content) != null) {
	    	FragmentManager fragMgr = getSupportFragmentManager();
	    	FragmentTransaction ft = fragMgr.beginTransaction();
            ft.detach(getSupportFragmentManager().findFragmentById(android.R.id.content));
            ft.commit();
	    }
	    
	    final Bundle bundle = getIntent().getExtras();
	    
	    if (savedInstanceState != null) {
		    savedTabIndex = savedInstanceState.getInt("CurrentTab",-1);
		    if (bundle.getInt("PollID", 0) == 0) { //Previously New Poll
		    	bundle.putInt("PollID", savedInstanceState.getInt("PollID"));
		    }
	    }
	    
        tracker = GoogleAnalyticsTracker.getInstance();

        // Start the tracker in manual dispatch mode...
        tracker.startNewSession(Keys.ANALYTICS_KEY, this);
	    
	    // setup action bar for tabs
	    final ActionBar actionBar = getSupportActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(true);
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    propertiesTab = actionBar.newTab()
        .setText(R.string.pollproperties_tabtitle)
        .setTabListener(new TabListener<PollProperties>(this, "pollproperties", PollProperties.class));
	    actionBar.addTab(propertiesTab);
	    
	    responsesTab = actionBar.newTab()
        .setText(R.string.pollresponses_tabtitle)
        .setTabListener(new TabListener<PollResponses>(this, "pollresponses", PollResponses.class));
	    actionBar.addTab(responsesTab);
	    
	    optionsTab = actionBar.newTab()
        .setText(R.string.polloptions_tabtitle)
        .setTabListener(new TabListener<PollOptions>(this, "polloptions", PollOptions.class));
	    
	    rsvpSummaryTab = actionBar.newTab()
        .setText(R.string.pollrsvpsummary_tabtitle)
        .setTabListener(new TabListener<PollRSVPSummary>(
                this, "pollrsvpsummary", PollRSVPSummary.class));
	    
	    
	    final PushPullApplication application = (PushPullApplication) getApplication();
	    if (bundle.getInt("PollID", 0) == 0) { //New Poll
			DialogFragment dialog = new PollDialogFragment();
			dialog.show(getSupportFragmentManager(), "PollDialogFragment");
	    }
	    else {
	    	if (application.getCurrentUser() == null) {
	    		User.getCurrentUser(application, new UserCallback() {
					@Override
					public void success(User user) {
						application.setCurrentUser(user);
						updatePoll(bundle.getInt("PollID"),true);
					}
					@Override
					public void failure(Exception e) {
						Toast.makeText(application, "AUTHENTICATION FAILURE", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
	    		});
	    	}
	    	else {
	    		updatePoll(bundle.getInt("PollID"),true);
	    	}
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.pollview, menu);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (poll != null) {
			if (poll.getState() > Poll.DRAFT) {
				menu.findItem(R.id.menu_activate).setVisible(false);
			}
			else {
				menu.findItem(R.id.menu_activate).setVisible(true);
			}
		}
		return true;
	}
	
	public void updatePoll(Integer pollID) {
		updatePoll(pollID,false);
	}
	
	public void updatePoll(Integer pollID, final boolean onCreate) {
	    final PushPullApplication application = (PushPullApplication) getApplication();
		
		Poll.get(application, pollID, new PollCallback() {
			@Override
			public void success(Poll output) {
				poll = output;
				
				checkTabs();
				
				if (onCreate && (poll.getState() >= Poll.ACTIVE)) {
					ActionBar actionBar = getSupportActionBar();
					if (poll.getPollType() == Poll.SHORT_TEXT) {
						actionBar.setSelectedNavigationItem(1);
					}
					else {
						actionBar.setSelectedNavigationItem(2);
					}
				}
				
				((Updateable) getSupportFragmentManager().findFragmentById(android.R.id.content)).update();
				
				invalidateOptionsMenu();
			}
			@Override
			public void failure(Exception e) {
				Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		});
	}
	
	public Poll getPoll() {
		return poll;
	}
	
	public void checkTabs() {
		ActionBar actionBar = getSupportActionBar();
		switch (poll.getPollType()) {
			case Poll.RSVP:
				if (optionsTab.getPosition() != Tab.INVALID_POSITION) {
					actionBar.removeTab(optionsTab);
				}
				if (rsvpSummaryTab.getPosition() == Tab.INVALID_POSITION) {
					actionBar.addTab(rsvpSummaryTab);
				}
				break;
			case Poll.SHORT_TEXT:
				if (optionsTab.getPosition() != Tab.INVALID_POSITION) {
					actionBar.removeTab(optionsTab);
				}
				if (rsvpSummaryTab.getPosition() != Tab.INVALID_POSITION) {
					actionBar.removeTab(rsvpSummaryTab);
				}
				break;
			default:
				if (optionsTab.getPosition() == Tab.INVALID_POSITION) {
					actionBar.addTab(optionsTab);
				}
				if (rsvpSummaryTab.getPosition() != Tab.INVALID_POSITION) {
					actionBar.removeTab(rsvpSummaryTab);
				}
				break;
		}
		if (savedTabIndex >= 0) { //Because tabs are added dynamically, have to restore saved tab index after all tabs are added
			actionBar.setSelectedNavigationItem(savedTabIndex);
			savedTabIndex = -1;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final PushPullApplication application = (PushPullApplication) getApplication();
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, PollList.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.menu_activate:
	        	if (poll.getQuestion().length() == 0) {
	        		Toast.makeText(application, "Poll must have a question", Toast.LENGTH_SHORT).show();
	        		return true;
	        	}
	        	else if (poll.getShortResponses().size() == 0) {
	        		Toast.makeText(application, "Poll must have at least one person", Toast.LENGTH_SHORT).show();
	        		return true;
	        	}
	        	else if ((poll.getPollType() == Poll.SELECT_MANY || poll.getPollType() == Poll.SELECT_ONE) && poll.getPollSummary().size() == 0) {
	        		Toast.makeText(application, "Poll must have at least one poll option", Toast.LENGTH_SHORT).show();
	        		return true;
	        	}
	        	else {
	        		poll.activate(application, new VoidCallback() {

						@Override
						public void success() {
							updatePoll(poll.getID());
						}
						@Override
						public void failure(Exception e) {
							Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
	        		});
	        		return true;
	        	}
	        case R.id.menu_delete:
	        	showDialog(DIALOG_DELETE_ID); //This is deprecated, but the modern way of doing it looks like a ton more code for no benefit
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
    @Override
    public void onDestroy() {
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
    	tracker.trackPageView("/polls/view");
    	super.onResume();
    }
    
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case DIALOG_DELETE_ID:
        	final PushPullApplication application = (PushPullApplication) getApplication();
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("Are you sure you want to delete this poll?")
        	       .setCancelable(false)
        	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        		        	poll.delete(application, new VoidCallback() {
        						@Override
        						public void success() {
        							poll = null;
        							PollView.this.finish();
        						}
        						@Override
        						public void failure(Exception e) {
        							Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
        							e.printStackTrace();
        						}
        		        	});
        	           }
        	       })
        	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                dialog.cancel();
        	           }
        	       });
        	AlertDialog alert = builder.create();
        	return alert;
        default:
            dialog = null;
        }
        return dialog;
    }
    
    @Override
    public void onSaveInstanceState (Bundle outState) {
	    ActionBar actionBar = getSupportActionBar();
	    if (actionBar != null) {
		    outState.putInt("CurrentTab", actionBar.getSelectedTab().getPosition());
		    if (poll != null) {
		    	outState.putInt("PollID", poll.getID());
		    }
	    }
	    //actionBar.removeAllTabs();
	    super.onSaveInstanceState(outState);
    }
    
    public void nextTab() {
    	final ActionBar actionBar = getSupportActionBar();
    	actionBar.setSelectedNavigationItem(actionBar.getSelectedNavigationIndex() + 1);
    }

	@Override
	public void onPollDialogPositiveClick(DialogFragment dialog, String question) {
		final ProgressDialog progressDialog = ProgressDialog.show(this, "","Loading. Please wait...", true);
		poll = new Poll();
		poll.setQuestion(question);
		final PushPullApplication application = (PushPullApplication) getApplication();
		poll.post(application, new PollCallback() {
			@Override
			public void success(Poll output) {
				updatePoll(output.getID(),true);
				progressDialog.dismiss();
			}
			@Override
			public void failure(Exception e) {
				Toast.makeText(PollView.this, "POLL CREATION FAILURE", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				progressDialog.dismiss();
				finish();
			}
		});
	}

	@Override
	public void onPollDialogNegativeClick(DialogFragment dialog) {
		finish();
	}
}
