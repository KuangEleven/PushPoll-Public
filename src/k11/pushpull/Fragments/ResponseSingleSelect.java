package k11.pushpull.Fragments;

import java.util.ArrayList;
import java.util.List;

import k11.pushpull.HasPoll;
import k11.pushpull.HasResponse;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Updateable;
import k11.pushpull.Callbacks.VoidCallback;
import k11.pushpull.Data.Poll;
import k11.pushpull.Data.PollOption;
import k11.pushpull.Data.Response;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class ResponseSingleSelect extends SherlockFragment implements Updateable {
	ArrayList<PollOption> pollOptionList;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.responsesingleselect, container, false);
    	View rootView = inflater.inflate(R.layout.responsesingleselect, container, false);
    	ListView lv = (ListView) rootView.findViewById(R.id.listview);
        lv.setOnItemClickListener( new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
				final Response response = ((HasResponse) getSherlockActivity()).getResponse();
				if (response == null) return;
				response.setPollOptionID(pollOptionList.get(position).getID());
				response.put(application, new VoidCallback() {
					@Override
					public void success() {
						Toast.makeText(application, "Response Submitted", Toast.LENGTH_SHORT).show();
						if (getSherlockActivity() != null) {
							((HasPoll) getSherlockActivity()).updatePoll(response.getPollID());
						}
					}
					@Override
					public void failure(Exception e) {
						Toast.makeText(application, "Connection Failure", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				});
			}
		});
    	return rootView;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	update();
    }
    
    public void update() {
    	final Response response = ((HasResponse) getSherlockActivity()).getResponse();
    	final Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
    	
    	if ((getView() != null) && (poll != null)) {
    		TextView questionTextView = (TextView) getSherlockActivity().findViewById(R.id.question_text);
    		questionTextView.setText(poll.getQuestion());
    	
	    	ListView lv = (ListView) getSherlockActivity().findViewById(R.id.listview);
	    	
	    	Integer checkedItem = 0;
			List<String> itemList = new ArrayList<String>();
			pollOptionList = new ArrayList<PollOption>();
			ArrayList<PollOption> pollSummary = poll.getPollSummary();
			Integer arraySize = pollSummary.size();
			for (Integer i = 0; i < arraySize; ++i) {
				itemList.add(pollSummary.get(i).getLabel());
				pollOptionList.add(pollSummary.get(i));
				if (response.getPollOptionID() != null) {
					if (response.getPollOptionID().equals(pollSummary.get(i).getID())) {
						checkedItem = i;
					}
				}
			}
			lv.setTextFilterEnabled(true);
			lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			
			if (poll.getState() > Poll.ACTIVE) {
				lv.setAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_single_choice, itemList) {
					public boolean isEnabled(int position) 
		            { 
		                    return false; 
		            } 
				});
			}
			else {
				lv.setAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_single_choice, itemList));
			}
			
			if (response.getPollOptionID() > 0) {
				lv.setItemChecked(checkedItem, true);
			}
    	}
    }
}
