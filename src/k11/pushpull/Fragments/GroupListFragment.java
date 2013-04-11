package k11.pushpull.Fragments;

import java.util.ArrayList;
import java.util.List;

import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Activities.Friends;
import k11.pushpull.Activities.GroupView;
import k11.pushpull.Callbacks.GroupListCallback;
import k11.pushpull.Data.Group;
import k11.pushpull.Data.Poll;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockListFragment;

public class GroupListFragment  extends SherlockListFragment {
	List<Group> groupList = new ArrayList<Group>();
	
	@Override
	public void onResume() {
		super.onResume();
		
		Friends activity = ((Friends) getSherlockActivity());
		activity.invalidateOptionsMenu();
		
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        
        final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
        
        lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Intent i = new Intent(application, GroupView.class);
				Bundle bundle = new Bundle();
				bundle.putInt("GroupID", groupList.get(position).getId());
				i.putExtras(bundle);
				startActivity(i);
			}
		});
        
        Group.getGroups(application, new GroupListCallback() {
			@Override
			public void success(List<Group> output) {
				GroupListFragment.this.groupList = output;
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
		ArrayList<String> itemList = new ArrayList<String>();
		int length = groupList.size();
		for (int i=0; i<length; i++) {
			itemList.add(groupList.get(i).getName());
		}
		setListAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_1, itemList));
	}
}
