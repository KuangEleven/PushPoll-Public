package k11.pushpull;

import k11.pushpull.Data.Poll;

public interface HasPoll { //To allow for using same fragment in both PollView and ResponseView. Some, more coupled fragments don't fully use this
	Poll getPoll();
	void updatePoll(Integer pollID);
}
