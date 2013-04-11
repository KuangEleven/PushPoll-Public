package k11.pushpull.Fragments;

import k11.pushpull.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class PollDialogFragment extends DialogFragment {

	public interface PollDialogListener {
        public void onPollDialogPositiveClick(DialogFragment dialog, String question);
        public void onPollDialogNegativeClick(DialogFragment dialog);
	}
	
	PollDialogListener listener;
	private EditText editView;
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (PollDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement PollDialogListener");
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.polldialog_title);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View mainView = inflater.inflate(R.layout.polldialog, null);
		builder.setView(mainView);
		editView = (EditText) mainView.findViewById(R.id.question);
		builder.setPositiveButton(R.string.polldialog_Accept, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				listener.onPollDialogPositiveClick(PollDialogFragment.this,editView.getText().toString());
			}
		});
		builder.setNegativeButton(R.string.polldialog_Cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onPollDialogNegativeClick(PollDialogFragment.this);
			}
		});
		return builder.create();
	}

}
