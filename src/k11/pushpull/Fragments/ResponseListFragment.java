package k11.pushpull.Fragments;

import java.util.ArrayList;
import java.util.List;

import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Activities.ResponseView;
import k11.pushpull.Callbacks.ResponsePollListCallback;
import k11.pushpull.Data.Poll;
import k11.pushpull.Data.Response;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockListFragment;

public class ResponseListFragment extends SherlockListFragment {
	private List<Response> responseList = new ArrayList<Response>();
	private List<Poll> pollList = new ArrayList<Poll>();
	private Integer listType = 0;
	private Integer completedOffset = 0;
	private Boolean moreResponses = false;
	public static final int PENDING = 1;
	public static final int COMPLETED = 2;
	
	public void onResume() {
		super.onResume();
		
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        
        final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
        
        lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				if (position == completedOffset + 20) {
					completedOffset =+ 20;
		    		Response.getCompleted(application, 20, completedOffset, new ResponsePollListCallback () {
		    			@Override
		    			public void success(List<Response> newResponseList, List<Poll> newPollList) {
		    				responseList.addAll(newResponseList);
		    				pollList.addAll(newPollList);
		    				if (responseList.size() == 20) {
		    					moreResponses = true;
		    				}
		    				else {
		    					moreResponses = false;
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
					Intent i = new Intent(application, ResponseView.class);
					Bundle bundle = new Bundle();
					bundle.putInt("ResponseID", responseList.get(position).getID());
					i.putExtras(bundle);
					startActivity(i);
				}
			}
		});
        
        switch (listType) {
	    	case PENDING:
	    		Response.getPending(application, new ResponsePollListCallback () {
	    			@Override
	    			public void success(List<Response> responseList, List<Poll> pollList) {
	    				ResponseListFragment.this.pollList = pollList;
	    				ResponseListFragment.this.responseList = responseList;
	    				refresh();
	    			}
	    			@Override
	    			public void failure(Exception e) {
	    				e.printStackTrace();
	    			}
	    		});
	    		break;
	    	case COMPLETED:
	    		Response.getCompleted(application, 20, 0, new ResponsePollListCallback () {
	    			@Override
	    			public void success(List<Response> responseList, List<Poll> pollList) {
	    				ResponseListFragment.this.pollList = pollList;
	    				ResponseListFragment.this.responseList = responseList;
	    				if (responseList.size() == 20) {
	    					moreResponses = true;
	    				}
	    				else {
	    					moreResponses = false;
	    				}
	    				completedOffset = 0;
	    				refresh();
	    			}
	    			@Override
	    			public void failure(Exception e) {
	    				e.printStackTrace();
	    			}
	    		});
	    		break;
        }
	}
	
	public void refresh() {
		ArrayList<AdapterTuple> itemList = new ArrayList<AdapterTuple>();
		final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
		int length = pollList.size();
        for (int i=0; i<length; i++) {
        	itemList.add(new AdapterTuple(pollList.get(i).getQuestion(),(pollList.get(i).getState() >= Poll.PAST)));
        }
        
        if (moreResponses) {
        	itemList.add(new AdapterTuple(application.getString(R.string.responselist_more),false));
        }
        
        ResponseAdapter adapter = new ResponseAdapter(getSherlockActivity(),android.R.layout.simple_list_item_1,itemList);
		setListAdapter(adapter);
		this.setSelection(completedOffset);
	}
	
	public void setListType(Integer listType) {
		this.listType = listType;
	}
	
	private class AdapterTuple {
		public String text;
		public Boolean lock;
		
		AdapterTuple(String text,Boolean lock) {
			this.text = text;
			this.lock = lock;
		}
	}
	
	private class ResponseAdapter extends ArrayAdapter<AdapterTuple> {
		private int textViewResouceId;
		private LayoutInflater layoutInflater;
		public ResponseAdapter(Context context, int textViewResourceId,List<AdapterTuple> objects) {
			super(context, textViewResourceId, objects);
			textViewResourceId = this.textViewResouceId;
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView (int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
			}
			
			TextView textView = (TextView) view.findViewById(android.R.id.text1);
			textView.setText(getItem(position).text);
			if (getItem(position).lock) {
				textView.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.stat_sys_secure,0);
			}
			else {
				textView.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
			}
			
			return view;
		}
	}
}
