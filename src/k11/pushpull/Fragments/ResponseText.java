package k11.pushpull.Fragments;

import k11.pushpull.HasPoll;
import k11.pushpull.HasResponse;
import k11.pushpull.PushPullApplication;
import k11.pushpull.R;
import k11.pushpull.Updateable;
import k11.pushpull.Callbacks.VoidCallback;
import k11.pushpull.Data.Poll;
import k11.pushpull.Data.Response;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class ResponseText extends SherlockFragment implements Updateable {
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.responsesingleselect, container, false);
    	return inflater.inflate(R.layout.responsetext, container, false);
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
            if (poll.getState() > Poll.ACTIVE) {
            	EditText responseTextView = (EditText) getSherlockActivity().findViewById(R.id.response_text);
            	responseTextView.setClickable(false);
            	responseTextView.setCursorVisible(false);
            	responseTextView.setFocusable(false);
            	responseTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS); //Don't auto-correct what they can't change!
            }
    	
			TextView questionTextView = (TextView) getSherlockActivity().findViewById(R.id.question_text);
			questionTextView.setText(poll.getQuestion());
			
			if (response.getShortText() != null && response.getShortText().length() > 0) {
				EditText responseTextView = (EditText) getSherlockActivity().findViewById(R.id.response_text);
				responseTextView.setText(response.getShortText());
			}
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	final Response response = ((HasResponse) getSherlockActivity()).getResponse();
    	EditText responseTextView = (EditText) getSherlockActivity().findViewById(R.id.response_text);
    	final PushPullApplication application = (PushPullApplication) getSherlockActivity().getApplication();
    	
		response.setShortText(responseTextView.getText().toString());
		
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
