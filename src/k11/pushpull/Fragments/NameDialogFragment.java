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

public class NameDialogFragment extends DialogFragment {
	
	public interface NameDialogListener {
        public void onNameDialogPositiveClick(DialogFragment dialog, String name);
        public void onNameDialogNegativeClick(DialogFragment dialog);
	}
	
	NameDialogListener listener;
	private EditText editView;
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (NameDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NameDialogListener");
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.namedialog_title);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View mainView = inflater.inflate(R.layout.namedialog, null);
		builder.setView(mainView);
		editView = (EditText) mainView.findViewById(R.id.name);
		builder.setPositiveButton(R.string.namedialog_Accept, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				listener.onNameDialogPositiveClick(NameDialogFragment.this,editView.getText().toString());
			}
		});
		builder.setNegativeButton(R.string.namedialog_Cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onNameDialogNegativeClick(NameDialogFragment.this);
			}
		});
		return builder.create();
	}
}