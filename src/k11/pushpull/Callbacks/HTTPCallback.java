package k11.pushpull.Callbacks;

public interface HTTPCallback {
	void success(String output);
	void failure(Exception e);
}
