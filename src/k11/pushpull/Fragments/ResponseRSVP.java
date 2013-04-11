package k11.pushpull.Fragments;

import k11.pushpull.HasPoll;
import k11.pushpull.HasResponse;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Updateable;
import k11.pushpull.Callbacks.VoidCallback;
import k11.pushpull.Data.Poll;
import k11.pushpull.Data.Response;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;

public class ResponseRSVP extends SherlockFragment implements OnClickListener,Updateable {
	ProgressDialog dialog;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.responsersvp, container, false);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	update();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	getSherlockActivity().findViewById(R.id.rsvp_me_button).setOnClickListener(this);
    	getSherlockActivity().findViewById(R.id.rsvp_plus_button).setOnClickListener(this);
    	getSherlockActivity().findViewById(R.id.rsvp_no_button).setOnClickListener(this);
    }
    
    public void update() {
    	Response response = ((HasResponse) getSherlockActivity()).getResponse();
    	Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
    	if ((getView() != null) && (poll != null)) {
            if (poll.getState() > Poll.ACTIVE) {
            	Button rsvpMeButton = (Button) getSherlockActivity().findViewById(R.id.rsvp_me_button);
            	rsvpMeButton.setClickable(false);
            	
            	Button rsvpPlusButton = (Button) getSherlockActivity().findViewById(R.id.rsvp_plus_button);
            	rsvpPlusButton.setClickable(false);
            	
            	EditText rsvpPlusEditText = (EditText) getSherlockActivity().findViewById(R.id.rsvp_plus_text);
            	rsvpPlusEditText.setClickable(false);
            	rsvpPlusEditText.setCursorVisible(false);
            	rsvpPlusEditText.setFocusable(false);
            	
            	Button rsvpNoButton = (Button) getSherlockActivity().findViewById(R.id.rsvp_no_button);
            	rsvpNoButton.setClickable(false);
            }
            
    		TextView questionTextView = (TextView) getSherlockActivity().findViewById(R.id.question_text);
    		questionTextView.setText(poll.getQuestion());
    		
    		ToggleButton rsvpNoButton = (ToggleButton) getSherlockActivity().findViewById(R.id.rsvp_no_button);
    		ToggleButton rsvpMeButton = (ToggleButton) getSherlockActivity().findViewById(R.id.rsvp_me_button);
    		ToggleButton rsvpPlusButton = (ToggleButton) getSherlockActivity().findViewById(R.id.rsvp_plus_button);
    		
    		if (response.getShortText() == null) {
    			//No response already found
    		}
    		else if (Integer.valueOf(response.getShortText()) == 0) {
    			
    			rsvpNoButton.setChecked(true);
    			rsvpMeButton.setChecked(false);
    			rsvpPlusButton.setChecked(false);
    		}
    		else if (Integer.valueOf(response.getShortText()) == 1) {
    			rsvpMeButton.setChecked(true);
    			rsvpNoButton.setChecked(false);
    			rsvpPlusButton.setChecked(false);
    		}
    		else {
    			rsvpPlusButton.setChecked(true);
    			Integer rsvpPlus = Integer.valueOf(response.getShortText()) - 1; //Short Text hold the total number of people RSVPing
    			EditText rsvpPlusEditText = (EditText) getSherlockActivity().findViewById(R.id.rsvp_plus_text);
    			rsvpPlusEditText.setText(rsvpPlus.toString());
    			rsvpMeButton.setChecked(false);
    			rsvpNoButton.setChecked(false);
    		}
    	}
    }
    
	@Override
	public void onClick(View v) {
		ToggleButton rsvpMeButton = (ToggleButton) getSherlockActivity().findViewById(R.id.rsvp_me_button);
		ToggleButton rsvpPlusButton = (ToggleButton) getSherlockActivity().findViewById(R.id.rsvp_plus_button);
		ToggleButton rsvpNoButton = (ToggleButton) getSherlockActivity().findViewById(R.id.rsvp_no_button);
		EditText rsvpPlusEditText = (EditText) getSherlockActivity().findViewById(R.id.rsvp_plus_text);
		final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
		Toast.makeText(application, "Response Submitted", Toast.LENGTH_SHORT).show(); //A lie, because we actually submit the response on onPause
		
    	switch (v.getId()) {
		case R.id.rsvp_me_button:
			rsvpPlusButton.setChecked(false);
			rsvpNoButton.setChecked(false);
			rsvpPlusEditText.setText("");
			break;
		case R.id.rsvp_plus_button:
			rsvpMeButton.setChecked(false);
			rsvpNoButton.setChecked(false);
			break;
		case R.id.rsvp_no_button:
			rsvpMeButton.setChecked(false);
			rsvpPlusButton.setChecked(false);
			rsvpPlusEditText.setText("");
			break;
    	}
	}
	
    @Override
    public void onPause() {
    	super.onPause();
    	final Response response = ((HasResponse) getSherlockActivity()).getResponse();
    	final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
    	
    	ToggleButton rsvpMeButton = (ToggleButton) getSherlockActivity().findViewById(R.id.rsvp_me_button);
    	ToggleButton rsvpPlusButton = (ToggleButton) getSherlockActivity().findViewById(R.id.rsvp_plus_button);
    	ToggleButton rsvpNoButton = (ToggleButton) getSherlockActivity().findViewById(R.id.rsvp_no_button);
    	EditText rsvpPlusEditText = (EditText) getSherlockActivity().findViewById(R.id.rsvp_plus_text);
    	
    	if (response != null) {
    		if (rsvpMeButton.isChecked()) {
    			response.setShortText("1");
    		}
    		else if (rsvpPlusButton.isChecked() && rsvpPlusEditText.getText().length() > 0) {
    			Integer numRSVP = 1 + Integer.valueOf(rsvpPlusEditText.getText().toString());
    			response.setShortText(numRSVP.toString());
    		}
    		else if (rsvpPlusButton.isChecked()) {
    			Integer numRSVP = 1;
    			response.setShortText(numRSVP.toString());
    		}
    		else if (rsvpNoButton.isChecked()) {
    			response.setShortText("0");
    		}
    	}
    	
		response.put(application, new VoidCallback() {
			@Override
			public void success() {
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
