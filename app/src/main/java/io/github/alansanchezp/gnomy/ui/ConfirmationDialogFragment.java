package io.github.alansanchezp.gnomy.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.util.android.SingleClickViewHolder;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirmation_dialog, container, false);
        Dialog dialog = getDialog();

        ((TextView)view.findViewById(R.id.confirmation_dialog_message))
                .setText(mMessage);

        SingleClickViewHolder<Button> yesBtn
                = new SingleClickViewHolder<>(view.findViewById(R.id.confirmation_dialog_yes));
        SingleClickViewHolder<Button> noBtn
                = new SingleClickViewHolder<>(view.findViewById(R.id.confirmation_dialog_no));
        yesBtn.onView(v -> v.setText(mYesString));
        noBtn.onView(v -> v.setText(mNoString));

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
        return view;
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

    public interface OnConfirmationDialogListener
            extends CustomDialogFragmentFactory.CustomDialogFragmentInterface {
        void onConfirmationDialogYes(DialogInterface dialog, String dialogTag, int which);
        void onConfirmationDialogNo(DialogInterface dialog, String dialogTag, int which);
        void onConfirmationDialogCancel(DialogInterface dialog, String dialogTag);
        void onConfirmationDialogDismiss(DialogInterface dialog, String dialogTag);
    }

}
