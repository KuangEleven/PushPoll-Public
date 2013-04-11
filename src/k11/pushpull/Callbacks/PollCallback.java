package k11.pushpull.Callbacks;

import k11.pushpull.Data.Poll;

public interface PollCallback {
	void success(Poll output);
	void failure(Exception e);
}
