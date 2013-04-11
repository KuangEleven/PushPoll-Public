package k11.pushpull.Activities;

import k11.pushpull.HasPoll;
import k11.pushpull.HasResponse;
import k11.pushpull.Keys;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.TabListener;
import k11.pushpull.Updateable;
import k11.pushpull.Callbacks.PollCallback;
import k11.pushpull.Callbacks.ResponseCallback;
import k11.pushpull.Callbacks.UserCallback;
import k11.pushpull.Data.Poll;
import k11.pushpull.Data.Response;
import k11.pushpull.Data.User;
import k11.pushpull.Fragments.PollOptions;
import k11.pushpull.Fragments.PollRSVPSummary;
import k11.pushpull.Fragments.PollResponses;
import k11.pushpull.Fragments.ResponseRSVP;
import k11.pushpull.Fragments.ResponseSingleSelect;
import k11.pushpull.Fragments.ResponseMultiSelect;
import k11.pushpull.Fragments.ResponseText;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ResponseView extends SherlockFragmentActivity implements HasPoll,HasResponse {
	GoogleAnalyticsTracker tracker;
	private Tab rsvpTab;
	private Tab singleSelectTab;
	private Tab multiSelectTab;
	private Tab shortTextTab;
	private Tab resultsSummaryTab;
	private Tab resultsRSVPTab;
	private Tab resultsListTab;
	private Response response;
	private Poll poll;
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
	    
	    if (savedInstanceState != null) {
		    savedTabIndex = savedInstanceState.getInt("CurrentTab",-1);
	    }
	    
        tracker = GoogleAnalyticsTracker.getInstance();

        // Start the tracker in manual dispatch mode...
        tracker.startNewSession(Keys.ANALYTICS_KEY, this);
	    
	    // setup action bar for tabs
	    ActionBar actionBar = getSupportActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(true);
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    rsvpTab = actionBar.newTab()
        .setText(R.string.response_tabtitle)
        .setTabListener(new TabListener<ResponseRSVP>(this, "responsersvp", ResponseRSVP.class));
	    
	    singleSelectTab = actionBar.newTab()
        .setText(R.string.response_tabtitle)
        .setTabListener(new TabListener<ResponseSingleSelect>(this, "responsesingleselect", ResponseSingleSelect.class));
	    
	    multiSelectTab = actionBar.newTab()
        .setText(R.string.response_tabtitle)
        .setTabListener(new TabListener<ResponseMultiSelect>(this, "responsemultiselect", ResponseMultiSelect.class));
	    
	    shortTextTab = actionBar.newTab()
        .setText(R.string.response_tabtitle)
        .setTabListener(new TabListener<ResponseText>(this, "responseshorttext", ResponseText.class));
	    
	    resultsSummaryTab = actionBar.newTab()
        .setText(R.string.responsesummary_tabtitle)
        .setTabListener(new TabListener<PollOptions>(this, "responsesummary", PollOptions.class));
	    
	    resultsRSVPTab = actionBar.newTab()
        .setText(R.string.responsesummary_tabtitle)
        .setTabListener(new TabListener<PollRSVPSummary>(this, "responsesummaryrsvp", PollRSVPSummary.class));
	    
	    resultsListTab = actionBar.newTab()
        .setText(R.string.responsesresultslist_tabtitle)
        .setTabListener(new TabListener<PollResponses>(this, "responseresultslist", PollResponses.class));
	    
	}
	
	public void updateResponse(Integer responseID) {
		final PushPullApplication application = (PushPullApplication) getApplication();
		
		Response.get(application, responseID, new ResponseCallback() {
			@Override
			public void success(Response output) {
				response = output;
				updatePoll(response.getPollID());
			}
			@Override
			public void failure(Exception e) {
				Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		});
	}
	
	public void updatePoll(Integer pollID) {
		final PushPullApplication application = (PushPullApplication) getApplication();
		
		Poll.get(application, pollID, new PollCallback() {
			@Override
			public void success(Poll output) {
				poll = output;
				
				ActionBar actionBar = getSupportActionBar();
				//Add appropriate tabs
				switch (poll.getPollType()) {
					case Poll.RSVP:
						if (rsvpTab.getPosition() == Tab.INVALID_POSITION) {
							actionBar.addTab(rsvpTab);
						}
						if (resultsRSVPTab.getPosition() == Tab.INVALID_POSITION && poll.getShareResults()) {
							actionBar.addTab(resultsRSVPTab);
						}
						break;
					case Poll.SELECT_ONE:
						if (singleSelectTab.getPosition() == Tab.INVALID_POSITION) {
							actionBar.addTab(singleSelectTab);
						}
						if (resultsSummaryTab.getPosition() == Tab.INVALID_POSITION && poll.getShareResults()) {
							actionBar.addTab(resultsSummaryTab);
						}
						break;
					case Poll.SELECT_MANY:
						if (multiSelectTab.getPosition() == Tab.INVALID_POSITION) {
							actionBar.addTab(multiSelectTab);
						}
						if (resultsSummaryTab.getPosition() == Tab.INVALID_POSITION && poll.getShareResults()) {
							actionBar.addTab(resultsSummaryTab);
						}
						break;
					case Poll.SHORT_TEXT:
						if (shortTextTab.getPosition() == Tab.INVALID_POSITION) {
							actionBar.addTab(shortTextTab);
						}
						break;
				}
				if (resultsListTab.getPosition() == Tab.INVALID_POSITION && poll.getShareResults()) {
					actionBar.addTab(resultsListTab);
				}
				
				//Next, update all tabs that are active
				if (getSupportFragmentManager().findFragmentById(android.R.id.content) != null && savedTabIndex == -1) { //The second part of this is terrible and wrong, but I get an unexplained NPE in findFragmentById otherwise. Also, the selected tab gets updated anyways
					((Updateable) getSupportFragmentManager().findFragmentById(android.R.id.content)).update();
				}
				
				if (savedTabIndex >= 0) { //Because tabs are added dynamically, have to restore saved tab index after all tabs are added
					actionBar.setSelectedNavigationItem(savedTabIndex);
					savedTabIndex = -1;
				}
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
	
	public Response getResponse() {
		return response;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, ResponseList.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
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
    	tracker.trackPageView("/responses/view");
    	super.onResume();
    	
	    final Bundle bundle = getIntent().getExtras();
	    final PushPullApplication application = (PushPullApplication) getApplication();
	    
    	if (application.getCurrentUser() == null) {
    		User.getCurrentUser(application, new UserCallback() {
				@Override
				public void success(User user) {
					application.setCurrentUser(user);
					updateResponse(bundle.getInt("ResponseID"));
				}
				@Override
				public void failure(Exception e) {
					Toast.makeText(application, "AUTHENTICATION FAILURE", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
    		});
    	}
    	else {
    		updateResponse(bundle.getInt("ResponseID"));
    	}
    }
    
    @Override
    public void onSaveInstanceState (Bundle outState) {
	    ActionBar actionBar = getSupportActionBar();
	    if (actionBar != null) {
	    	outState.putInt("CurrentTab", actionBar.getSelectedTab().getPosition());
	    }
	    //actionBar.removeAllTabs();
	    super.onSaveInstanceState(outState);
    }
}
