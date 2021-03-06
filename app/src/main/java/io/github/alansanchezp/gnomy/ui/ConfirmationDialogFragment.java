package io.github.alansanchezp.gnomy.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.databinding.DialogConfirmationBinding;
import io.github.alansanchezp.gnomy.androidUtil.SingleClickViewHolder;

/**
 * Custom alert dialog for simple yes/no operations.
 * It uses a custom layout instead of wrapping an AlertDialog
 * so that it can show on top of other dialogs.
 */
public class ConfirmationDialogFragment extends DialogFragment {
    public static final String ARG_TITLE = "ConfirmationDialog.Title";
    public static final String ARG_MESSAGE = "ConfirmationDialog.Message";
    public static final String ARG_YES_STRING = "ConfirmationDialog.YesString";
    public static final String ARG_NO_STRING = "ConfirmationDialog.NoString";
    public static final String ARG_IS_ASYNC = "ConfirmationDialog.IsAsync";
    private final OnConfirmationDialogListener mListener;
    private String mTitle, mMessage, mYesString, mNoString;
    private boolean mIsAsync = false;

    public ConfirmationDialogFragment() {
        throw new IllegalArgumentException("This class must be provided with an OnConfirmationDialogListener instance.");
    }

    public ConfirmationDialogFragment(OnConfirmationDialogListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mTitle = args.getString(ARG_TITLE);
            mMessage = args.getString(ARG_MESSAGE);
            mYesString  = args.getString(ARG_YES_STRING);
            mNoString  = args.getString(ARG_NO_STRING);
            mIsAsync = args.getBoolean(ARG_IS_ASYNC, false);
        }
        if (mTitle == null) mTitle = getString(R.string.confirmation_dialog_title);
        if (mMessage == null) mMessage = getString(R.string.confirmation_dialog_description);
        if (mYesString == null) mYesString = getString(R.string.confirmation_dialog_yes);
        if (mNoString == null) mNoString = getString(R.string.confirmation_dialog_no);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_Alert);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DialogConfirmationBinding $ = DialogConfirmationBinding.inflate(inflater, container, false);
        Dialog dialog = getDialog();

        $.confirmationDialogMessage.setText(mMessage);

        SingleClickViewHolder<Button> yesBtn
                = new SingleClickViewHolder<>($.confirmationDialogYes);
        SingleClickViewHolder<Button> noBtn
                = new SingleClickViewHolder<>($.confirmationDialogNo);
        yesBtn.onView(requireActivity(), v -> v.setText(mYesString));
        noBtn.onView(requireActivity(), v -> v.setText(mNoString));

        if (dialog != null) {
            dialog.setTitle(mTitle);

            yesBtn.setOnClickListener(v -> {
                mListener.onConfirmationDialogYes(dialog, getDialogTag(), Dialog.BUTTON_POSITIVE);
                if(!mIsAsync) dialog.dismiss();
            });

            noBtn.setOnClickListener(v -> {
                mListener.onConfirmationDialogNo(dialog, getDialogTag(), Dialog.BUTTON_NEGATIVE);
                dialog.cancel();
            });
        }
        return $.getRoot();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        mListener.onConfirmationDialogCancel(dialog, getDialogTag());
        super.onCancel(dialog);
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

    /**
     * Helper interface to allow communication between dialog and its caller.
     */
    public interface OnConfirmationDialogListener {
        void onConfirmationDialogYes(DialogInterface dialog, String dialogTag, int which);
        void onConfirmationDialogNo(DialogInterface dialog, String dialogTag, int which);
        void onConfirmationDialogCancel(DialogInterface dialog, String dialogTag);
        void onConfirmationDialogDismiss(DialogInterface dialog, String dialogTag);
    }

}
