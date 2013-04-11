package k11.pushpull.Callbacks;

import java.util.List;

import k11.pushpull.Data.Friend;

public interface FriendListCallback {
	void success(List<Friend> output);
	void failure(Exception e);
}
