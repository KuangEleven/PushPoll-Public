package k11.pushpull.Callbacks;

import k11.pushpull.Data.Group;

public interface GroupCallback {
	void success(Group output);
	void failure(Exception e);
}
