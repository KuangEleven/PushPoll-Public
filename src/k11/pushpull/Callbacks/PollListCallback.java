package k11.pushpull.Callbacks;

import java.util.List;

import k11.pushpull.Data.Poll;


public interface PollListCallback {
	void success(List<Poll> output);
	void failure(Exception e);
}
