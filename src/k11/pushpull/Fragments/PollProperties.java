package k11.pushpull.Fragments;

import k11.pushpull.HasNextTab;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Updateable;
import k11.pushpull.Activities.PollView;
import k11.pushpull.Callbacks.PollCallback;
import k11.pushpull.Callbacks.VoidCallback;
import k11.pushpull.Data.Poll;
import k11.pushpull.HasPoll;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class PollProperties extends SherlockFragment implements OnItemSelectedListener,Updateable {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pollproperties, container, false);
        
        //Instantiate spinner
		Spinner spinner = (Spinner) view.findViewById(R.id.polltype_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getSherlockActivity(),R.array.poll_type, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		
		spinner.setOnItemSelectedListener(this);
		
		return view;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	update();
    }
    
    public void update() {
    	Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
    	if ((getView() != null) && (poll != null)) {
    		EditText questionText = (EditText) getSherlockActivity().findViewById(R.id.question_text);
    		Spinner pollTypeSpinner = (Spinner) getSherlockActivity().findViewById(R.id.polltype_spinner);
    		CheckBox shareResultsCheckBox = (CheckBox) getSherlockActivity().findViewById(R.id.shareresults_check);
    		
    		questionText.setText(poll.getQuestion());
	    	pollTypeSpinner.setSelection(poll.getPollType() - 1);
    		shareResultsCheckBox.setChecked(poll.getShareResults());
    		
    		PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
    		if (!(poll.isOwner(application.getCurrentUser().getID()))) { //If not owner of poll, do not allow modifying poll
    			pollTypeSpinner.setEnabled(false);
    			questionText.setClickable(false);
    			questionText.setCursorVisible(false);
    			questionText.setFocusable(false);
    			//questionText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    			shareResultsCheckBox.setClickable(false);
    		}
    		
    		if (poll.getState() > Poll.DRAFT) {
    			pollTypeSpinner.setEnabled(false);
    		}
    		
    		if (poll.getState() > Poll.ACTIVE) {
    			questionText.setClickable(false);
    			questionText.setCursorVisible(false);
    			questionText.setFocusable(false);
    			//questionText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    			shareResultsCheckBox.setClickable(false);
    		}
    		
    		if (poll.getState() < Poll.ACTIVE) {
    			Button nextButton = (Button) getSherlockActivity().findViewById(R.id.next_button);
    			nextButton.setVisibility(Button.VISIBLE);
    			nextButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						((HasNextTab) getSherlockActivity()).nextTab();
					}
				});
    		}
    	}
    }
    
    @Override
    public void onPause() { //Save to Activities Poll
    	super.onResume();
    	if (getSherlockActivity() != null) {
	    	Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
	    	final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
	    	
			EditText questionText = (EditText) getSherlockActivity().findViewById(R.id.question_text);
			CheckBox shareResultsCheckBox = (CheckBox) getSherlockActivity().findViewById(R.id.shareresults_check);
			if (poll != null) {
		    	if (poll.isOwner(application.getCurrentUser().getID()) && poll.getState() < Poll.PAST && questionText.getText().length() > 0) {
					poll.setQuestion(questionText.getText().toString());
					poll.setShareResults(shareResultsCheckBox.isChecked());
		    		poll.put(application, new VoidCallback() {
						@Override
						public void success() {
							//Do nothing
						}
						@Override
						public void failure(Exception e) {
							Toast.makeText(application, "POLL UPDATE FAILURE", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
		    		});
		    	}
			}
    	}
    }

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		//Only triggers on Spinner
		PollView activity = (PollView) getSherlockActivity();
		if ( activity.getPoll() != null) {
			activity.getPoll().setPollType(position + 1);
			activity.checkTabs();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		//Do Nothing
	}
}
