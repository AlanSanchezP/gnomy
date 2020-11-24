package io.github.alansanchezp.gnomy.ui.transaction;

import android.content.Intent;
import android.os.Bundle;

import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.util.android.ViewTintingUtil;

public class AddEditTransactionActivity extends BackButtonActivity {
    public static final String EXTRA_TRANSACTION_TYPE = "AddEditTransactionActivity.TransactionType";
    public static final String EXTRA_TRANSACTION_ID = "AddEditTransactionActivity.TransactionId";
    private int mTransactionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int transactionId = intent.getIntExtra(EXTRA_TRANSACTION_ID, 0);
        mTransactionType = intent.getIntExtra(EXTRA_TRANSACTION_TYPE, MoneyTransaction.EXPENSE);
        if (mTransactionType == MoneyTransaction.EXPENSE) {
            if (transactionId == 0) setTitle(R.string.transaction_new_expense);
            else setTitle(R.string.transaction_modify_expense);
            setThemeColor(getResources().getColor(R.color.colorExpenses));
        } else if (mTransactionType == MoneyTransaction.INCOME) {
            if (transactionId == 0) setTitle(R.string.transaction_new_income);
            else setTitle(R.string.transaction_modify_income);
            setThemeColor(getResources().getColor(R.color.colorIncomes));
        }
    }

    @Override
    protected void tintWindowElements() {
        super.tintWindowElements();
        findViewById(R.id.addedit_transaction_container)
                .setBackgroundColor(mThemeColor);
        ViewTintingUtil
                .monotintTextInputLayout(findViewById(R.id.addedit_transaction_amount), mThemeTextColor);
        ViewTintingUtil
                .tintTextInputLayout(findViewById(R.id.addedit_transaction_concept), mThemeColor);
        ViewTintingUtil
                .tintTextInputLayout(findViewById(R.id.addedit_transaction_date), mThemeColor);
        ViewTintingUtil
                .tintTextInputLayout(findViewById(R.id.addedit_transaction_notes), mThemeColor);
        ViewTintingUtil
                .tintFAB(findViewById(R.id.addedit_transaction_FAB), mThemeColor, mThemeTextColor);
        ViewTintingUtil
                .tintSwitch(findViewById(R.id.addedit_transaction_mark_as_done), mThemeColor);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_add_edit_transaction;
    }

    @Override
    protected boolean displayDialogOnBackPress() {
        return true;
    }
}