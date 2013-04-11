package k11.pushpull.Callbacks;

import k11.pushpull.Data.Response;

public interface ResponseCallback {
	void success(Response output);
	void failure(Exception e);
}
