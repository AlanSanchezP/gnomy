package io.github.alansanchezp.gnomy.ui.transaction;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.maltaisn.calcdialog.CalcDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.ui.GnomyFragmentFactory;
import io.github.alansanchezp.gnomy.ui.account.AddEditAccountActivity;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.ColorUtil;
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

import static io.github.alansanchezp.gnomy.util.android.SimpleTextWatcherWrapper.onlyOnTextChanged;

// TODO: Implement recurrent transactions
// TODO: Can MaterialSpinner.setAdapter() help to improve spinner UX?
public class AddEditTransactionActivity
        extends BackButtonActivity
        implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        CalcDialog.CalcDialogCallback {
    public static final String EXTRA_TRANSACTION_TYPE = "AddEditTransactionActivity.TransactionType";
    public static final String EXTRA_TRANSACTION_ID = "AddEditTransactionActivity.TransactionId";
    public static final String TAG_DATE_DIALOG = "AddEditTransactionActivity.DateDialog";
    public static final String TAG_TIME_DIALOG = "AddEditTransactionActivity.TimeDialog";
    public static final String TAG_CALCULATOR_DIALOG = "AddEditTransactionActivity.CalculatorDialog";
    private int mTransactionType;
    private LinearLayout mBoxLayout;
    private TextInputLayout mTransactionConceptTIL;
    private TextInputEditText mTransactionConceptTIET;
    private TextInputLayout mAmountTIL;
    private TextInputEditText mAmountTIET;
    private TextInputLayout mDateTIL;
    private TextInputEditText mDateTIET;
    private Switch mDateTimeSwitch;
    private MaterialSpinner mCurrencySpinner;
    private MaterialSpinner mCategorySpinner;
    private MaterialSpinner mAccountSpinner;
    private TextInputLayout mNotesTIL;
    private TextInputEditText mNotesTIET;
    private Switch mMarkAsDoneSwitch;
    private SingleClickViewHolder<FloatingActionButton> mFABVH;
    private AddEditTransactionViewModel mViewModel;
    private MoneyTransaction mTransaction;
    private boolean mIsNewScreen = true;
    private OffsetDateTime mAccountMinDate;

    // Flags for async operations
    private List<Account> mAccountsList;
    private List<Category> mCategoriesList;

    @Override
    protected GnomyFragmentFactory getFragmentFactory() {
        return super.getFragmentFactory()
                .addMapElement(DatePickerDialog.class, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this)
                .get(AddEditTransactionViewModel.class);
        mBoxLayout = findViewById(R.id.addedit_transaction_box);
        mTransactionConceptTIL = findViewById(R.id.addedit_transaction_concept);
        mTransactionConceptTIET = findViewById(R.id.addedit_transaction_concept_input);
        mAmountTIL = findViewById(R.id.addedit_transaction_amount);
        mAmountTIET = findViewById(R.id.addedit_transaction_amount_input);
        mDateTIL = findViewById(R.id.addedit_transaction_date);
        mDateTIET = findViewById(R.id.addedit_transaction_date_input);
        mDateTimeSwitch = findViewById(R.id.addedit_transaction_include_time);
        mCurrencySpinner = findViewById(R.id.addedit_transaction_currency);
        mCategorySpinner = findViewById(R.id.addedit_transaction_category);
        mAccountSpinner = findViewById(R.id.addedit_transaction_from_account);
        mNotesTIL = findViewById(R.id.addedit_transaction_notes);
        mNotesTIET = findViewById(R.id.addedit_transaction_notes_input);
        mMarkAsDoneSwitch = findViewById(R.id.addedit_transaction_mark_as_done);
        mFABVH = new SingleClickViewHolder<>(findViewById(R.id.addedit_transaction_FAB), true);
        // TODO: Find a better way to present these as actions
        TextView newAccountTV = findViewById(R.id.addedit_transaction_new_account);
        TextView newCategoryTV = findViewById(R.id.addedit_transaction_new_category);
        newAccountTV.setPaintFlags(newAccountTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        newCategoryTV.setPaintFlags(newCategoryTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        newAccountTV.setOnClickListener(this::openNewAccountActivity);
        newCategoryTV.setOnClickListener(this::openNewCategoryActivity);

        Intent intent = getIntent();
        int transactionId = intent.getIntExtra(EXTRA_TRANSACTION_ID, 0);
        mTransactionType = intent.getIntExtra(EXTRA_TRANSACTION_TYPE, MoneyTransaction.EXPENSE);
        // TODO: Support for transfers
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
            transaction.setDate(DateUtil.OffsetDateTimeNow());
            transaction.setType(mTransactionType);
            onTransactionChanged(transaction);
        } else {
            mIsNewScreen = false;
            mBoxLayout.setVisibility(View.INVISIBLE);
            mFABVH.onView(this, v -> v.setVisibility(View.INVISIBLE));
            ld.observe(this, this::onTransactionChanged);
        }

        setCurrencySpinner();
        setInputFilters();
        mViewModel.getAccounts().observe(this, this::onAccountsListChanged);
        mViewModel.getCategories().observe(this, this::onCategoriesListChanged);
        mFABVH.setOnClickListener(this::processData);
        mAmountTIL.setEndIconOnClickListener(this::openCalculator);
        mDateTIL.setEndIconOnClickListener(this::openDatePicker);
        // As data gets reset to its stored value after rotation it is possible
        //  that date and/or time pickers allow selection of an invalid date,
        //  and then FAB is expected to trigger an error on date field.
        //  If this happens, it would be impossible for the user to select
        //  a valid date if error icon doesn't hold a callback too.
        mDateTIL.setErrorIconOnClickListener(this::openDatePicker);
        mAmountTIL.setErrorIconOnClickListener(this::openCalculator);
        mDateTimeSwitch.setOnCheckedChangeListener((btn, b) -> updateDateText());

        mAmountTIET.addTextChangedListener(onlyOnTextChanged((s, start, count, after) ->
                onTransactionAmountChanges(s.toString())));
        mTransactionConceptTIET.addTextChangedListener(onlyOnTextChanged((s, start, count, after) ->
                onTransactionConceptChanges(s.toString())));
        mNotesTIET.addTextChangedListener(onlyOnTextChanged((s, start, count, after) -> {
            if (mTransaction != null)
                mTransaction.setNotes(s.toString());
        }));
    }

    @Override
    protected void disableActions() {
        super.disableActions();
        mFABVH.blockClicks();
    }

    @Override
    protected void enableActions() {
        super.enableActions();
        mFABVH.allowClicks();
    }

    @Override
    protected void tintWindowElements() {
        super.tintWindowElements();
        int fabBgColor = ColorUtil.getVariantByFactor(mThemeColor, 0.86f);
        int fabTextColor = ColorUtil.getTextColor(fabBgColor);

        findViewById(R.id.addedit_transaction_container)
                .setBackgroundColor(mThemeColor);
        ViewTintingUtil
                .monotintTextInputLayout(mAmountTIL, mThemeTextColor);
        ViewTintingUtil
                .tintTextInputLayout(mTransactionConceptTIL, mThemeColor);
        ViewTintingUtil
                .tintTextInputLayout(mDateTIL, mThemeColor);
        ViewTintingUtil
                .tintSwitch(mDateTimeSwitch, mThemeColor);
        ViewTintingUtil
                .tintTextInputLayout(mNotesTIL, mThemeColor);
        mFABVH.onView(this, v ->
                ViewTintingUtil.tintFAB(v, fabBgColor, fabTextColor));
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
            mAmountTIL.setError(getResources().getString(R.string.transaction_error_amount));
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
            mTransactionConceptTIL.setError(getResources().getString(R.string.transaction_error_concept));
        } else {
            mTransactionConceptTIL.setErrorEnabled(false);
        }

        mViewModel.notifyTransactionConceptChanged();
    }

    private void tryToForceConfirmedStatus() {
        // TODO: How can we test this behavior?
        if (mTransaction == null) return;
        if (mTransaction.getDate().isAfter(OffsetDateTime.now())) {
            boolean previousSelectedState = mTransaction.isConfirmed();
            mMarkAsDoneSwitch.setChecked(false); // event listener will update mTransaction too
            mMarkAsDoneSwitch.setEnabled(false);
            // event listener will set status as false, we restore it manually here
            mViewModel.setUserSelectedConfirmedStatus(previousSelectedState);
        } else {
            mMarkAsDoneSwitch.setEnabled(true);
            mMarkAsDoneSwitch.setChecked(mViewModel.getUserSelectedConfirmedStatus()); // event listener will update mTransaction too
        }
    }

    private void updateDateText() {
        if (mTransaction == null) return;
        mDateTIET.setText(DateUtil.getOffsetDateTimeString(mTransaction.getDate(),
                mDateTimeSwitch.isChecked()));
    }

    private void onTransactionChanged(MoneyTransaction transaction) {
        // TODO: Evaluate if this approach is better than finish() the Activity
        if (transaction == null)
            throw new RuntimeException("Attempting to update non-existent Transaction.");
        if (transaction.getType() != mTransactionType)
            throw new RuntimeException("Attempting an invalid operation: Changing a transaction's type.");
        // TODO: Should we keep edited data on rotation?
        //  An option could be to force portrait mode on form activities
        mTransaction = transaction;
        mViewModel.setUserSelectedConfirmedStatus(transaction.isConfirmed());
        tryToForceConfirmedStatus();
        updateDateText();
        mMarkAsDoneSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mTransaction.setConfirmed(isChecked);
            mViewModel.setUserSelectedConfirmedStatus(isChecked);
        });
        if(!mIsNewScreen) {
            mAmountTIET.setText(transaction.getOriginalValue().toPlainString());
            mTransactionConceptTIET.setText(transaction.getConcept());
            mNotesTIET.setText(transaction.getNotes());
            tryToDisplayContainer();
        }
    }

    private void onCategoriesListChanged(List<Category> categories) {
        if (categories == null || categories.size() == 0)
            throw new RuntimeException("Categories list is not meant to be empty.");
        mCategoriesList = categories;
        mCategorySpinner.setItems(categories.toArray());
        mCategorySpinner.setOnItemSelectedListener((view, position, id, item) ->
                mTransaction.setCategory(categories.get(position).getId()));
        if (mIsNewScreen)
            mTransaction.setCategory(categories.get(0).getId());
        else
            tryToDisplayContainer();
    }

    private void setAccountAndData(Account account) {
        mTransaction.setAccount(account.getId());
        mAccountMinDate = account.getCreatedAt();
        if (mAccountMinDate.isAfter(mTransaction.getDate())) {
            mTransaction.setDate(mAccountMinDate);
            tryToForceConfirmedStatus();
            updateDateText();
        }
        setAccountCurrency(account);
    }

    private void setAccountCurrency(Account account) {
        if (mIsNewScreen)
            mCurrencySpinner.setSelectedIndex(
                    CurrencyUtil.getCurrencyIndex(
                            account.getDefaultCurrency()));
    }

    private void onAccountsListChanged(List<Account> _accounts) {
        // Prevent NullPointerException
        final List<Account> accounts;
        if (_accounts == null) accounts = new ArrayList<>();
        else accounts = _accounts;

        mAccountsList = accounts;
        mAccountSpinner.setItems(accounts.toArray());

        mAccountSpinner.setError(null);
        mAccountSpinner.setHintTextColor(getResources().getColor(R.color.colorTextSecondary));
        mAccountSpinner.setHint(R.string.transaction_from_account);

        mAccountSpinner.setOnItemSelectedListener((view, position, id, item) -> {
            Account selectedAccount = accounts.get(position);
            setAccountAndData(selectedAccount);
        });

        if (mViewModel.accountsListHasArrivedBefore() && accounts.size() > 0) {
            // Sets last account as selected
            mAccountSpinner.setSelectedIndex(accounts.size() - 1);
            setAccountAndData(accounts.get(accounts.size() - 1));
        } else if (mIsNewScreen && accounts.size() > 0) {
            // Prevent IndexOutOfBoundsException
            setAccountAndData(accounts.get(0));
        }
        tryToDisplayContainer();
        mViewModel.notifyAccountsListFirstArrival();
    }

    // TODO: MediatorLiveData is probably a better approach
    private void tryToDisplayContainer() {
        if (mIsNewScreen) return;
        if (mAccountsList == null ||
            mCategoriesList == null ||
            mTransaction == null) return;
        if (!mViewModel.accountsListHasArrivedBefore()) {
            Account selectedAccount = mAccountsList.get(
                    getAccountListIndex(mTransaction.getAccount()));
            setAccountAndData(selectedAccount);
            mCurrencySpinner.setSelectedIndex(CurrencyUtil.getCurrencyIndex(mTransaction.getCurrency()));
            mAccountSpinner.setSelectedIndex(getAccountListIndex(mTransaction.getAccount()));
            mCategorySpinner.setSelectedIndex(getCategoryListIndex(mTransaction.getCategory()));
        }
        mBoxLayout.setVisibility(View.VISIBLE);
        mFABVH.onView(this, v -> v.setVisibility(View.VISIBLE));
    }

    // TODO: Can refactor these methods using reflection?
    private int getAccountListIndex(int accountId) {
        for (int i = 0; i < mAccountsList.size(); i++) {
            if (mAccountsList.get(i).getId() == accountId) {
                return i;
            }
        }
        return -1;
    }

    private int getCategoryListIndex(int categoryId) {
        for (int i = 0; i < mCategoriesList.size(); i++) {
            if (mCategoriesList.get(i).getId() == categoryId) {
                return i;
            }
        }
        return -1;
    }

    private void setCurrencySpinner() {
        try {
            String[] currencies = CurrencyUtil.getDisplayArray();
            mCurrencySpinner.setItems(currencies);
            mCurrencySpinner.setOnItemSelectedListener((view, position, id, item) ->
                mTransaction.setCurrency(CurrencyUtil.getCurrencyCode(position)));
            // TODO: Use global default currency (WHEN IMPLEMENTED)
            if (mIsNewScreen) {
                mTransaction.setCurrency(currencies[0]);
            }
        } catch (GnomyCurrencyException e) {
            // This shouldn't happen
            Log.wtf("AddEditTransaction", "setLists: CURRENCIES array triggers error", e);
        }
    }

    private void setInputFilters() {
        mAmountTIET.setFilters(new InputFilter[]{new InputFilterMinMax(MoneyTransaction.MIN_VALUE, MoneyTransaction.MAX_VALUE, BigDecimalUtil.DECIMAL_SCALE)});
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

    private boolean validateAccountSpinner() {
        if (mAccountsList == null || mAccountsList.size() == 0) {
            // MaterialSpinner doesn't have custom setError() implementation,
            //  and doesn't show the message, but espresso can still test it.
            mAccountSpinner.setError(getResources().getString(R.string.transaction_error_account));
            mAccountSpinner.setHintTextColor(getResources().getColor(R.color.colorError));
            mAccountSpinner.setHint(R.string.transaction_error_account);
            return false;
        }
        return true;
    }

    private void processData(View v) {
        boolean texFieldsAreValid = validateTextFields();
        boolean selectedAccountIsNotNull = validateAccountSpinner();

        if (texFieldsAreValid  && selectedAccountIsNotNull) {
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

    private void openDatePicker(View v) {
        Calendar originalDate = Calendar.getInstance();
        originalDate.set(mTransaction.getDate().getYear(),
                // OffsetDateTime uses month numbers from 1-12
                // but Calendar returns numbers from 0-11
                mTransaction.getDate().getMonthValue()-1,
                mTransaction.getDate().getDayOfMonth());
        DatePickerDialog dialog = DatePickerDialog.newInstance(
                this, originalDate);
        if (mAccountMinDate != null) {
            Calendar minDate = Calendar.getInstance();
            minDate.set(mAccountMinDate.getYear(),
                    // OffsetDateTime uses month numbers from 1-12
                    // but DatePickerDialog uses numbers from 0-11
                    mAccountMinDate.getMonthValue()-1,
                    mAccountMinDate.getDayOfMonth());
            dialog.setMinDate(minDate);
        }
        dialog.setAccentColor(mThemeColor);
        dialog.show(getSupportFragmentManager(), TAG_DATE_DIALOG);
    }

    private void openTimePicker() {
        TimePickerDialog dialog = TimePickerDialog.newInstance(
                this, false);
        if (mAccountMinDate != null && mTransaction.getDate().toLocalDate().isEqual(mAccountMinDate.toLocalDate())) {
            Timepoint minTime = new Timepoint(mAccountMinDate.getHour(),
                    mAccountMinDate.getMinute(),
                    mAccountMinDate.getSecond());
            dialog.setMinTime(minTime);
        }
        dialog.setAccentColor(mThemeColor);
        dialog.show(getSupportFragmentManager(), TAG_TIME_DIALOG);
    }

    private void openNewAccountActivity(View view) {
        Intent intent = new Intent(this, AddEditAccountActivity.class);
        startActivity(intent);
    }

    private void openNewCategoryActivity(View view) {
        Toast.makeText(this, getResources().getString(R.string.wip), Toast.LENGTH_LONG).show();
    }

    private void openCalculator(View v) {
        CalcDialog dialog = new CalcDialog();
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setRoundingMode(BigDecimalUtil.ROUNDING_MODE);
        numberFormat.setMaximumFractionDigits(BigDecimalUtil.DECIMAL_SCALE);
        dialog.getSettings()
                .setInitialValue(mTransaction.getOriginalValue())
                .setMinValue(MoneyTransaction.MIN_VALUE)
                .setMaxValue(MoneyTransaction.MAX_VALUE)
                .setSignBtnShown(false)
                .setAnswerBtnShown(true)
                .setNumberFormat(numberFormat);
        dialog.show(getSupportFragmentManager(), TAG_CALCULATOR_DIALOG);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        OffsetDateTime dateTime = mTransaction.getDate()
                .withYear(year)
                // OffsetDateTime uses month numbers from 1-12
                // but OnDateSetListener returns numbers from 0-11
                .withMonth(monthOfYear+1)
                .withDayOfMonth(dayOfMonth);
        mTransaction.setDate(dateTime);
        if (mDateTimeSwitch.isChecked()) {
            openTimePicker();
        } else {
            tryToForceConfirmedStatus();
            updateDateText();
        }
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        OffsetDateTime dateTime = mTransaction.getDate()
                .withHour(hourOfDay)
                .withMinute(minute)
                .withSecond(second);
        mTransaction.setDate(dateTime);
        tryToForceConfirmedStatus();
        updateDateText();
    }

    @Override
    public void onValueEntered(int requestCode, @Nullable BigDecimal value) {
        if (value != null)
            mAmountTIET.setText(value.toPlainString());
    }
}