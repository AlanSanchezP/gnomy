package io.github.alansanchezp.gnomy.ui.transaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.maltaisn.calcdialog.CalcDialog;
import com.tiper.MaterialSpinner;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.databinding.ActivityAddEditTransactionBinding;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.ui.ConfirmationDialogFragment;
import io.github.alansanchezp.gnomy.ui.GnomyFragmentFactory;
import io.github.alansanchezp.gnomy.ui.GnomySpinnerAdapter;
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

import static io.github.alansanchezp.gnomy.util.ISpinnerItem.getItemIndexById;
import static io.github.alansanchezp.gnomy.util.android.SimpleTextWatcherWrapper.onlyOnTextChanged;

// TODO: Implement recurrent transactions
public class AddEditTransactionActivity
        extends BackButtonActivity<ActivityAddEditTransactionBinding>
        implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        CalcDialog.CalcDialogCallback {
    public static final String EXTRA_TRANSACTION_TYPE = "AddEditTransactionActivity.TransactionType";
    public static final String EXTRA_TRANSACTION_ID = "AddEditTransactionActivity.TransactionId";
    // TODO: Implement logic to use these two extras
    public static final String EXTRA_TRANSACTION_ACCOUNT = "AddEditTransactionActivity.TransactionAccount";
    public static final String EXTRA_TRANSFER_DESTINATION_ACCOUNT = "AddEditTransactionActivity.TransferDestinationAccount";
    private static final String TAG_ARCHIVED_DESTINATION_DIALOG = "AddEditTransactionActivity.ArchivedDestinationDialog";
    private static final String TAG_DATE_DIALOG = "AddEditTransactionActivity.DateDialog";
    private static final String TAG_TIME_DIALOG = "AddEditTransactionActivity.TimeDialog";
    private static final String TAG_CALCULATOR_DIALOG = "AddEditTransactionActivity.CalculatorDialog";

    // Utilities for validation and data management
    private int mTransactionType;
    private boolean mIsNewScreen = true;
    private OffsetDateTime mTransactionMinDate;
    private MoneyTransaction mTransaction;
    private AddEditTransactionViewModel mViewModel;

    // Layout objects
    private SingleClickViewHolder<FloatingActionButton> mFABVH;

    // Flags for async operations
    private List<Account> mAccountsList;
    private List<Category> mCategoriesList;

    public AddEditTransactionActivity() {
        super(null,true,ActivityAddEditTransactionBinding::inflate);
    }

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
        mFABVH = new SingleClickViewHolder<>($.addeditTransactionFAB, true);
        // TODO: Find a better way to present these as actions
        $.addeditTransactionNewAccount.setPaintFlags(
                $.addeditTransactionNewAccount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        $.addeditTransactionNewCategory.setPaintFlags(
                $.addeditTransactionNewCategory.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        $.addeditTransactionNewAccount.setOnClickListener(this::openNewAccountActivity);
        $.addeditTransactionNewCategory.setOnClickListener(this::openNewCategoryActivity);
        toggleMoreOptions();

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
            $.addeditTransactionFromAccount.setHint(R.string.transaction_to_account);
        } else if (mTransactionType == MoneyTransaction.TRANSFER) {
            if (transactionId == 0) setTitle(R.string.transaction_new_transfer);
            else setTitle(R.string.transaction_modify_transfer);
            setThemeColor(getResources().getColor(R.color.colorTransfers));
            $.addeditTransactionMarkAsDone.setEnabled(false);
            $.addeditTransactionMarkAsDone.setChecked(true);
            $.addeditTransactionMarkAsDone.setVisibility(View.GONE);
            $.addeditTransactionToAccount.setVisibility(View.VISIBLE);
        } else {
            throw new RuntimeException("Invalid transaction type.");
        }

        LiveData<MoneyTransaction> ld = mViewModel.getTransaction(transactionId);
        if (ld == null) {
            MoneyTransaction transaction = new MoneyTransaction();
            transaction.setType(mTransactionType);
            transaction.setAccount(mViewModel.getSelectedAccount());
            transaction.setCategory(mViewModel.getSelectedCategory());
            if (mTransactionType == MoneyTransaction.TRANSFER)
                transaction.setTransferDestinationAccount(mViewModel.getSelectedTransferAccount());
            onTransactionChanged(transaction);
        } else {
            mIsNewScreen = false;
            $.addeditTransactionBox.setVisibility(View.INVISIBLE);
            mFABVH.onView(this, v -> v.setVisibility(View.INVISIBLE));
            ld.observe(this, this::onTransactionChanged);
        }

        setCurrencySpinner();
        setInputFilters();
        mViewModel.getAccounts().observe(this, this::onAccountsListChanged);
        mViewModel.getCategories().observe(this, this::onCategoriesListChanged);
        mFABVH.setOnClickListener(this::processData);
        $.addeditTransactionAmount.setEndIconOnClickListener(this::openCalculator);
        $.addeditTransactionDate.setEndIconOnClickListener(this::openDatePicker);
        // As data gets reset to its stored value after rotation it is possible
        //  that date and/or time pickers allow selection of an invalid date,
        //  and then FAB is expected to trigger an error on date field.
        //  If this happens, it would be impossible for the user to select
        //  a valid date if error icon doesn't hold a callback too.
        $.addeditTransactionDate.setErrorIconOnClickListener(this::openDatePicker);
        $.addeditTransactionAmount.setErrorIconOnClickListener(this::openCalculator);
        $.addeditTransactionIncludeTime.setOnCheckedChangeListener((btn, b) -> updateDateText());
        $.addeditTransactionMoreOptionsToggle.setOnClickListener(v -> {
            mViewModel.toggleShowMoreOptions();
            toggleMoreOptions();
        });

        $.addeditTransactionAmountInput.addTextChangedListener(onlyOnTextChanged((s, start, count, after) ->
                onTransactionAmountChanges(s.toString())));
        $.addeditTransactionConceptInput.addTextChangedListener(onlyOnTextChanged((s, start, count, after) ->
                onTransactionConceptChanges(s.toString())));
        $.addeditTransactionNotesInput.addTextChangedListener(onlyOnTextChanged((s, start, count, after) -> {
            if (mTransaction != null)
                mTransaction.setNotes(s.toString());
        }));
    }

    /* INHERITED METHODS FROM GnomyActivity */

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

        $.addeditTransactionContainer.setBackgroundColor(mThemeColor);
        ViewTintingUtil
                .monotintTextInputLayout($.addeditTransactionAmount, mThemeTextColor);
        ViewTintingUtil
                .tintTextInputLayout($.addeditTransactionConcept, mThemeColor);
        ViewTintingUtil
                .tintTextInputLayout($.addeditTransactionDate, mThemeColor);
        ViewTintingUtil
                .tintSwitch($.addeditTransactionIncludeTime, mThemeColor);
        ViewTintingUtil
                .tintTextInputLayout($.addeditTransactionNotes, mThemeColor);
        mFABVH.onView(this, v ->
                ViewTintingUtil.tintFAB(v, fabBgColor, fabTextColor));
        ViewTintingUtil
                .tintSwitch($.addeditTransactionMarkAsDone, mThemeColor);
        ViewTintingUtil
                .tintSpinner($.addeditTransactionFromAccount, mThemeColor);
        ViewTintingUtil
                .tintSpinner($.addeditTransactionToAccount, mThemeColor);
        ViewTintingUtil
                .tintSpinner($.addeditTransactionCategory, mThemeColor);
        ViewTintingUtil
                .tintSpinner($.addeditTransactionCurrency, mThemeColor);

    }

    /* UI METHODS*/

    private void toggleMoreOptions() {
        if (mViewModel.showMoreOptions()) {
            $.addeditTransactionMoreOptionsArrow.setRotation(180f);
            $.addeditTransactionMoreOptionsText.setText(R.string.show_less_options);
            $.addeditTransactionMoreOptionsContainer.setVisibility(View.VISIBLE);
        } else {
            $.addeditTransactionMoreOptionsArrow.setRotation(0);
            $.addeditTransactionMoreOptionsText.setText(R.string.show_more_options);
            $.addeditTransactionMoreOptionsContainer.setVisibility(View.GONE);
        }
    }

    private void updateDateText() {
        if (mTransaction == null) return;
        $.addeditTransactionDateInput.setText(DateUtil.getOffsetDateTimeString(mTransaction.getDate(),
                $.addeditTransactionIncludeTime.isChecked()));
    }

    private void setCurrencySpinner() {
        try {
            String[] currencies = CurrencyUtil.getDisplayArray();
            $.addeditTransactionCurrency.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currencies));
            $.addeditTransactionCurrency.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(@NotNull MaterialSpinner parent, @Nullable View view, int position, long id) {
                    if (mTransaction == null) return; // Should only happen when spinner is restored
                    mTransaction.setCurrency(CurrencyUtil.getCurrencyCode(position));
                }

                @Override
                public void onNothingSelected(@NotNull MaterialSpinner parent) {
                }
            });
            // TODO: Use global default currency (WHEN IMPLEMENTED)
            if (mIsNewScreen) {
                mTransaction.setCurrency("USD");
            }
        } catch (GnomyCurrencyException e) {
            // This shouldn't happen
            Log.wtf("AddEditTransaction", "setLists: CURRENCIES array triggers error", e);
        }
    }

    private void setInputFilters() {
        $.addeditTransactionAmountInput.setFilters(new InputFilter[]{new InputFilterMinMax(MoneyTransaction.MIN_VALUE, MoneyTransaction.MAX_VALUE, BigDecimalUtil.DECIMAL_SCALE)});
    }

    /* EDITTEXT WATCHERS */

    private void onTransactionAmountChanges(String value) {
        if (value.length() == 0) {
            if (mViewModel.transactionAmountIsPristine()) return;
            $.addeditTransactionAmount.setError(getString(R.string.transaction_error_amount));
        } else {
            if (mTransaction != null) mTransaction.setOriginalValue(value);
            $.addeditTransactionAmount.setErrorEnabled(false);
        }

        mViewModel.notifyTransactionAmountChanged();
    }

    private void onTransactionConceptChanges(String value) {
        if (mTransaction != null) mTransaction.setConcept(value);

        if (value.trim().length() == 0) {
            if (mViewModel.transactionConceptIsPristine()) return;
            $.addeditTransactionConcept.setError(getString(R.string.transaction_error_concept));
        } else {
            $.addeditTransactionConcept.setErrorEnabled(false);
        }

        mViewModel.notifyTransactionConceptChanged();
    }


    /* LIVEDATA OBSERVERS */

    private void onTransactionChanged(MoneyTransaction transaction) {
        // TODO: Evaluate if this approach is better than finish() the Activity
        if (transaction == null)
            throw new RuntimeException("Attempting to update non-existent Transaction.");
        if (transaction.getType() != mTransactionType)
            throw new RuntimeException("Attempting an invalid operation: Changing a transaction's type.");
        // TODO: Should we keep edited data on rotation?
        //  An option could be to force portrait mode on form activities
        mTransactionMinDate = transaction.getDate();
        mTransaction = transaction;
        mViewModel.setUserSelectedConfirmedStatus(transaction.isConfirmed());
        tryToForceConfirmedStatus();
        updateDateText();
        if (mTransactionType != MoneyTransaction.TRANSFER) { // This switch is disabled for transfers
            $.addeditTransactionMarkAsDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mTransaction.setConfirmed(isChecked);
                mViewModel.setUserSelectedConfirmedStatus(isChecked);
            });
        }
        if(!mIsNewScreen) {
            $.addeditTransactionAmountInput.setText(transaction.getOriginalValue().toPlainString());
            $.addeditTransactionConceptInput.setText(transaction.getConcept());
            $.addeditTransactionNotesInput.setText(transaction.getNotes());
        }
        attemptMixedDataSourceOperations();
    }

    private void onCategoriesListChanged(List<Category> categories) {
        if (categories == null || categories.isEmpty())
            throw new RuntimeException("Categories list is not meant to be empty.");
        mCategoriesList = categories;
        GnomySpinnerAdapter<Category> adapter = new GnomySpinnerAdapter<>(this, categories);
        $.addeditTransactionCategory.setAdapter(adapter);
        $.addeditTransactionCategory.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NotNull MaterialSpinner parent, @Nullable View view, int position, long id) {
                if (mTransaction == null) return;
                parent.setErrorEnabled(false);
                mTransaction.setCategory((int) id);
                // Edit mode discards changes after Activity gets recreated to avoid accidental updates
                if (mIsNewScreen) mViewModel.setSelectedCategory((int) id);
                parent.setStartIconDrawable(adapter.getItemDrawable(position));
            }

            @Override
            public void onNothingSelected(@NotNull MaterialSpinner parent) {
            }
        });
        attemptMixedDataSourceOperations();
    }

    private void onAccountsListChanged(List<Account> _accounts) {
        // Prevent NullPointerException
        final List<Account> accounts;
        if (_accounts == null) accounts = new ArrayList<>();
        else accounts = _accounts;

        mAccountsList = accounts;
        GnomySpinnerAdapter<Account> adapter = new GnomySpinnerAdapter<>(this, accounts);

        $.addeditTransactionFromAccount.setAdapter(adapter);
        $.addeditTransactionFromAccount.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NotNull MaterialSpinner parent, @Nullable View view, int position, long id) {
                if (mTransaction == null) return;
                parent.setErrorEnabled(false);
                Account item = accounts.get(position);
                setTransactionAccount(item);
                if (mTransactionType == MoneyTransaction.TRANSFER) {
                    if ($.addeditTransactionToAccount.getError() != null &&
                        $.addeditTransactionToAccount.getError().toString().equals(
                                getString(R.string.transaction_error_transfer_destination_account)
                        )) {
                        // Keeps non-null account error
                        $.addeditTransactionToAccount.setErrorEnabled(false);
                    }
                    if (mTransaction.getTransferDestinationAccount() != null &&
                            (int) id == mTransaction.getTransferDestinationAccount()) {
                        $.addeditTransactionToAccount.setError(getString(R.string.transaction_error_transfer_destination_account));
                        $.addeditTransactionToAccount.getChildAt(1).setVisibility(View.VISIBLE);
                    }
                }
                parent.setStartIconDrawable(adapter.getItemDrawable(position));
            }

            @Override
            public void onNothingSelected(@NotNull MaterialSpinner parent) {
            }
        });

        if (mTransactionType == MoneyTransaction.TRANSFER) {
            GnomySpinnerAdapter<Account> transferAdapter = new GnomySpinnerAdapter<>(this, accounts);
            if (accounts.size() > 1) {
                $.addeditTransactionToAccount.setAdapter(transferAdapter);
                $.addeditTransactionToAccount.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(@NotNull MaterialSpinner parent, @Nullable View view, int position, long id) {
                        if (mTransaction == null) return;
                        parent.setErrorEnabled(false);
                        Account item = accounts.get(position);
                        setDestinationAccount(item);
                        if ((int) id == mTransaction.getAccount()) {
                            parent.setError(getString(R.string.transaction_error_transfer_destination_account));
                            parent.getChildAt(1).setVisibility(View.VISIBLE);
                        }
                        parent.setStartIconDrawable(adapter.getItemDrawable(position));
                    }

                    @Override
                    public void onNothingSelected(@NotNull MaterialSpinner parent) {
                    }
                });
            } else { // Send empty list that prevents ONE possible occurrence of same-account error
                $.addeditTransactionToAccount.setAdapter(new GnomySpinnerAdapter<>(this, new ArrayList<>()));
                mTransaction.setTransferDestinationAccount(null);
                $.addeditTransactionToAccount.setOnItemSelectedListener(null);
            }
            if (mViewModel.isExpectingNewAccount()) {
                // Selects last inserted account
                $.addeditTransactionToAccount.setSelection(mAccountsList.size() - 1);
                mViewModel.notifyExpectingNewAccount(false);
            } else if ($.addeditTransactionToAccount.getError() != null &&
                    $.addeditTransactionToAccount.getError().toString().equals(
                            getString(R.string.transaction_error_account)
                    )) {
                // Keeps same-account account error
                $.addeditTransactionToAccount.setErrorEnabled(false);
            }
        } else if (mViewModel.isExpectingNewAccount()) {
            // Selects last inserted account
            $.addeditTransactionFromAccount.setSelection(mAccountsList.size() - 1);
            mViewModel.notifyExpectingNewAccount(false);
        }
        attemptMixedDataSourceOperations();
    }

    /* DATA MANAGEMENT */

    // TODO: MediatorLiveData is probably a better approach
    private void attemptMixedDataSourceOperations() {
        if (mAccountsList == null ||
                mCategoriesList == null ||
                mTransaction == null) return;

        if (mViewModel.isExpectingNewAccount() && !mAccountsList.isEmpty()) {
            $.addeditTransactionFromAccount.setSelection(mAccountsList.size() - 1);
            setTransactionAccount(mAccountsList.get(mAccountsList.size() - 1));
            if (mTransactionType == MoneyTransaction.TRANSFER &&
                    mTransaction.getTransferDestinationAccount() == null) {
                setDestinationAccount(mAccountsList.get(0));
            }
            mViewModel.notifyExpectingNewAccount(false);
        } else {
            if (mTransaction.getAccount() != 0) {
                int selectedIndex = getItemIndexById(mAccountsList, mTransaction.getAccount());
                Account selectedAccount = mAccountsList.get(selectedIndex);
                setTransactionAccount(selectedAccount);
                $.addeditTransactionFromAccount.setSelection(selectedIndex);
            }
            if (mTransaction.getType() == MoneyTransaction.TRANSFER &&
                    mTransaction.getTransferDestinationAccount() != null) {
                try {
                    int selectedIndex = getItemIndexById(mAccountsList, mTransaction.getTransferDestinationAccount());
                    Account destinationAccount = mAccountsList.get(selectedIndex);
                    setDestinationAccount(destinationAccount);
                    $.addeditTransactionToAccount.setSelection(selectedIndex);
                } catch(ArrayIndexOutOfBoundsException e) {
                    $.addeditTransactionToAccount.setVisibility(View.GONE);
                    ConfirmationDialogFragment dialog = new ConfirmationDialogFragment(this);
                    Bundle args = new Bundle();
                    args.putString(ConfirmationDialogFragment.ARG_TITLE, getString(R.string.transaction_transfer_destination_archived));
                    args.putString(ConfirmationDialogFragment.ARG_MESSAGE, getString(R.string.transaction_transfer_destination_archived_message));
                    args.putString(ConfirmationDialogFragment.ARG_YES_STRING, getString(R.string.transaction_transfer_destination_archived_action));
                    args.putString(ConfirmationDialogFragment.ARG_NO_STRING, getString(R.string.transaction_transfer_destination_archived_ignore));
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), TAG_ARCHIVED_DESTINATION_DIALOG);
                }
            }
            $.addeditTransactionCurrency.setSelection(CurrencyUtil.getCurrencyIndex(mTransaction.getCurrency()));
        }

        if (!mViewModel.isExpectingNewCategory() && !mCategoriesList.isEmpty()) {
            $.addeditTransactionCategory.setSelection(getItemIndexById(mCategoriesList, mTransaction.getCategory()));
        }
        if (!mIsNewScreen) {
            $.addeditTransactionBox.setVisibility(View.VISIBLE);
            mFABVH.onView(this, v -> v.setVisibility(View.VISIBLE));
        }
    }

    private void tryToForceConfirmedStatus() {
        // TODO: How can we test this behavior?
        if (mTransaction == null) return;
        if (mTransactionType == MoneyTransaction.TRANSFER) return;
        if (mTransaction.getDate().isAfter(OffsetDateTime.now())) {
            boolean previousSelectedState = mTransaction.isConfirmed();
            $.addeditTransactionMarkAsDone.setChecked(false); // event listener will update mTransaction too
            $.addeditTransactionMarkAsDone.setEnabled(false);
            // event listener will set status as false, we restore it manually here
            mViewModel.setUserSelectedConfirmedStatus(previousSelectedState);
        } else {
            $.addeditTransactionMarkAsDone.setEnabled(true);
            $.addeditTransactionMarkAsDone.setChecked(mViewModel.getUserSelectedConfirmedStatus()); // event listener will update mTransaction too
        }
    }

    private void setOrIgnoreMinDate(@NonNull Account account) {
        mTransactionMinDate = account.getCreatedAt();
        if (mTransactionMinDate.isAfter(mTransaction.getDate())) {
            mTransaction.setDate(mTransactionMinDate);
            tryToForceConfirmedStatus();
            updateDateText();
        }
    }

    private void setOrIgnoreMinDate(Account originAccount, Account destinationAccount) {
        if (originAccount == null && destinationAccount == null)
            return;
        if (originAccount == null) {
            setOrIgnoreMinDate(destinationAccount);
            return;
        }
        if (destinationAccount == null) {
            setOrIgnoreMinDate(originAccount);
            return;
        }
        Account newerAccount;
        if (originAccount.getCreatedAt().compareTo(destinationAccount.getCreatedAt()) < 0) {
            newerAccount = destinationAccount;
        } else {
            newerAccount = originAccount;
        }
        setOrIgnoreMinDate(newerAccount);
    }

    private void setTransactionAccount(Account account) {
        mTransaction.setAccount(account.getId());
        if (mIsNewScreen) mViewModel.setSelectedAccount(account.getId());
        if (mTransactionType != MoneyTransaction.TRANSFER)
            setOrIgnoreMinDate(account);
        else
            setOrIgnoreMinDate(account, (Account) $.addeditTransactionToAccount.getSelectedItem());
        setAccountCurrency(account);
    }

    private void setDestinationAccount(Account account) {
        if (mTransactionType != MoneyTransaction.TRANSFER)
            throw new RuntimeException("Invalid operation: Setting transfer destination account on non-transfer transaction.");
        if (account == null) {
            account = new Account();
            account.setCreatedAt(DateUtil.OffsetDateTimeNow());
            mTransaction.setTransferDestinationAccount(null);
            if (mIsNewScreen) mViewModel.setSelectedTransferAccount(null);
        }
        else if (account.getId() != 0){
            mTransaction.setTransferDestinationAccount(account.getId());
            if (mIsNewScreen) mViewModel.setSelectedTransferAccount(account.getId());
        }
        setOrIgnoreMinDate((Account) $.addeditTransactionFromAccount.getSelectedItem(), account);
    }

    private void setAccountCurrency(Account account) {
        if (mIsNewScreen)
            $.addeditTransactionCurrency.setSelection(
                    CurrencyUtil.getCurrencyIndex(
                            account.getDefaultCurrency()));
    }

    /* FORM SUBMISSION AND VALIDATION */

    private boolean validateTextFields() {
        mViewModel.notifyTransactionAmountChanged();
        mViewModel.notifyTransactionConceptChanged();

        String amountString = Objects.requireNonNull($.addeditTransactionAmountInput.getText()).toString();
        String transactionConcept = Objects.requireNonNull($.addeditTransactionConceptInput.getText()).toString();
        onTransactionAmountChanges(amountString);
        onTransactionConceptChanges(transactionConcept);

        return transactionConcept.length() > 0
                && amountString.length() > 0;
    }

    private boolean validateAccounts() {
        boolean anySpinnerIsNull = false;

        if (mTransaction.getAccount() == 0) {
            anySpinnerIsNull = true;
            $.addeditTransactionFromAccount.setError(getString(R.string.transaction_error_account));
            $.addeditTransactionFromAccount.getChildAt(1).setVisibility(View.VISIBLE);
        }

        if (mTransactionType == MoneyTransaction.TRANSFER) {
            if (mTransaction.getTransferDestinationAccount() == null || mTransaction.getTransferDestinationAccount() == 0) {
                anySpinnerIsNull = true;
                $.addeditTransactionToAccount.setError(getString(R.string.transaction_error_account));
                $.addeditTransactionToAccount.getChildAt(1).setVisibility(View.VISIBLE);
            }

            if (anySpinnerIsNull) return false;

            if (mTransaction.getAccount() == mTransaction.getTransferDestinationAccount()) {
                $.addeditTransactionToAccount.setError(getString(R.string.transaction_error_transfer_destination_account));
                $.addeditTransactionToAccount.getChildAt(1).setVisibility(View.VISIBLE);
                return false;
            }
        } else if (mTransaction.getTransferDestinationAccount() != null) {
            throw new RuntimeException("Non-transfers cannot have a transfer destination account.");
        }

        return true;
    }

    private boolean validateCategory() {
        if (mTransaction.getCategory() == 0) {
            $.addeditTransactionCategory.setError(getString(R.string.transaction_error_category));
            $.addeditTransactionCategory.getChildAt(1).setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    private boolean validateDate() {
        if (mTransactionMinDate == null) return false;
        if(!mTransaction.getDate().isBefore(mTransactionMinDate)) {
            $.addeditTransactionDate.setErrorEnabled(false);
            return true;
        }
        $.addeditTransactionDate.setError(getString(R.string.transaction_error_date));
        return false;
    }

    private void processData(View v) {
        boolean texFieldsAreValid = validateTextFields();
        boolean accountsAreValid = validateAccounts();
        boolean selectedDateIsValid = validateDate();
        boolean categoryIsValid = validateCategory();

        if (texFieldsAreValid  && accountsAreValid && selectedDateIsValid && categoryIsValid) {
            saveData();
        } else {
            Toast.makeText(this, getString(R.string.form_error), Toast.LENGTH_LONG).show();
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

    /* BUTTON CLICK LISTENERS */

    private void openDatePicker(View v) {
        Calendar originalDate = Calendar.getInstance();
        originalDate.set(mTransaction.getDate().getYear(),
                // OffsetDateTime uses month numbers from 1-12
                // but Calendar returns numbers from 0-11
                mTransaction.getDate().getMonthValue()-1,
                mTransaction.getDate().getDayOfMonth());
        DatePickerDialog dialog = DatePickerDialog.newInstance(
                this, originalDate);
        if (mTransactionMinDate != null) {
            Calendar minDate = Calendar.getInstance();
            minDate.set(mTransactionMinDate.getYear(),
                    // OffsetDateTime uses month numbers from 1-12
                    // but DatePickerDialog uses numbers from 0-11
                    mTransactionMinDate.getMonthValue()-1,
                    mTransactionMinDate.getDayOfMonth());
            dialog.setMinDate(minDate);
        }
        if (mTransactionType == MoneyTransaction.TRANSFER) {
            dialog.setMaxDate(Calendar.getInstance());
        }
        dialog.setAccentColor(mThemeColor);
        dialog.show(getSupportFragmentManager(), TAG_DATE_DIALOG);
    }

    private void openTimePicker() {
        TimePickerDialog dialog = TimePickerDialog.newInstance(
                this, false);
        if (mTransactionMinDate != null && mTransaction.getDate().toLocalDate().isEqual(mTransactionMinDate.toLocalDate())) {
            Timepoint minTime = new Timepoint(mTransactionMinDate.getHour(),
                    mTransactionMinDate.getMinute(),
                    mTransactionMinDate.getSecond());
            dialog.setMinTime(minTime);
        }
        if (mTransactionType == MoneyTransaction.TRANSFER) {
            OffsetDateTime now = DateUtil.OffsetDateTimeNow();
            Timepoint maxTime = new Timepoint(now.getHour(), now.getMinute(), now.getSecond());
            dialog.setMaxTime(maxTime);
        }
        dialog.setAccentColor(mThemeColor);
        dialog.show(getSupportFragmentManager(), TAG_TIME_DIALOG);
    }

    private void openNewAccountActivity(View view) {
        mViewModel.notifyExpectingNewAccount(true);
        Intent intent = new Intent(this, AddEditAccountActivity.class);
        startActivity(intent);
    }

    private void openNewCategoryActivity(View view) {
        mViewModel.notifyExpectingNewCategory(true);
        Toast.makeText(this, getString(R.string.wip), Toast.LENGTH_LONG).show();
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

    /* INTERFACE METHODS FROM THIRD-PARTY LIBRARIES */

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        OffsetDateTime dateTime = mTransaction.getDate()
                .withYear(year)
                // OffsetDateTime uses month numbers from 1-12
                // but OnDateSetListener returns numbers from 0-11
                .withMonth(monthOfYear+1)
                .withDayOfMonth(dayOfMonth);
        mTransaction.setDate(dateTime);
        if ($.addeditTransactionIncludeTime.isChecked()) {
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
            $.addeditTransactionAmountInput.setText(value.toPlainString());
    }

    @Override
    public void onConfirmationDialogYes(DialogInterface dialog, String dialogTag, int which) {
        if (dialogTag.equals(TAG_ARCHIVED_DESTINATION_DIALOG)) {
            mCompositeDisposable.add(
                mViewModel.restoreDestinationAccount(mTransaction.getTransferDestinationAccount())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            integer -> {
                                $.addeditTransactionToAccount.setVisibility(View.VISIBLE);
                            },
                            throwable -> {
                                Toast.makeText(this, R.string.generic_data_error, Toast.LENGTH_LONG).show();
                            }));
        } else {
            super.onConfirmationDialogYes(dialog, dialogTag, which);
        }
    }
}