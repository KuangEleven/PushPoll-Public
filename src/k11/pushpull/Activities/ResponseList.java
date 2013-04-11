package k11.pushpull.Activities;

import k11.pushpull.Keys;
import k11.pushpull.R;
import k11.pushpull.TabListenerList;
import k11.pushpull.Fragments.ResponseListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ResponseList extends SherlockFragmentActivity {
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
	    
	    Tab responsesPendingTab = actionBar.newTab()
        .setText(R.string.responsespending_tabtitle)
        .setTabListener(new TabListenerList<ResponseListFragment>(this, "responsespending", ResponseListFragment.class));
	    actionBar.addTab(responsesPendingTab,true);
	    
	    Tab responsesCompletedTab = actionBar.newTab()
        .setText(R.string.responsescompleted_tabtitle)
        .setTabListener(new TabListenerList<ResponseListFragment>(this, "responsescompleted", ResponseListFragment.class));
	    actionBar.addTab(responsesCompletedTab,true);
	    
	    actionBar.setSelectedNavigationItem(0);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		tracker.trackPageView("/responses");
		
	    ((ResponseListFragment) getSupportFragmentManager().findFragmentByTag("responsespending")).setListType(ResponseListFragment.PENDING);
	    ((ResponseListFragment) getSupportFragmentManager().findFragmentByTag("responsescompleted")).setListType(ResponseListFragment.COMPLETED);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, MainMenu.class);
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
