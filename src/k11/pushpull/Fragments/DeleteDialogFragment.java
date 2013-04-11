package k11.pushpull.Fragments;

import k11.pushpull.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DeleteDialogFragment extends DialogFragment {
	public interface DeleteDialogListener {
        public void onDeleteDialogPositiveClick(DialogFragment dialog);
        public void onDeleteDialogNegativeClick(DialogFragment dialog);
	}
	
	DeleteDialogListener listener;
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (DeleteDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DeleteDialogListener");
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.deletedialog_text);
		builder.setPositiveButton(R.string.deletedialog_accept, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onDeleteDialogPositiveClick(DeleteDialogFragment.this);
			}
		});
		builder.setNegativeButton(R.string.deletedialog_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onDeleteDialogNegativeClick(DeleteDialogFragment.this);
			}
		});
		return builder.create();
	}
}
