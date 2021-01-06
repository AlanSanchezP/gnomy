package io.github.alansanchezp.gnomy.dummy;

import io.github.alansanchezp.gnomy.databinding.ActivityAccountHistoryBinding;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;

import android.os.Bundle;

public class BackButtonDummyActivity
    // Using this BindingClass just so that there is an appbar. Avoiding MainActivity
    // since navigation component causes it to crash.
        extends BackButtonActivity<ActivityAccountHistoryBinding> {

    public boolean actionsEnabled = true;

    public BackButtonDummyActivity() {
        super(null, true, ActivityAccountHistoryBinding::inflate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void disableActions() {
        super.disableActions();
        actionsEnabled = false;
    }

    @Override
    protected void enableActions() {
        super.enableActions();
        actionsEnabled = true;
    }

    public void simulatePendingOperations(boolean pendingOperations) {
        mOperationsPending = pendingOperations;
    }
}