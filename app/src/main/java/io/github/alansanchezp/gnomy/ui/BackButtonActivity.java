package io.github.alansanchezp.gnomy.ui;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import io.github.alansanchezp.gnomy.R;

public abstract class BackButtonActivity
        extends GnomyActivity {

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
            // TODO: Use DialogFragment to recreate fragment on rotation
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.confirmation_dialog_title))
                    .setMessage(getString(R.string.confirmation_dialog_description))
                    .setPositiveButton(getString(R.string.confirmation_dialog_yes), (dialog, which) -> super.onBackPressed())
                    .setNegativeButton(getString(R.string.confirmation_dialog_no), null)
                    .setOnDismissListener(dialog -> {
                        if (!mOperationsPending) enableActions();
                        mHandlingBackButton = false;
                    })
                    .show();
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

    @SuppressWarnings("EmptyMethod")
    protected void finishPendingOperations() {
        // @Override to implement custom actions
    }

    protected abstract boolean displayDialogOnBackPress();
}
