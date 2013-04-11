package k11.pushpull.Callbacks;

import java.util.List;

import k11.pushpull.Data.Response;

public interface ResponseListCallback {
	void success(List<Response> output);
	void failure(Exception e);
}
