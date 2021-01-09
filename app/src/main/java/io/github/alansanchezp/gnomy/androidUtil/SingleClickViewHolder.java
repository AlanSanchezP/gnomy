package io.github.alansanchezp.gnomy.androidUtil;

import android.app.Activity;
import android.os.SystemClock;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

/**
 * Helper class to prevent double clicks on clickable elements.
 * @param <T>   Class of the hosted clickable element.
 */
public class SingleClickViewHolder<T extends View> {
    private final T mView;
    private final boolean mIsAsync;
    private final boolean mKeepElevationOnClick;
    private final boolean mDisableDuringProcessing;
    private boolean mAsyncInProcess;
    private long mLastClickTime;

    public SingleClickViewHolder(T view) {
        this(view, false);
    }

    public SingleClickViewHolder(T view,
                                 boolean isAsync) {
        this(view, isAsync, false);
    }

    public SingleClickViewHolder(T view,
                                 boolean isAsync,
                                 boolean disableDuringProcessing) {
        this(view, isAsync, disableDuringProcessing, false);
    }

    /**
     *
     * @param view      Original view.
     * @param isAsync   If true, clicks will not be re-enabled automatically,
     *                  and the programmer will have to enable them manually.
     * @param disableDuringProcessing   If true, both setClickable() and setEnabled()
     *                                  will be used called in the embedded view, otherwise
     *                                  only setClickable() will get called. Important
     *                                  to change the View's appearance.
     * @param keepElevationOnClick      If true, the DEFAULT CLASS elevation of the embedded view
     *                                  will be preserved after it gets clicked. Only used if
     *                                  disableDuringProcessing = true.
     */
    public SingleClickViewHolder(T view,
                                 boolean isAsync,
                                 boolean disableDuringProcessing,
                                 boolean keepElevationOnClick) {
        mView = view;
        mIsAsync = isAsync;
        mDisableDuringProcessing = disableDuringProcessing;
        mKeepElevationOnClick = keepElevationOnClick;
    }

    /**
     * Bridge method to perform any operation over the embedded {@link View}.
     * This is meant to be used for coloring and toggling visibility purposes,
     * but any operation is technically possible.
     *
     * @param hostActivity  Reference to the host {@link Activity} the View
     *                      resides on. Necessary to perform the operations
     *                      and avoid ViewRootImpl$CalledFromWrongThreadException
     * @param consumer      Operations to be performed on the embedded View.
     */
    public void onView(@NonNull Activity hostActivity, Consumer<T> consumer) {
        hostActivity.runOnUiThread(() -> consumer.accept(mView));
    }

    /**
     * Wrapper method for {@link View#setOnClickListener(View.OnClickListener)}.
     * Adds custom checks before the actual click listener gets called, in order
     * to prevent unwanted clicks.
     *
     * @param listener  Click listener that would normally be passed directly to View.
     */
    public void setOnClickListener(View.OnClickListener listener) {
        mView.setOnClickListener(v -> {
            if (mIsAsync && mAsyncInProcess) return;
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1500) return;

            // If the item was not clicked too recently and no async operation is in process
            // we proceed to actually process the click listener.
            // First, future clicks on view are prevented and we reset mLastClickTime
            blockClicks();
            mLastClickTime = SystemClock.elapsedRealtime();

            if (mIsAsync) mAsyncInProcess = true;

            listener.onClick(v);

            // If the action is asynchronous, clicks have to be allowed manually
            if (!mIsAsync) allowClicks();
        });
    }

    /**
     * Uses native {@link View} methods to disable the embedded View.
     * Used so that ripple animations don't get triggered (since keeping them
     * while not processing the actual click listener would confuse the user),
     * as well as to support asynchronous operations.
     *
     */
    public void blockClicks() {
        mView.setClickable(false);
        if (!mDisableDuringProcessing) return;

        mView.setEnabled(false);
        if (mView instanceof FloatingActionButton) {
            if (mKeepElevationOnClick) {
                mView.setElevation(6f);
            }
        }
    }

    /**
     *  Uses native {@link View} methods to enable clicks on the embedded View.
     *
     *  Avoid using it directly for asynchronous tasks, use {@link #notifyOnAsyncOperationFinished()} instead.
     */
    public void allowClicks() {
        mView.setClickable(true);
        if (mDisableDuringProcessing) mView.setEnabled(true);
    }

    /**
     *  Indicates that the asynchronous operations triggered by the click listener
     *  have finished so that the embedded View can get enabled again.
     *
     *  Does nothing if the object was not told to handle asynchronous operations.
     */
    public void notifyOnAsyncOperationFinished() {
        if (!mIsAsync) return;
        if (!mAsyncInProcess) return;
        mAsyncInProcess = false;
        allowClicks();
    }
}
