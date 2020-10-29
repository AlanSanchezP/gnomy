package io.github.alansanchezp.gnomy.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import io.github.alansanchezp.gnomy.R;

public class ConfirmationDialogFragment extends DialogFragment {
    public static final String ARG_TITLE = "ConfirmationDialog.Title";
    public static final String ARG_MESSAGE = "ConfirmationDialog.Message";
    public static final String ARG_YES_STRING = "ConfirmationDialog.YesString";
    public static final String ARG_NO_STRING = "ConfirmationDialog.NoString";
    private final OnConfirmationDialogListener mListener;

    public ConfirmationDialogFragment() {
        throw new IllegalArgumentException("This class must be provided with an OnConfirmationDialogListener instance.");
    }

    public ConfirmationDialogFragment(OnConfirmationDialogListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = null;
        String message = null;
        String yesString = null;
        String noString = null;
        if (args != null) {
            title = args.getString(ARG_TITLE);
            message = args.getString(ARG_MESSAGE);
            yesString  = args.getString(ARG_YES_STRING);
            noString  = args.getString(ARG_NO_STRING);
        }
        if (title == null) title = getString(R.string.confirmation_dialog_title);
        if (message == null) message = getString(R.string.confirmation_dialog_description);
        if (yesString == null) yesString = getString(R.string.confirmation_dialog_yes);
        if (noString == null) noString = getString(R.string.confirmation_dialog_no);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(yesString, (dialog, which) ->
                    mListener.onConfirmationDialogYes(dialog, getDialogTag(), which))
                .setNegativeButton(noString, (dialog, which) ->
                        mListener.onConfirmationDialogNo(dialog, getDialogTag(), which));
        return builder.create();
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        mListener.onConfirmationDialogDismiss(dialog, getDialogTag());
        super.onDismiss(dialog);
    }

    protected final String getDialogTag() {
        String tag = getTag();
        if (tag == null || tag.equals("")) throw new IllegalArgumentException("Dialog must be provided with a tag");
        return tag;
    }

    public interface OnConfirmationDialogListener
            extends CustomDialogFragmentFactory.CustomDialogFragmentInterface {
        void onConfirmationDialogYes(DialogInterface dialog, String dialogTag, int which);
        void onConfirmationDialogNo(DialogInterface dialog, String dialogTag, int which);
        void onConfirmationDialogDismiss(DialogInterface dialog, String dialogTag);
    }

}
