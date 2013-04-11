package k11.pushpull.Callbacks;

import java.util.List;

import k11.pushpull.Data.Group;

public interface GroupListCallback {
	void success(List<Group> output);
	void failure(Exception e);
}
