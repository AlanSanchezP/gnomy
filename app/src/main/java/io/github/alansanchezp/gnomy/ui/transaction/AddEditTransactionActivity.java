package io.github.alansanchezp.gnomy.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.util.android.InputFilterMinMax;
import io.github.alansanchezp.gnomy.util.android.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.util.android.ViewTintingUtil;
import io.github.alansanchezp.gnomy.viewmodel.transaction.AddEditTransactionViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

// TODO: Implement recurrent transactions
// TODO: Can MaterialSpinner.setAdapter() help to improve spinner UX?
// TODO: Add links to create accounts and categories. Validate that both fields are not empty before submitting
public class AddEditTransactionActivity extends BackButtonActivity {
    public static final String EXTRA_TRANSACTION_TYPE = "AddEditTransactionActivity.TransactionType";
    public static final String EXTRA_TRANSACTION_ID = "AddEditTransactionActivity.TransactionId";
    private int mTransactionType;
    private TextInputLayout mTransactionConceptTIL;
    private TextInputEditText mTransactionConceptTIET;
    private TextInputLayout mAmountTIL;
    private TextInputEditText mAmountTIET;
    private TextInputLayout mDateTIL;
    private TextInputEditText mDateTIET;
    private MaterialSpinner mCurrencySpinner;
    private MaterialSpinner mCategorySpinner;
    private MaterialSpinner mAccountSpinner;
    private TextInputLayout mNotesTIL;
    private Switch mMarkAsDoneSwitch;
    private SingleClickViewHolder<FloatingActionButton> mFABVH;
    private AddEditTransactionViewModel mViewModel;
    private MoneyTransaction mTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this)
                .get(AddEditTransactionViewModel.class);
        mTransactionConceptTIL = findViewById(R.id.addedit_transaction_concept);
        mTransactionConceptTIET = findViewById(R.id.addedit_transaction_concept_input);
        mAmountTIL = findViewById(R.id.addedit_transaction_amount);
        mAmountTIET = findViewById(R.id.addedit_transaction_amount_input);
        mDateTIL = findViewById(R.id.addedit_transaction_date);
        mDateTIET = findViewById(R.id.addedit_transaction_date_input);
        mCurrencySpinner = findViewById(R.id.addedit_transaction_currency);
        mCategorySpinner = findViewById(R.id.addedit_transaction_category);
        mAccountSpinner = findViewById(R.id.addedit_transaction_from_account);
        mNotesTIL = findViewById(R.id.addedit_transaction_notes);
        TextInputEditText notesTIET = findViewById(R.id.addedit_transaction_notes_input);
        mMarkAsDoneSwitch = findViewById(R.id.addedit_transaction_mark_as_done);
        mFABVH = new SingleClickViewHolder<>(findViewById(R.id.addedit_transaction_FAB), true);

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
        LiveData<MoneyTransaction> ld = mViewModel.getTransaction(transactionId);
        if (ld == null) {
            MoneyTransaction transaction = new MoneyTransaction();
            // TODO: Implement dynamic picker instead of hardcoded now()
            // TODO: Set "confirmed" to false if future date (and block switch)
            // TODO: Restrict min date to account's creation
            transaction.setDate(DateUtil.OffsetDateTimeNow());
            onTransactionChanged(transaction);
        } else {
            ld.observe(this, this::onTransactionChanged);
        }

        // TODO: What if accounts or categories come BEFORE transaction to update?
        //  (and vice-versa)
        mViewModel.getAccounts().observe(this, this::onAccountsListChanged);
        mViewModel.getCategories().observe(this, this::onCategoriesListChanged);
        setCurrencySpinner();
        setInputFilters();
        mFABVH.setOnClickListener(this::processData);

        mAmountTIET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onTransactionAmountChanges(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mTransactionConceptTIET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onTransactionConceptChanges(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        notesTIET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTransaction.setNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void tintWindowElements() {
        super.tintWindowElements();
        findViewById(R.id.addedit_transaction_container)
                .setBackgroundColor(mThemeColor);
        ViewTintingUtil
                .monotintTextInputLayout(mAmountTIL, mThemeTextColor);
        ViewTintingUtil
                .tintTextInputLayout(mTransactionConceptTIL, mThemeColor);
        ViewTintingUtil
                .tintTextInputLayout(mDateTIL, mThemeColor);
        ViewTintingUtil
                .tintTextInputLayout(mNotesTIL, mThemeColor);
        // TODO: Tint with FAB variant
        mFABVH.onView(this, v -> ViewTintingUtil.tintFAB(v, mThemeColor, mThemeTextColor));
        ViewTintingUtil
                .tintSwitch(mMarkAsDoneSwitch, mThemeColor);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_add_edit_transaction;
    }

    @Override
    protected boolean displayDialogOnBackPress() {
        return true;
    }

    private void onTransactionAmountChanges(String value) {
        if (value.length() == 0) {
            if (mViewModel.transactionAmountIsPristine()) return;
            mAmountTIL.setError(getResources().getString(R.string.account_error_initial_value));
        } else {
            if (mTransaction != null) mTransaction.setOriginalValue(value);
            mAmountTIL.setErrorEnabled(false);
        }

        mViewModel.notifyTransactionAmountChanged();
    }

    private void onTransactionConceptChanges(String value) {
        if (mTransaction != null) mTransaction.setConcept(value);

        if (value.trim().length() == 0) {
            if (mViewModel.transactionConceptIsPristine()) return;
            mTransactionConceptTIL.setError(getResources().getString(R.string.account_error_name));
        } else {
            mTransactionConceptTIL.setErrorEnabled(false);
        }

        mViewModel.notifyTransactionConceptChanged();
    }

    private void onTransactionChanged(MoneyTransaction transaction) {
        mTransaction = transaction;
        mDateTIET.setText(transaction.getDate().toString());
        mTransaction.setType(mTransactionType);
        mDateTIET.setText(mTransaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        mMarkAsDoneSwitch.setChecked(transaction.isConfirmed());
        mMarkAsDoneSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                mTransaction.setConfirmed(isChecked));
    }

    private void onCategoriesListChanged(List<Category> categories) {
        mCategorySpinner.setItems(categories.toArray());
        mCategorySpinner.setOnItemSelectedListener((view, position, id, item) ->
                mTransaction.setCategory(categories.get(position).getId()));
        // TODO: Use stored value for updates
        mTransaction.setCategory(categories.get(0).getId());
    }

    private void onAccountsListChanged(List<Account> accounts) {
        mAccountSpinner.setItems(accounts.toArray());
        // TODO: Set account default currency as transaction currency after selection
        mAccountSpinner.setOnItemSelectedListener((view, position, id, item) ->
                mTransaction.setAccount(accounts.get(position).getId()));
        // TODO: Use stored value for updates
        mTransaction.setAccount(accounts.get(0).getId());
    }

    private void setCurrencySpinner() {
        try {
            String[] currencies = CurrencyUtil.getDisplayArray();
            mCurrencySpinner.setItems(currencies);
            mCurrencySpinner.setOnItemSelectedListener((view, position, id, item) ->
                    mTransaction.setCurrency(CurrencyUtil.getCurrencyCode(position)));
        } catch (GnomyCurrencyException e) {
            // This shouldn't happen
            Log.wtf("AddEditTransaction", "setLists: CURRENCIES array triggers error", e);
        }
    }

    private void setInputFilters() {
        mAmountTIET.setFilters(new InputFilter[]{new InputFilterMinMax(MoneyTransaction.MIN_INITIAL, MoneyTransaction.MAX_INITIAL, BigDecimalUtil.DECIMAL_SCALE)});
    }

    private boolean validateTextFields() {
        mViewModel.notifyTransactionAmountChanged();
        mViewModel.notifyTransactionConceptChanged();

        String amountString = Objects.requireNonNull(mAmountTIET.getText()).toString();
        String transactionConcept = Objects.requireNonNull(mTransactionConceptTIET.getText()).toString();
        onTransactionAmountChanges(amountString);
        onTransactionConceptChanges(transactionConcept);

        return transactionConcept.length() > 0
                && amountString.length() > 0;
    }

    private void processData(View v) {
        boolean texFieldsAreValid = validateTextFields();

        if (texFieldsAreValid) {
            saveData();
        } else {
            Toast.makeText(this, getResources().getString(R.string.form_error), Toast.LENGTH_LONG).show();
            mFABVH.notifyOnAsyncOperationFinished();
        }
    }

    private void saveData() {
        try {
            Disposable disposable;
            if (mTransaction.getId() == 0) {
                disposable = mViewModel.insert(mTransaction)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                longs -> {
                                    Toast.makeText(this, R.string.transaction_message_saved, Toast.LENGTH_LONG).show();
                                    finish();
                                },
                                throwable -> {
                                    Toast.makeText(this, R.string.generic_data_error, Toast.LENGTH_LONG).show();
                                    mFABVH.notifyOnAsyncOperationFinished();
                                });

            } else {
                disposable = mViewModel.update(mTransaction)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                integer -> {
                                    Toast.makeText(this, R.string.transaction_message_updated, Toast.LENGTH_LONG).show();
                                    finish();
                                },
                                throwable -> {
                                    Toast.makeText(this, R.string.generic_data_error, Toast.LENGTH_LONG).show();
                                    mFABVH.notifyOnAsyncOperationFinished();
                                });
            }
            mCompositeDisposable.add(disposable);
        } catch(NumberFormatException nfe) {
            Log.wtf("AddEditTransaction", "saveData: TextField validations failed", nfe);
            mFABVH.notifyOnAsyncOperationFinished();
        }
    }
}