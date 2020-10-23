package io.github.alansanchezp.gnomy.ui.account;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
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
import io.github.alansanchezp.gnomy.viewmodel.account.AccountAddEditViewModel;

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
import android.view.InflateException;
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
    // TODO: Evaluate if onSaveInstanceState and onRestoreInstanceState should be replaced with ViewModel
    static final String EXTRA_ID = "account_id";
    static final String EXTRA_BG_COLOR = "account_bg_color";
    static final String EXTRA_NAME = "account_name";
    static final String EXTRA_INITIAL_VALUE = "account_initial_value";
    static final String EXTRA_INCLUDED_IN_SUM = "account_included_in_sum";
    static final String EXTRA_CURRENCY = "account_currency";
    static final String EXTRA_TYPE = "account_type";
    static final String SAVED_NAME_PRISTINE = "name_is_pristine";
    static final String SAVED_INITIAL_VALUE_PRISTINE = "initial_value_is_pristine";
    static final String TAG_PICKER_DIALOG = "color_picker_dialog";
    private Account mAccount;
    private boolean mNameInputIsPristine = true;
    private boolean mValueInputIsPristine = true;
    private Toolbar mAppbar;
    private Drawable mUpArrow;
    private AccountAddEditViewModel mAccountViewModel;
    private FloatingActionButton mFAB;
    // Only used for edit purposes
    protected int mAccountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_account);


        mAppbar = (Toolbar) findViewById(R.id.custom_appbar);
        setSupportActionBar(mAppbar);

        mAccountViewModel = new ViewModelProvider(this,
                new SavedStateViewModelFactory(
                        this.getApplication(), (SavedStateRegistryOwner) this))
                .get(AccountAddEditViewModel.class);

        TextInputLayout nameTIL = findViewById(R.id.new_account_name);
        TextInputLayout valueTIL = findViewById(R.id.new_account_initial_value);
        if (nameTIL.getEditText() == null || valueTIL.getEditText() == null)
            throw new InflateException("<R.id.new_account_name> and <R.id.new_account_initial_value> are expected to have a child TextInputEditText element.");

        Intent intent = getIntent();
        mAccountId = intent.getIntExtra(EXTRA_ID, 0);
        LiveData<Account> accountLD = mAccountViewModel.getAccount(mAccountId);

        String activityTitle;
        if (accountLD != null) {
            activityTitle = getString(R.string.account_card_modify);
            accountLD.observe(this, account -> {
                if (account == null) {
                    Log.e("AddEditAccount", "onCreate: Account not found. Closing activity.");
                    finish();
                    return;
                }
                mAccount = account;
                mAccountViewModel.setAccountColor(account.getBackgroundColor());
                nameTIL.getEditText().setText(account.getName());
                valueTIL.getEditText().setText(account.getInitialValue().toPlainString());
                Switch includeInSwitch = (Switch) findViewById(R.id.new_account_show_in_home);
                includeInSwitch.setChecked(account.isShowInDashboard());
            });
        } else {
            activityTitle = getString(R.string.account_new);
            mAccount = new Account();

            // Only generate new color if viewModel doesn't have one stored already
            if (Objects.requireNonNull(mAccountViewModel.accountColor.getValue()) == 1) {
                mAccountViewModel.setAccountColor(ColorUtil.getRandomColor());
            }
        }

        setTitle(activityTitle);
        setSupportActionBar(mAppbar);
        mUpArrow = getResources().getDrawable(R.drawable.abc_vector_test);

        //noinspection ConstantConditions
        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFAB = (FloatingActionButton) findViewById(R.id.new_account_ok);

        mAccountViewModel.accountColor.observe(this, this::setColors);
        setLists(intent.getStringExtra(EXTRA_CURRENCY),
                intent.getIntExtra(EXTRA_TYPE, 0));
        setFilters();

        //noinspection ConstantConditions
        nameTIL.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateName();
                mNameInputIsPristine = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        //noinspection ConstantConditions
        valueTIL.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateValueString();
                mValueInputIsPristine = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(SAVED_NAME_PRISTINE, mNameInputIsPristine);
        savedInstanceState.putBoolean(SAVED_INITIAL_VALUE_PRISTINE, mValueInputIsPristine);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean nameIsPristine = savedInstanceState.getBoolean(SAVED_NAME_PRISTINE);
        boolean valueIsPristine = savedInstanceState.getBoolean(SAVED_INITIAL_VALUE_PRISTINE);

        if (nameIsPristine) {
            mNameInputIsPristine = true;
            TextInputLayout nameTIL = (TextInputLayout) findViewById(R.id.new_account_name);
            nameTIL.setErrorEnabled(false);
        }
        if (valueIsPristine) {
            mValueInputIsPristine = true;
            TextInputLayout valueTIL = (TextInputLayout) findViewById(R.id.new_account_initial_value);
            valueTIL.setErrorEnabled(false);
        }
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

    protected void setColors(@ColorInt int color) {
        // 1 is an invalid HEX number, no point in trying to color elements
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
        LinearLayout container = (LinearLayout) findViewById(R.id.new_account_container);
        TextInputLayout nameTIL = (TextInputLayout) findViewById(R.id.new_account_name);
        TextInputEditText nameTIET = (TextInputEditText) findViewById(R.id.new_account_name_input);
        TextInputLayout valueTIL = (TextInputLayout) findViewById(R.id.new_account_initial_value);
        Switch includeInSwitch = (Switch) findViewById(R.id.new_account_show_in_home);
        ImageButton palette = (ImageButton) findViewById(R.id.new_account_color_button);

        // Custom ColorStateLists
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

        nameTIL.setBoxStrokeColorStateList(nameCSL);
        nameTIL.setDefaultHintTextColor(textCSL);
        nameTIET.setTextColor(textColor);

        valueTIL.setBoxStrokeColor(color);
        valueTIL.setHintTextColor(bgCSL);

        nameTIL.setErrorTextColor(textCSL);
        nameTIL.setErrorIconTintList(textCSL);
        nameTIL.setBoxStrokeErrorColor(textCSL);

        includeInSwitch.getThumbDrawable().setTintList(switchCSL);
        includeInSwitch.getTrackDrawable().setTintList(switchCSL);

        palette.setBackgroundTintList(bgCSL);
        palette.getDrawable().mutate().setTint(textColor);
    }

    protected void setLists(String currencyCode, int accountType) {
        MaterialSpinner currencySpinner = (MaterialSpinner) findViewById(R.id.new_account_currency);
        MaterialSpinner typeSpinner = (MaterialSpinner) findViewById(R.id.new_account_type);

        try {
            String[] currencies = CurrencyUtil.getDisplayArray();
            String[] accountTypes = getResources().getStringArray(R.array.account_types);

            currencySpinner.setItems(currencies);
            if (currencyCode != null) {
                currencySpinner.setSelectedIndex(
                        Arrays.asList(CurrencyUtil.getCurrencies()).indexOf(currencyCode)
                );
            }
            typeSpinner.setItems(accountTypes);
            if (accountType != 0) {
                typeSpinner.setSelectedIndex(accountType - 1);
            }
        } catch (GnomyCurrencyException e) {
            // This shouldn't happen
            Log.wtf("NewAccountActivity", "setLists: CURRENCIES array triggers error", e);
        }
    }

    protected void setFilters() {
        TextInputEditText valueTIET = (TextInputEditText) findViewById(R.id.new_account_initial_value_input);
        valueTIET.setFilters(new InputFilter[]{new InputFilterMinMax(Account.MIN_INITIAL, Account.MAX_INITIAL, Account.DECIMAL_SCALE)});
    }

    protected ColorStateList getSwitchColorStateList(int color) {
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

    protected ColorStateList getStrokeColorStateList(int color) {
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
                .setSelectedColor(Objects.requireNonNull(mAccountViewModel.accountColor.getValue()))
                .setDismissOnColorSelected(true)
                .setOutlineWidth(0)
                .setFixedColumnCount(5)
                .setOnColorSelectedListener((positiveResult, color) -> {
                    if (positiveResult) {
                        Log.d("AEAActivity", "showColorPicker: setting color " + color);
                        mAccountViewModel.setAccountColor(color);
                    }
                }).build().show(getSupportFragmentManager(), TAG_PICKER_DIALOG);
    }

    public void processData(View v) {
        boolean isValid = validateData();

        if (isValid) {
            TextInputEditText nameTIET = (TextInputEditText) findViewById(R.id.new_account_name_input);
            TextInputEditText valueTIET = (TextInputEditText) findViewById(R.id.new_account_initial_value_input);
            Switch includeInSwitch = (Switch) findViewById(R.id.new_account_show_in_home);
            MaterialSpinner currencySpinner = (MaterialSpinner) findViewById(R.id.new_account_currency);
            MaterialSpinner typeSpinner = (MaterialSpinner) findViewById(R.id.new_account_type);

            int currencyIndex = currencySpinner.getSelectedIndex();

            String initialValueString = valueTIET.getText().toString();
            String name = nameTIET.getText().toString();
            int accountType = typeSpinner.getSelectedIndex() + 1;
            String currencyCode = CurrencyUtil.getCurrencyCode(currencyIndex);
            boolean includeInHomepage = includeInSwitch.isChecked();

            saveData(name, initialValueString, currencyCode, accountType, includeInHomepage);
        } else {
            Toast.makeText(this, getResources().getString(R.string.form_error), Toast.LENGTH_LONG).show();
        }
    }

    protected boolean validateName() {
        TextInputLayout nameTIL = (TextInputLayout) findViewById(R.id.new_account_name);
        try {
            String name = nameTIL.getEditText().getText().toString();

            if (name.trim().length() == 0) {
                nameTIL.setError(getResources().getString(R.string.account_error_name));
                return false;
            }
            nameTIL.setErrorEnabled(false);
            return true;
        } catch (NullPointerException npe) {
            Log.e("NewAccountActivity", "validateName: ", npe);
            return false;
        }
    }

    protected boolean validateValueString() {
        TextInputLayout valueTIL = (TextInputLayout) findViewById(R.id.new_account_initial_value);
        try {
            String initialValueString = valueTIL.getEditText().getText().toString();

            if (initialValueString.length() == 0) {
                valueTIL.setError(getResources().getString(R.string.account_error_initial_value));
                return false;
            }
            valueTIL.setErrorEnabled(false);
            return true;
        } catch (NullPointerException npe) {
            Log.e("NewAccountActivity", "validateValueString: ", npe);
            return false;
        }
    }

    protected boolean validateData() {
        return validateName() && validateValueString();
    }

    protected void saveData(String name, String initialValueString, String currencyCode, int accountType, boolean includeInHomepage) {
        try {
            Account account = mAccount;
            String toastMessage;

            account.setName(name);
            account.setInitialValue(initialValueString);
            account.setShowInDashboard(includeInHomepage);
            account.setType(accountType);
            account.setDefaultCurrency(currencyCode);

            mFAB.setEnabled(false);
            if (mAccountId == 0) {
                mAccountViewModel.insert(account);
                toastMessage = getResources().getString(R.string.account_message_saved);
            } else {
                account.setId(mAccountId);
                mAccountViewModel.update(account);
                toastMessage = getResources().getString(R.string.account_message_updated);
            }

            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
            finish();
        } catch(NumberFormatException nfe) {
            Log.wtf("NewAccountActivity", "saveData: Initial value validation failed", nfe);
        }
    }
}
