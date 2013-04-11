package k11.pushpull.Fragments;

import k11.pushpull.R;
import k11.pushpull.Updateable;
import k11.pushpull.Data.Poll;
import k11.pushpull.Data.ResponseShort;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import k11.pushpull.HasPoll;

import com.actionbarsherlock.app.SherlockFragment;

public class PollRSVPSummary extends SherlockFragment implements Updateable {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.pollrsvpsummary, container, false);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	update();
    }
    
    public void update() {
    	Poll poll = ((HasPoll) getSherlockActivity()).getPoll();
    	if ((getView() != null) && (poll != null)) {
    		Integer rsvpTotal = 0;
    		Integer responsesTotal = 0;
    		Integer responsesAnswered = 0;
    		for (ResponseShort responseShort : poll.getShortResponses()) {
    			responsesTotal += 1;
    			if (responseShort.getCleanOutput() != null) {
    				rsvpTotal += Integer.parseInt(responseShort.getCleanOutput());
    				responsesAnswered += 1;
    			}
    		}
            TextView totalTextView = (TextView) getSherlockActivity().findViewById(R.id.total_text);
            totalTextView.setText(rsvpTotal.toString());
            
            TextView responsesTextView = (TextView) getSherlockActivity().findViewById(R.id.pollrsvpsummary_text);
            responsesTextView.setText(responsesAnswered + "/" + responsesTotal);
            
            ProgressBar progress = (ProgressBar) getSherlockActivity().findViewById(R.id.rsvpsummary_progress);
            progress.setMax(responsesTotal);
            progress.setProgress(responsesAnswered);
    	}
    }
}
