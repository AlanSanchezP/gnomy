package io.github.alansanchezp.gnomy.ui.account;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.savedstate.SavedStateRegistryOwner;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.util.android.InputFilterMinMax;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.viewmodel.account.AddEditAccountViewModel;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.thebluealliance.spectrum.SpectrumDialog;

import java.util.Arrays;
import java.util.Objects;

public class AddEditAccountActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "account_id";
    private static final String TAG_PICKER_DIALOG = "color_picker_dialog";
    private AddEditAccountViewModel mAddEditAccountViewModel;
    private Account mAccount;
    private Toolbar mAppbar;
    private Drawable mUpArrow;
    private LinearLayout mBoxLayout;
    private TextInputLayout mAccountNameTIL;
    private TextInputLayout mInitialValueTIL;
    private TextInputEditText mAccountNameTIET;
    private TextInputEditText mInitialValueTIET;
    private MaterialSpinner mCurrencySpinner;
    private MaterialSpinner mTypeSpinner;
    private Switch mShownInDashboardSwitch;
    private FloatingActionButton mFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_account);

        mAddEditAccountViewModel = new ViewModelProvider(this,
                new SavedStateViewModelFactory(
                        this.getApplication(), (SavedStateRegistryOwner) this))
                .get(AddEditAccountViewModel.class);

        mAppbar = findViewById(R.id.custom_appbar);
        // Prevent potential noticeable blink in color
        mAppbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        setSupportActionBar(mAppbar);
        mUpArrow = getResources().getDrawable(R.drawable.abc_vector_test);
        //noinspection ConstantConditions
        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBoxLayout = findViewById(R.id.addedit_account_box);
        mAccountNameTIL = findViewById(R.id.addedit_account_name);
        mInitialValueTIL = findViewById(R.id.addedit_account_initial_value);
        mAccountNameTIET = findViewById(R.id.addedit_account_name_input);
        mInitialValueTIET = findViewById(R.id.addedit_account_initial_value_input);
        mCurrencySpinner = findViewById(R.id.addedit_account_currency);
        mTypeSpinner = findViewById(R.id.addedit_account_type);
        mShownInDashboardSwitch = findViewById(R.id.addedit_account_show_in_home);
        mFAB = findViewById(R.id.addedit_account_FAB);

        initSpinners();
        setInputFilters();

        Intent intent = getIntent();
        int accountId = intent.getIntExtra(EXTRA_ID, 0);
        LiveData<Account> accountLD = mAddEditAccountViewModel.getAccount(accountId);

        String activityTitle;
        if (accountLD != null) {
            activityTitle = getString(R.string.account_card_modify);
            // Prevent potential noticeable blink in spinners
            mBoxLayout.setVisibility(View.INVISIBLE);
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

        mAddEditAccountViewModel.accountColor.observe(this, this::onAccountColorChanged);

        mAccountNameTIET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onAccountNameEditTextChanges(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mInitialValueTIET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onInitialValueEditTextChanges(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        mFAB.setEnabled(false);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirmation_dialog_title))
                .setMessage(getString(R.string.confirmation_dialog_description))
                .setPositiveButton(getString(R.string.confirmation_dialog_yes), (dialog, which) -> finish())
                .setNegativeButton(getString(R.string.confirmation_dialog_no),  null)
                .setOnDismissListener(dialog -> mFAB.setEnabled(true))
                .show();
    }

    private void onAccountChanged(Account account) {
        if (account == null) {
            try {
                // TODO: REALLY FIND A WAY TO INSERT TO TEST DATABASE TO AVOID DOING THIS
                Class.forName("io.github.alansanchezp.gnomy.MainNavigationInstrumentedTest");
                Log.d("AddEditAccount", "onAccountChanged: Test environment. Setting empty account..");
                account = new Account();
            } catch (ClassNotFoundException cnfe) {
                Log.e("AddEditAccount", "onAccountChanged: Account not found. Closing activity.");
                finish();
                return;
            }
        }
        mAccount = account;
        mAddEditAccountViewModel.setAccountColor(account.getBackgroundColor());
        mAccountNameTIET.setText(account.getName());
        mInitialValueTIET.setText(account.getInitialValue().toPlainString());
        mCurrencySpinner.setSelectedIndex(
                Arrays.asList(CurrencyUtil.getCurrencies()).indexOf(mAccount.getDefaultCurrency())
        );
        mTypeSpinner.setSelectedIndex(mAccount.getType() - 1);
        mShownInDashboardSwitch.setChecked(account.isShowInDashboard());
        // Restore container visibility once all data has been initialized
        // TODO: How can we prevent Switch animation from triggering?
        mBoxLayout.setVisibility(View.VISIBLE);
    }

    private void onAccountColorChanged(@ColorInt int color) {
        // 1 is an invalid HEX number, no point in trying to apply color to elements
        if (color == 1) return;
        // If account is null (null != empty) then no data has been received from Room.
        // That means that we will receive a second color pretty soon and therefore
        // this first coloring can be skipped.
        if (mAccount == null) return;

        int textColor = ColorUtil.getTextColor(color);

        mAccount.setBackgroundColor(color);
        mAppbar.setBackgroundColor(color);
        mAppbar.setTitleTextColor(textColor);
        mUpArrow.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);

        //noinspection ConstantConditions
        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);
        getWindow().setStatusBarColor(ColorUtil.getDarkVariant(color));
        LinearLayout container = (LinearLayout) findViewById(R.id.addedit_account_container);
        ImageButton palette = (ImageButton) findViewById(R.id.addedit_account_color_button);

        // Custom ColorStateLists
        // TODO: Create util class to retrieve custom colorStateLists
        ColorStateList switchCSL = getSwitchColorStateList(color);
        ColorStateList nameCSL = getStrokeColorStateList(textColor);
        ColorStateList textCSL = ColorStateList.valueOf(textColor);
        ColorStateList bgCSL = ColorStateList.valueOf(color);
        int fabBgColor = ColorUtil.getVariantByFactor(color, 0.86f);
        int fabTextColor = ColorUtil.getTextColor(fabBgColor);

        container.setBackgroundColor(color);
        mFAB.setBackgroundTintList(ColorStateList.valueOf(fabBgColor));
        mFAB.getDrawable().mutate().setTint(fabTextColor);
        mFAB.setRippleColor(textColor);

        mAccountNameTIL.setBoxStrokeColorStateList(nameCSL);
        mAccountNameTIL.setDefaultHintTextColor(textCSL);
        mAccountNameTIET.setTextColor(textColor);

        mInitialValueTIL.setBoxStrokeColor(color);
        mInitialValueTIL.setHintTextColor(bgCSL);

        mAccountNameTIL.setErrorTextColor(textCSL);
        mAccountNameTIL.setErrorIconTintList(textCSL);
        mAccountNameTIL.setBoxStrokeErrorColor(textCSL);

        mShownInDashboardSwitch.getThumbDrawable().setTintList(switchCSL);
        mShownInDashboardSwitch.getTrackDrawable().setTintList(switchCSL);

        palette.setBackgroundTintList(bgCSL);
        palette.getDrawable().mutate().setTint(textColor);
    }

    private void initSpinners() {
        try {
            String[] currencies = CurrencyUtil.getDisplayArray();
            String[] accountTypes = getResources().getStringArray(R.array.account_types);

            mCurrencySpinner.setItems(currencies);
            mTypeSpinner.setItems(accountTypes);
        } catch (GnomyCurrencyException e) {
            // This shouldn't happen
            Log.wtf("AddEditAccount", "setLists: CURRENCIES array triggers error", e);
        }
    }

    private void setInputFilters() {
        TextInputEditText valueTIET = (TextInputEditText) findViewById(R.id.addedit_account_initial_value_input);
        valueTIET.setFilters(new InputFilter[]{new InputFilterMinMax(Account.MIN_INITIAL, Account.MAX_INITIAL, Account.DECIMAL_SCALE)});
    }

    private ColorStateList getSwitchColorStateList(int color) {
        return new ColorStateList(
            new int[][]{
                new int[]{-android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_enabled},
                new int[]{}
            },
            new int[]{
                Color.GRAY,
                Color.LTGRAY,
                color,
                color,
            }
        );
    }

    private ColorStateList getStrokeColorStateList(int color) {
        return new ColorStateList(
            new int[][]{
                new int[]{-android.R.attr.state_enabled},
                new int[]{
                    -android.R.attr.state_focused,
                    android.R.attr.state_focused,
                },
                new int[]{}
            },
            new int[]{
                Color.GRAY,
                color,
                color,
            }
        );
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
                    mCurrencySpinner.getSelectedIndex());
            int accountType = mTypeSpinner.getSelectedIndex() + 1;
            boolean showInDashboard = mShownInDashboardSwitch.isChecked();

            saveData(currencyCode, accountType, showInDashboard);
        } else {
            Toast.makeText(this, getResources().getString(R.string.form_error), Toast.LENGTH_LONG).show();
        }
    }

    private void onAccountNameEditTextChanges(String value) {
        if (mAccount != null) mAccount.setName(value);

        if (value.trim().length() == 0) {
            if (mAddEditAccountViewModel.accountNameIsPristine()) return;
            mAccountNameTIL.setError(getResources().getString(R.string.account_error_name));
        } else {
            mAccountNameTIL.setErrorEnabled(false);
        }

        mAddEditAccountViewModel.notifyAccountNameChanged();
    }

    private void onInitialValueEditTextChanges(String value) {
        if (value.length() == 0) {
            if (mAddEditAccountViewModel.initialValueIsPristine()) return;
            mInitialValueTIL.setError(getResources().getString(R.string.account_error_initial_value));
        } else {
            if (mAccount != null) mAccount.setInitialValue(value);
            mInitialValueTIL.setErrorEnabled(false);
        }

        mAddEditAccountViewModel.notifyInitialValueChanged();
    }

    @SuppressWarnings("ConstantConditions")
    private boolean validateTextFields() {
        mAddEditAccountViewModel.notifyAccountNameChanged();
        mAddEditAccountViewModel.notifyInitialValueChanged();

        String accountName = mAccountNameTIET.getText().toString();
        String initialValueString = mInitialValueTIET.getText().toString();
        onAccountNameEditTextChanges(accountName);
        onInitialValueEditTextChanges(initialValueString);

        return accountName.length() > 0 && initialValueString.length() > 0;
    }

    private void saveData(String currencyCode, int accountType, boolean includeInHomepage) {
        try {
            String toastMessage;

            mAccount.setShowInDashboard(includeInHomepage);
            mAccount.setType(accountType);
            mAccount.setDefaultCurrency(currencyCode);

            mFAB.setEnabled(false);
            if (mAccount.getId() == 0) {
                mAddEditAccountViewModel.insert(mAccount);
                toastMessage = getResources().getString(R.string.account_message_saved);
            } else {
                mAddEditAccountViewModel.update(mAccount);
                toastMessage = getResources().getString(R.string.account_message_updated);
            }

            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
            finish();
        } catch(NumberFormatException nfe) {
            Log.wtf("AddEditAccount", "saveData: Initial value validation failed", nfe);
        }
    }
}
