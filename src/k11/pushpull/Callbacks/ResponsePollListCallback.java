package k11.pushpull.Callbacks;

import java.util.List;

import k11.pushpull.Data.Poll;
import k11.pushpull.Data.Response;

public interface ResponsePollListCallback {
	void success(List<Response> responseList, List<Poll> pollList);
	void failure(Exception e);
}