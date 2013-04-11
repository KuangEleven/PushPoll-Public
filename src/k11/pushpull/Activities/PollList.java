package k11.pushpull.Activities;

import k11.pushpull.Keys;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.TabListenerList;
import k11.pushpull.Fragments.PollListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.gcm.GCMRegistrar;

public class PollList extends SherlockFragmentActivity {
	GoogleAnalyticsTracker tracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    if (getSupportFragmentManager().findFragmentById(android.R.id.content) != null) {
	    	FragmentManager fragMgr = getSupportFragmentManager();
	    	FragmentTransaction ft = fragMgr.beginTransaction();
            ft.detach(getSupportFragmentManager().findFragmentById(android.R.id.content));
            ft.commit();
	    }
	    
        tracker = GoogleAnalyticsTracker.getInstance();

        // Start the tracker in manual dispatch mode...
        tracker.startNewSession(Keys.ANALYTICS_KEY, this);
	    // setup action bar for tabs
	    ActionBar actionBar = getSupportActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(true);
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    Tab pollsDraftTab = actionBar.newTab()
        .setText(R.string.pollsdraft_tabtitle)
        .setTabListener(new TabListenerList<PollListFragment>(this, "pollsdraft", PollListFragment.class));
	    actionBar.addTab(pollsDraftTab,true);
	    
	    Tab pollsActiveTab = actionBar.newTab()
        .setText(R.string.pollsactive_tabtitle)
        .setTabListener(new TabListenerList<PollListFragment>(this, "pollsactive", PollListFragment.class));
	    actionBar.addTab(pollsActiveTab,true);
	    	    
	    Tab pollsCompletedTab = actionBar.newTab()
        .setText(R.string.pollscompleted_tabtitle)
        .setTabListener(new TabListenerList<PollListFragment>(this, "pollscompleted", PollListFragment.class));
	    actionBar.addTab(pollsCompletedTab,true);
	    
	    actionBar.setSelectedNavigationItem(1);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		tracker.trackPageView("/polls");
		
	    ((PollListFragment) getSupportFragmentManager().findFragmentByTag("pollsdraft")).setListType(PollListFragment.DRAFT);
	    ((PollListFragment) getSupportFragmentManager().findFragmentByTag("pollsactive")).setListType(PollListFragment.ACTIVE);
		((PollListFragment) getSupportFragmentManager().findFragmentByTag("pollscompleted")).setListType(PollListFragment.COMPLETED);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.polllist, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final PushPullApplication application = (PushPullApplication) getApplication();
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, MainMenu.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.menu_new:
				Intent i = new Intent(application, PollView.class);
				Bundle bundle = new Bundle();
				bundle.putInt("PollID", 0);
				i.putExtras(bundle);
				startActivity(i);
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
    public void onSaveInstanceState (Bundle outState) {
	    ActionBar actionBar = getSupportActionBar();
	    outState.putInt("CurrentTab", actionBar.getSelectedTab().getPosition());
	    //actionBar.removeAllTabs();
	    super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle inState) {
    	ActionBar actionBar = getSupportActionBar();
    	actionBar.setSelectedNavigationItem(inState.getInt("CurrentTab"));
    	super.onRestoreInstanceState(inState);
    }
}
