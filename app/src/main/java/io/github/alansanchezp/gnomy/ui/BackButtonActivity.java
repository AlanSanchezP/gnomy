package io.github.alansanchezp.gnomy.ui;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;

import java.util.Objects;
import java.util.function.Function;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewbinding.ViewBinding;
import io.github.alansanchezp.gnomy.R;

/**
 * Generic class that handles the implementation of a Back button
 * logic in Activity's appbar.
 *
 * @param <B>   ViewBinding class to use. See {@link GnomyActivity}.
 */
public abstract class BackButtonActivity<B extends ViewBinding>
        extends GnomyActivity<B> {

    public static final String TAG_BACK_DIALOG = "BackButtonActivity.BackConfirmationDialog";
    protected Drawable mUpArrowDrawable;
    protected boolean mOperationsPending = false;
    private final boolean mDisplayDialogOnBackPress;

    /**
     * Creates a new instance, initializing class fields and specifying
     * that ViewBinding will be used.
     *
     * @param menuResourceId            Menu to use in the appbar.
     *                                  Set to null if no menu is required.
     * @param viewBindingInflater       Inflater method to retrieve the ViewBinding object.
     *                                  In order to avoid using Java reflection, this must be
     *                                  provided by each child class. Lambda method
     *                                  references can be used as follows:
     *                                  ViewBindingClass::inflate
     */
    @SuppressWarnings("unused")
    protected BackButtonActivity(@Nullable Integer menuResourceId,
                                 boolean displayDialogOnBackPress,
                                 @NonNull Function<LayoutInflater, B> viewBindingInflater) {
        super(menuResourceId, viewBindingInflater);
        mDisplayDialogOnBackPress = displayDialogOnBackPress;
    }

    /**
     * Creates a new instance, initializing class fields and specifying
     * that traditional inflater method (with R.layout.ID) will be used.
     * Not recommended, but available if needed.
     *
     * @param menuResourceId                Menu to use in the host appbar.
     *                                      Set to null if no menu is required.
     * @param displayDialogOnBackPress      Specifies if a confirmation dialog
     *                                      should be displayed when user attemps
     *                                      to go back.
     * @param layoutResourceId              Layout resource id that will be used to
     *                                      inflate the Fragment View hierarchy.
     */
    @SuppressWarnings("unused")
    protected BackButtonActivity(@Nullable Integer menuResourceId,
                                 boolean displayDialogOnBackPress,
                                 @NonNull Integer layoutResourceId) {
        super(menuResourceId, layoutResourceId);
        mDisplayDialogOnBackPress = displayDialogOnBackPress;
    }

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
        if (mDisplayDialogOnBackPress) {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.findFragmentByTag(TAG_BACK_DIALOG) != null) return;
            ConfirmationDialogFragment dialog = (ConfirmationDialogFragment)
                    fm.getFragmentFactory().instantiate(
                            getClassLoader(), ConfirmationDialogFragment.class.getName());
            dialog.show(fm, TAG_BACK_DIALOG);
        } else {
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
    public void onConfirmationDialogCancel(DialogInterface dialog, String dialogTag) {
        if (dialogTag.equals(TAG_BACK_DIALOG)) {
            if (!mOperationsPending) enableActions();
        }
    }

    @SuppressWarnings("EmptyMethod")
    protected void finishPendingOperations() {
        // @Override to implement custom actions
    }
}
