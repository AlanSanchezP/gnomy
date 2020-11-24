package io.github.alansanchezp.gnomy.ui.transaction;

import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;

public class AddEditTransactionActivity extends BackButtonActivity {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_add_edit_transaction;
    }

    @Override
    protected boolean displayDialogOnBackPress() {
        return true;
    }
}