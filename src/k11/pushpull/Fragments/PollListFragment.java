package k11.pushpull.Fragments;

import java.util.ArrayList;
import java.util.List;

import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Activities.PollView;
import k11.pushpull.Callbacks.PollListCallback;
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

public class PollListFragment extends SherlockListFragment {
	List<Poll> pollList = new ArrayList<Poll>();
	Integer listType = 0;
	private Integer completedOffset = 0;
	private Boolean morePolls = false;
	
	public static final int DRAFT = 1;
	public static final int ACTIVE = 2;
	public static final int COMPLETED = 3;
	
	@Override
	public void onResume() {
		super.onResume();
		
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        
        final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
        
        lv.setOnItemClickListener(new OnItemClickListener() {
        	
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				if (position == completedOffset + 20) {
					completedOffset =+ 20;
		    		Poll.getCompleted(application, 20, completedOffset, new PollListCallback () {
		    			@Override
		    			public void success(List<Poll> newPollList) {
		    				pollList.addAll(newPollList);
		    				if (pollList.size() == 20) {
		    					morePolls = true;
		    				}
		    				else {
		    					morePolls = false;
		    				}
		    				completedOffset =+ 20;
		    				refresh();
		    			}
		    			@Override
		    			public void failure(Exception e) {
		    				e.printStackTrace();
		    			}
		    		});
				}
				else {
					Intent i = new Intent(application, PollView.class);
					Bundle bundle = new Bundle();
					bundle.putInt("PollID", pollList.get(position).getID());
					i.putExtras(bundle);
					startActivity(i);
				}
			}
		});
        
        switch (listType) {
        	case DRAFT:
        		Poll.getDraft(application, new PollListCallback() {
        			@Override
        			public void success(List<Poll> output) {
        				PollListFragment.this.pollList = output;
        				refresh();
        			}
        			@Override
        			public void failure(Exception e) {
        				Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
        				e.printStackTrace();
        			}
        		});
        		break;
        	case ACTIVE:
        		Poll.getActive(application, new PollListCallback() {
        			@Override
        			public void success(List<Poll> output) {
        				PollListFragment.this.pollList = output;
        				refresh();
        			}
        			@Override
        			public void failure(Exception e) {
        				Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
        				e.printStackTrace();
        			}
        		});
        		break;
        	case COMPLETED:
        		Poll.getCompleted(application, 20, 0, new PollListCallback() {
        			@Override
        			public void success(List<Poll> output) {
        				PollListFragment.this.pollList = output;
	    				if (pollList.size() == 20) {
	    					morePolls = true;
	    				}
	    				else {
	    					morePolls = false;
	    				}
	    				completedOffset = 0;
        				refresh();
        			}
        			@Override
        			public void failure(Exception e) {
        				Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
        				e.printStackTrace();
        			}
        		});
        		break;
        }
	}
	
	public void refresh() {
		ArrayList<String> itemList = new ArrayList<String>();
		final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
		int length = pollList.size();
		for (int i=0; i<length; i++) {
			itemList.add(pollList.get(i).getQuestion());
		}
		
        if (morePolls) {
        	itemList.add(application.getString(R.string.polllist_more));
        }
		
		setListAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_1, itemList));
		this.setSelection(completedOffset);
	}
	
	public void setListType(Integer listType) {
		this.listType = listType;
	}
}
