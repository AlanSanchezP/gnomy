package io.github.alansanchezp.gnomy.ui;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.util.Objects;

import androidx.core.content.ContextCompat;
import io.github.alansanchezp.gnomy.R;

public abstract class BackButtonActivity
        extends GnomyActivity {

    public static final String TAG_BACK_DIALOG = "BackButtonActivity.BackConfirmationDialog";
    protected Drawable mUpArrowDrawable;
    protected boolean mOperationsPending = false;
    protected boolean mHandlingBackButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUpArrowDrawable = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24);
        //noinspection ConstantConditions
        getSupportActionBar().setHomeAsUpIndicator(mUpArrowDrawable);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableActions();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void finish() {
        if (mOperationsPending) finishPendingOperations();
        super.finish();
    }

    @Override
    public void onBackPressed() {
        disableActions();
        if (displayDialogOnBackPress() && !mHandlingBackButton) {
            mHandlingBackButton = true;
            ConfirmationDialogFragment df = new ConfirmationDialogFragment(
                    (ConfirmationDialogFragment.OnConfirmationDialogListener) this);
            df.show(getSupportFragmentManager(), TAG_BACK_DIALOG);
        } else if (!displayDialogOnBackPress()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void tintAppbar() {
        super.tintAppbar();
        tintBackArrow();
    }

    protected void tintBackArrow() {
        mUpArrowDrawable.setColorFilter(mThemeTextColor, PorterDuff.Mode.SRC_ATOP);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(mUpArrowDrawable);
    }

    @Override
    public void onConfirmationDialogYes(DialogInterface dialog, String dialogTag, int which) {
        if (dialogTag.equals(TAG_BACK_DIALOG)) {
            super.onBackPressed();
        }
    }
    @Override
    public void onConfirmationDialogDismiss(DialogInterface dialog, String dialogTag) {
        if (dialogTag.equals(TAG_BACK_DIALOG)) {
            if (!mOperationsPending) enableActions();
            mHandlingBackButton = false;
        }
    }

    @SuppressWarnings("EmptyMethod")
    protected void finishPendingOperations() {
        // @Override to implement custom actions
    }

    protected abstract boolean displayDialogOnBackPress();
}
