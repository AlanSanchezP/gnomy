package io.github.alansanchezp.gnomy.ui.account;

import androidx.annotation.ColorInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.databinding.ActivityAddEditAccountBinding;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.android.InputFilterMinMax;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.android.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.util.android.ViewTintingUtil;
import io.github.alansanchezp.gnomy.viewmodel.account.AddEditAccountViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.thebluealliance.spectrum.SpectrumDialog;

import java.util.Arrays;
import java.util.Objects;

import static io.github.alansanchezp.gnomy.util.android.SimpleTextWatcherWrapper.onlyOnTextChanged;

public class AddEditAccountActivity
        extends BackButtonActivity<ActivityAddEditAccountBinding> {
    public static final String EXTRA_ACCOUNT_ID = "AddEditAccountActivity.AccountId";
    private static final String TAG_PICKER_DIALOG = "AddEditAccountActivity.ColorPickerDialog";
    private AddEditAccountViewModel mAddEditAccountViewModel;
    private Account mAccount;
    private SingleClickViewHolder<ImageButton> mColorPickerBtnVH;
    private SingleClickViewHolder<FloatingActionButton> mFABVH;

    public AddEditAccountActivity() {
        super(null,true, ActivityAddEditAccountBinding::inflate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAddEditAccountViewModel = new ViewModelProvider(this,
                new SavedStateViewModelFactory(
                        this.getApplication(), this))
                .get(AddEditAccountViewModel.class);

        // Prevent potential noticeable blink in color
        mAppbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        // TODO: Either dynamically show/hide FAB on landscape or find a better UI behavior for it
        //  It makes UX unpleasant on landscape because it overlays with other elements
        mFABVH = new SingleClickViewHolder<>($.addeditAccountFAB, true);
        mFABVH.setOnClickListener(this::processData);
        mColorPickerBtnVH = new SingleClickViewHolder<>($.addeditAccountColorButton);
        mColorPickerBtnVH.setOnClickListener(this::showColorPicker);

        mAddEditAccountViewModel.accountColor.observe(this, this::onAccountColorChanged);

        initSpinners();
        setInputFilters();

        Intent intent = getIntent();
        int accountId = intent.getIntExtra(EXTRA_ACCOUNT_ID, 0);
        LiveData<Account> accountLD = mAddEditAccountViewModel.getAccount(accountId);

        String activityTitle;
        if (accountLD != null) {
            activityTitle = getString(R.string.account_card_modify);
            // Prevent potential noticeable blink in spinners
            $.addeditAccountBox.setVisibility(View.INVISIBLE);

            // TODO: (When currency support is implemented) (only edit mode)
            //  update: Do we even want this behavior? Analyze it, maybe we are fine just disabling the spinner on edit mode
            /*
             Monitor currency change and display an alert to either preserve ALL transactions values or convert them to their equivalents
             on the new selected currency (and perform this operation until submitting the new data)
            */
            $.addeditAccountCurrency.setEnabled(false);
            $.addeditAccountCurrencyCannotChange.setVisibility(View.VISIBLE);
            accountLD.observe(this, this::onAccountChanged);
        } else {
            activityTitle = getString(R.string.account_new);
            mAccount = new Account();
            // Only generate new color if viewModel doesn't have one stored already
            if (Objects.requireNonNull(mAddEditAccountViewModel.accountColor.getValue()) == 1) {
                mAddEditAccountViewModel.setAccountColor(ColorUtil.getRandomColor());
            }
        }

        setTitle(activityTitle);

        $.addeditAccountNameInput.addTextChangedListener(onlyOnTextChanged((s, start, count, after) ->
                onAccountNameEditTextChanges(s.toString())));
        $.addeditAccountInitialValueInput.addTextChangedListener(onlyOnTextChanged((s, start, count, after) ->
                onInitialValueEditTextChanges(s.toString())));
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

    private void onAccountChanged(Account account) {
        if (account == null) {
            Log.e("AddEditAccount", "onAccountChanged: Account not found. Closing activity.");
            finish();
            return;
        }
        mAccount = account;
        mAddEditAccountViewModel.setAccountColor(account.getBackgroundColor());
        $.addeditAccountNameInput.setText(account.getName());
        $.addeditAccountInitialValueInput.setText(account.getInitialValue().toPlainString());
        $.addeditAccountCurrency.setSelectedIndex(
                Arrays.asList(CurrencyUtil.getCurrencies()).indexOf(mAccount.getDefaultCurrency())
        );
        $.addeditAccountType.setSelectedIndex(mAccount.getType() - 1);
        $.addeditAccountShowInHome.setChecked(account.isShowInDashboard());
        // Restore container visibility once all data has been initialized
        // TODO: How can we prevent Switch animation from triggering?
        //  When on edit mode, the animation is triggered if account.isShowInDashboard() is true
        $.addeditAccountBox.setVisibility(View.VISIBLE);
    }

    private void onAccountColorChanged(@ColorInt int color) {
        // 1 is an invalid HEX number, no point in trying to apply color to elements
        if (color == 1) return;
        // If account is null (null != empty) then no data has been received from Room.
        // That means that we will receive a second color pretty soon and therefore
        // this first coloring can be skipped.
        if (mAccount == null) return;

        mAccount.setBackgroundColor(color);
        setThemeColor(color);

        int fabBgColor = ColorUtil.getVariantByFactor(color, 0.86f);
        int fabTextColor = ColorUtil.getTextColor(fabBgColor);

        $.addeditAccountContainer.setBackgroundColor(color);

        // TODO: There is a blink in both FAB and color picker button
        //  that is sometimes noticeable.
        mFABVH.onView(this, v -> ViewTintingUtil.tintFAB(v, fabBgColor, fabTextColor));
        ViewTintingUtil
                .monotintTextInputLayout($.addeditAccountName, mThemeTextColor);
        // TODO: Lighter colors make the hint barely readable
        //  Possible solutions:
        //      A) Use a darker variant of the selected color
        //      B) Change UI of this element (any ideas?)
        //      C) Is it possible to add a shadow or border to the hint text?
        ViewTintingUtil
                .tintTextInputLayout($.addeditAccountInitialValue, mThemeColor);
        ViewTintingUtil
                .tintSwitch($.addeditAccountShowInHome, mThemeColor);

        // TODO: (Wishlist, not a big deal) How can we unify the ripple color with the one from FAB?
        mColorPickerBtnVH.onView(this, v -> {
            $.addeditAccountColorButton.setBackgroundTintList(ColorStateList.valueOf(mThemeColor));
            $.addeditAccountColorButton.getDrawable().mutate().setTint(mThemeTextColor);
        });
    }

    private void initSpinners() {
        try {
            String[] currencies = CurrencyUtil.getDisplayArray();
            String[] accountTypes = getResources().getStringArray(R.array.account_types);

            $.addeditAccountCurrency.setItems(currencies);
            $.addeditAccountType.setItems(accountTypes);
        } catch (GnomyCurrencyException e) {
            // This shouldn't happen
            Log.wtf("AddEditAccount", "setLists: CURRENCIES array triggers error", e);
        }
    }

    private void setInputFilters() {
        $.addeditAccountInitialValueInput.setFilters(new InputFilter[]{new InputFilterMinMax(Account.MIN_INITIAL, Account.MAX_INITIAL, BigDecimalUtil.DECIMAL_SCALE)});
    }

    public void showColorPicker(View v) {
        new SpectrumDialog.Builder(this)
                .setColors(ColorUtil.getColors())
                .setSelectedColor(Objects.requireNonNull(mAddEditAccountViewModel.accountColor.getValue()))
                .setDismissOnColorSelected(true)
                .setOutlineWidth(0)
                .setTitle(R.string.account_pick_color_title)
                .setFixedColumnCount(5)
                .setOnColorSelectedListener((positiveResult, color) -> {
                    if (positiveResult) {
                        mAddEditAccountViewModel.setAccountColor(color);
                    }
                }).build().show(getSupportFragmentManager(), TAG_PICKER_DIALOG);
    }

    public void processData(View v) {
        boolean texFieldsAreValid = validateTextFields();

        if (texFieldsAreValid) {
            String currencyCode = CurrencyUtil.getCurrencyCode(
                    $.addeditAccountCurrency.getSelectedIndex());
            int accountType = $.addeditAccountType.getSelectedIndex() + 1;
            boolean showInDashboard = $.addeditAccountShowInHome.isChecked();

            saveData(currencyCode, accountType, showInDashboard);
        } else {
            Toast.makeText(this, getResources().getString(R.string.form_error), Toast.LENGTH_LONG).show();
            mFABVH.notifyOnAsyncOperationFinished();
        }
    }

    private void onAccountNameEditTextChanges(String value) {
        if (mAccount != null) mAccount.setName(value);

        if (value.trim().length() == 0) {
            if (mAddEditAccountViewModel.accountNameIsPristine()) return;
            $.addeditAccountName.setError(getResources().getString(R.string.account_error_name));
        } else {
            $.addeditAccountName.setErrorEnabled(false);
        }

        mAddEditAccountViewModel.notifyAccountNameChanged();
    }

    private void onInitialValueEditTextChanges(String value) {
        if (value.length() == 0) {
            if (mAddEditAccountViewModel.initialValueIsPristine()) return;
            $.addeditAccountInitialValue.setError(getResources().getString(R.string.account_error_initial_value));
        } else {
            if (mAccount != null) mAccount.setInitialValue(value);
            $.addeditAccountInitialValue.setErrorEnabled(false);
        }

        mAddEditAccountViewModel.notifyInitialValueChanged();
    }

    @SuppressWarnings("ConstantConditions")
    private boolean validateTextFields() {
        mAddEditAccountViewModel.notifyAccountNameChanged();
        mAddEditAccountViewModel.notifyInitialValueChanged();

        String accountName = $.addeditAccountNameInput.getText().toString();
        String initialValueString = $.addeditAccountInitialValueInput.getText().toString();
        onAccountNameEditTextChanges(accountName);
        onInitialValueEditTextChanges(initialValueString);

        return accountName.length() > 0 && initialValueString.length() > 0;
    }

    private void saveData(String currencyCode, int accountType, boolean includeInHomepage) {
        try {
            Disposable disposable;

            mAccount.setShowInDashboard(includeInHomepage);
            mAccount.setType(accountType);
            mAccount.setDefaultCurrency(currencyCode);

            if (mAccount.getId() == 0) {
                disposable = mAddEditAccountViewModel.insert(mAccount)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            longs -> {
                                Toast.makeText(this, R.string.account_message_saved, Toast.LENGTH_LONG).show();
                                finish();
                            },
                            throwable -> {
                                Toast.makeText(this, R.string.generic_data_error, Toast.LENGTH_LONG).show();
                                mFABVH.notifyOnAsyncOperationFinished();
                            });

            } else {
                disposable = mAddEditAccountViewModel.update(mAccount)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            integer -> {
                                Toast.makeText(this, R.string.account_message_updated, Toast.LENGTH_LONG).show();
                                finish();
                            },
                            throwable -> {
                                Toast.makeText(this, R.string.generic_data_error, Toast.LENGTH_LONG).show();
                                mFABVH.notifyOnAsyncOperationFinished();
                            });
            }
            mCompositeDisposable.add(disposable);
        } catch(NumberFormatException nfe) {
            Log.wtf("AddEditAccount", "saveData: Initial value validation failed", nfe);
            mFABVH.notifyOnAsyncOperationFinished();
        }
    }
}
