package k11.pushpull.Callbacks;

public interface TokenCallback {
	void success(String token);
	void failure(Exception e);
}
