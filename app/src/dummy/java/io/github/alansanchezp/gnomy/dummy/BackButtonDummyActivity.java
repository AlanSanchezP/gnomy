package io.github.alansanchezp.gnomy.dummy;

import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;

import android.os.Bundle;

public class BackButtonDummyActivity
        extends BackButtonActivity {

    public boolean actionsEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean displayDialogOnBackPress() {
        return true;
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