package k11.pushpull;

//import android.app.FragmentTransaction;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;

//import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

//So far, I'm just copying straight from the Android API docs...
public class TabListenerList<T extends SherlockListFragment> implements ActionBar.TabListener {
    private Fragment mFragment; //I think this should use SherlockFragment, but then I don't get a proper FragmentTransaction
    private final SherlockFragmentActivity mActivity;
    private final String mTag;
    private final Class<T> mClass;

    
    public TabListenerList(SherlockFragmentActivity activity, String tag, Class<T> clz) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
    }
	
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ftIgnore) {
		FragmentManager fragMgr = mActivity.getSupportFragmentManager();
	    FragmentTransaction ft = fragMgr.beginTransaction();
        // Check if the fragment is already initialized
        if (mFragment == null) {
            // If not, instantiate and add it to the activity
            mFragment = SherlockFragment.instantiate(mActivity, mClass.getName());
            ft.add(android.R.id.content, mFragment, mTag);
            ft.commit(); //This is supposed to be wrong...
        } else {
            // If it exists, simply attach it in order to show it
            ft.attach(mFragment);
            ft.commit(); //This is supposed to be wrong...
        }
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ftIgnore) {
		FragmentManager fragMgr = ((FragmentActivity)mActivity).getSupportFragmentManager();
	    FragmentTransaction ft = fragMgr.beginTransaction();
        if (mFragment != null) {
            // Detach the fragment, because another one is being attached
            ft.detach(mFragment);
            ft.commit(); //This is supposed to be wrong...
        }
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// User selected the already selected tab. Usually do nothing.
	}
	
}
