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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class ResponseMultiSelect extends SherlockFragment implements Updateable {
	ArrayList<PollOption> pollOptionList;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.responsesingleselect, container, false);
    	return inflater.inflate(R.layout.responsemultiselect, container, false);
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
    		List<Integer> checkedItems = new ArrayList<Integer>();
    		ListView lv = (ListView) getSherlockActivity().findViewById(R.id.listview);
			List<String>itemList = new ArrayList<String>();
			pollOptionList = new ArrayList<PollOption>();
			ArrayList<PollOption> pollSummary = poll.getPollSummary();
			Integer arraySize = pollSummary.size();
			for (Integer i = 0; i < arraySize; ++i) {
				itemList.add(pollSummary.get(i).getLabel());
				pollOptionList.add(pollSummary.get(i));
				if (response.getShortText() != null) {
					if ((" " + response.getShortText() + " ").contains(" " + pollSummary.get(i).getID().toString() + " "))
						checkedItems.add(i);
				}
			}
			
			lv.setTextFilterEnabled(true);
			lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			
			if (poll.getState() > Poll.ACTIVE) {
				lv.setAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_multiple_choice, itemList) {
					public boolean isEnabled(int position) 
		            { 
		                    return false; 
		            } 
				});
			}
			else {
				lv.setAdapter(new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_multiple_choice, itemList));
			}
			
			for (Integer checkedItem : checkedItems) {
				lv.setItemChecked(checkedItem, true);
			}
			
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	ListView lv = (ListView) getSherlockActivity().findViewById(R.id.listview);
		Integer listSize = lv.getAdapter().getCount();
		final Response response = ((HasResponse) getSherlockActivity()).getResponse();
		final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
		String pollOptionsString = "";
		for (Integer i = 0; i < listSize; ++i) {
			if (lv.isItemChecked(i)) {
				pollOptionsString = pollOptionsString.concat(pollOptionList.get(i).getID().toString() + " ");
			}
		}
		if (pollOptionsString.length() > 0)
			pollOptionsString = pollOptionsString.substring(0, pollOptionsString.length() - 1);
		response.setShortText(pollOptionsString);
		
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

}
