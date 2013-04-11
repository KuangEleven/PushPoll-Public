package k11.pushpull.Fragments;

import java.util.ArrayList;
import java.util.List;

import k11.pushpull.HasPoll;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Activities.Friends;
import k11.pushpull.Callbacks.FriendListCallback;
import k11.pushpull.Callbacks.VoidCallback;
import k11.pushpull.Data.Friend;
import k11.pushpull.Data.Poll;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;

public class FriendListFragment extends SherlockListFragment {
    
    @Override
    public void onResume() {
    	super.onResume();
        
    	update();
    }
    
    public void update() {
    	if (getSherlockActivity() != null) {
    		
    		Friends activity = ((Friends) getSherlockActivity());
    		activity.invalidateOptionsMenu();
    		
    		final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
    		Friend.getDomesticFriends(application, new FriendListCallback() {
				@Override
				public void success(List<Friend> output) {
					ListView lv = getListView();
					ArrayList<String> list = new ArrayList<String>();
					List<Friend> friendList = output;
					
					int length = friendList.size();
			        for (int i=0; i<length; i++) {
			        	if (friendList.get(i).getName().length() > 0) {
			        		list.add(friendList.get(i).getName());
			        	}
			        	else {
			        		list.add(friendList.get(i).getEmail());
			        	}
			        }
			        setListAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_1, list));
				}
				@Override
				public void failure(Exception e) {
					Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
    		});
    	}
    }

}
