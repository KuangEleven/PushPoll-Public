package k11.pushpull.Callbacks;

import k11.pushpull.Data.User;

public interface UserCallback {
	void success(User user);
	void failure(Exception e);
}
