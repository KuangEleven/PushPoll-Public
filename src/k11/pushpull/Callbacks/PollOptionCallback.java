package k11.pushpull.Callbacks;

import k11.pushpull.Data.PollOption;

public interface PollOptionCallback {
	void success(PollOption output);
	void failure(Exception e);
}
