package k11.pushpull.Callbacks;

import k11.pushpull.Data.GroupMember;

public interface GroupMemberCallback {
	void success(GroupMember output);
	void failure(Exception e);
}
