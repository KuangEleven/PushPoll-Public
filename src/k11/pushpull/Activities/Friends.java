package k11.pushpull.Activities;

import k11.pushpull.Keys;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.TabListenerList;
import k11.pushpull.Fragments.FriendListFragment;
import k11.pushpull.Fragments.GroupListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Friends extends SherlockFragmentActivity {
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
	    
	    Tab friendsTab = actionBar.newTab()
        .setText(R.string.friends_tabtitle)
        .setTabListener(new TabListenerList<FriendListFragment>(this, "friends", FriendListFragment.class));
	    actionBar.addTab(friendsTab,true);
	    
	    Tab groupsTab = actionBar.newTab()
        .setText(R.string.groups_tabtitle)
        .setTabListener(new TabListenerList<GroupListFragment>(this, "groups", GroupListFragment.class));
	    actionBar.addTab(groupsTab,true);
	    
	    actionBar.setSelectedNavigationItem(0);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		tracker.trackPageView("/friends");
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
				Intent i = new Intent(application, GroupView.class);
				Bundle bundle = new Bundle();
				bundle.putInt("GroupID", 0);
				i.putExtras(bundle);
				startActivity(i);
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
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.friendlist, menu);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar.getSelectedNavigationIndex() == 0) {
			menu.findItem(R.id.menu_new).setVisible(false);
		}
		else {
			menu.findItem(R.id.menu_new).setVisible(true);
		}
		return true;
	}

}
