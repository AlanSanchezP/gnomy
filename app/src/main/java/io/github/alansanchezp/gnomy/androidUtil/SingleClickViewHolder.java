package io.github.alansanchezp.gnomy.androidUtil;

import android.app.Activity;
import android.os.SystemClock;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

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
     * Bridge class to perform any operation over the embedded {@link View}.
     * This is meant to be used for coloring and toggling visibility purposes,
     * but any operation is technically possible.
     *
     * @param hostActivity  Reference to the host {@link Activity} the View
     *                      resides on. Necessary to perform the operations
     *                      and avoid ViewRootImpl$CalledFromWrongThreadException
     * @param performer     Interface that contains the operations to be
     *                      performed on the embedded View.
     */
    public void onView(@NonNull Activity hostActivity, Consumer<T> performer) {
        hostActivity.runOnUiThread(() -> performer.accept(mView));
    }

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

    public void allowClicks() {
        mView.setClickable(true);
        if (mDisableDuringProcessing) mView.setEnabled(true);
    }

    // Has to be called manually on view owner due to async operations
    public void notifyOnAsyncOperationFinished() {
        if (!mIsAsync) return;
        if (!mAsyncInProcess) return;
        mAsyncInProcess = false;
        allowClicks();
    }
}
