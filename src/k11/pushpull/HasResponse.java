package k11.pushpull;

import k11.pushpull.Data.Response;

public interface HasResponse {
	Response getResponse();
	void updateResponse(Integer responseID);
}
