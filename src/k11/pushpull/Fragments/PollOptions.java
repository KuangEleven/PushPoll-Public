package k11.pushpull.Fragments;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;

import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Updateable;
import k11.pushpull.Callbacks.PollOptionCallback;
import k11.pushpull.Callbacks.VoidCallback;
import k11.pushpull.Data.Poll;
import k11.pushpull.Data.PollOption;
import k11.pushpull.Data.ResponseShort;
import k11.pushpull.HasPoll;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PollOptions extends SherlockFragment implements OnClickListener,Updateable {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.polloptions, container, false);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	update();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	getSherlockActivity().findViewById(R.id.addoption_button).setOnClickListener(this);
    }
    
    public void update() {
    	if (getSherlockActivity() != null) {
	    	Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
	    	if ((getView() != null) && (poll != null)) {
				Integer responsesTotal = 0;
				Integer responsesAnswered = 0;
				for (ResponseShort responseShort : poll.getShortResponses()) {
					responsesTotal += 1;
					if (responseShort.getCleanOutput() != null) {
						responsesAnswered += 1;
					}
				}
		        
		        TextView responsesTextView = (TextView) getSherlockActivity().findViewById(R.id.options_text);
		        responsesTextView.setText(responsesAnswered + "/" + responsesTotal);
		        
		        ProgressBar progress = (ProgressBar) getSherlockActivity().findViewById(R.id.options_progress);
		        progress.setMax(responsesTotal);
		        progress.setProgress(responsesAnswered);
				
		        ListView lv = (ListView) getSherlockActivity().findViewById(R.id.options_listview);
		        ArrayList<AdapterTuple> list = new ArrayList<AdapterTuple>();
		        
		        int length = poll.getPollSummary().size();
		        for (int i=0; i<length; i++) {
		        	list.add(new AdapterTuple(responsesAnswered,poll.getPollSummary().get(i).getTimesChosen(),poll.getPollSummary().get(i).getLabel(),poll.getPollSummary().get(i).getID(),poll.getPollSummary().get(i).getPollID()));
		        }
		        ResultsAdapter adapter = new ResultsAdapter(getSherlockActivity(),R.layout.progresslistitem,list);
		        lv.setAdapter(adapter);
		        
				PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
				if (!(poll.isOwner(application.getCurrentUser().getID()))) {
					LinearLayout addLayout = (LinearLayout) getSherlockActivity().findViewById(R.id.options_add);
					addLayout.setVisibility(View.GONE);
				}
				
				if (poll.getState() > Poll.DRAFT) { //Poll already started
					LinearLayout addLayout = (LinearLayout) getSherlockActivity().findViewById(R.id.options_add);
					addLayout.setVisibility(View.GONE);
				}	
			}
    	}
    }
    
	private class ResultsAdapter extends ArrayAdapter<AdapterTuple> {
		private int textViewResouceId;
		private LayoutInflater layoutInflater;
		public ResultsAdapter(Context context, int textViewResourceId,
				List<AdapterTuple> objects) {
			super(context, textViewResourceId, objects);
			textViewResourceId = this.textViewResouceId;
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView (final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (convertView == null) {
				view = layoutInflater.inflate(R.layout.progresslistitem, null);
			}
			
			ProgressBar progress = (ProgressBar) view.findViewById(R.id.bar);
			progress.setMax(getItem(position).max);
			progress.setProgress(getItem(position).progress);
			
			TextView caption = (TextView) view.findViewById(R.id.caption);
			caption.setText(getItem(position).caption);
			
			final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
			Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
			if (poll.isOwner(application.getCurrentUser().getID()) && poll.getState() < Poll.ACTIVE) {
				ImageView deleteImage = (ImageView) view.findViewById(R.id.delete);
				deleteImage.setVisibility(ImageView.VISIBLE);
				deleteImage.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						PollOption.delete(application, getItem(position).optionID, getItem(position).pollID, new VoidCallback() {
							@Override
							public void success() {
								((HasPoll) getSherlockActivity()).updatePoll(getItem(position).pollID);
							}
							@Override
							public void failure(Exception e) {
								Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
								e.printStackTrace();
							}
						});
					}
				});
			}
			return view;
		}
	}
	
	private class AdapterTuple {
		public Integer max;
		public Integer progress;
		public String caption;
		public Integer optionID;
		public Integer pollID;
		
		AdapterTuple(Integer max,Integer progress,String caption,Integer optionID,Integer pollID) {
			this.max = max;
			this.progress = progress;
			this.caption = caption;
			this.optionID = optionID;
			this.pollID = pollID;
		}
	}

	@Override
	public void onClick(View v) {
		Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
		final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
		
		switch (v.getId()) {
			case R.id.addoption_button:
				EditText optionText = (EditText) getSherlockActivity().findViewById(R.id.addoption_text);
				
				if (poll.getState() == Poll.NEW) {
					Toast.makeText(application, "Poll creation requires a question", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (optionText.getText().length() == 0) {
					return;
				}
				
				PollOption pollOption = new PollOption();
				pollOption.setPollID(poll.getID());
				pollOption.setLabel(optionText.getText().toString());
				pollOption.post(application, new PollOptionCallback() {
					@Override
					public void success(PollOption output) {
						((HasPoll) getSherlockActivity()).updatePoll(output.getPollID());
					}
					@Override
					public void failure(Exception e) {
						Toast.makeText(application, "CONNECTION FAILURE", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
				});
				optionText.setText("");
				break;
		}
	}
}

