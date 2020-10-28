package io.github.alansanchezp.gnomy.util.android;

import android.os.SystemClock;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    public void onView(OnViewActionPerformer<T> performer) {
        performer.onView(mView);
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

    public interface OnViewActionPerformer<T extends View> {
        void onView(T v);
    }
}
