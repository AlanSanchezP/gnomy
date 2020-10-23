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
    static final String EXTRA_ID = "account_id";
    static final String EXTRA_BG_COLOR = "account_bg_color";
    static final String EXTRA_NAME = "account_name";
    static final String EXTRA_INITIAL_VALUE = "account_initial_value";
    static final String EXTRA_INCLUDED_IN_SUM = "account_included_in_sum";
    static final String EXTRA_CURRENCY = "account_currency";
    static final String EXTRA_TYPE = "account_type";
    static final String TAG_PICKER_DIALOG = "color_picker_dialog";
    private Account mAccount;
    private Toolbar mAppbar;
    private Drawable mUpArrow;
    private TextInputEditText mAccountNameTIET;
    private TextInputEditText mInitialValueTIET;
    private AccountAddEditViewModel mAccountViewModel;
    private FloatingActionButton mFAB;

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

        mAccountNameTIET = findViewById(R.id.new_account_name_input);
        mInitialValueTIET = findViewById(R.id.new_account_initial_value_input);

        Intent intent = getIntent();
        int accountId = intent.getIntExtra(EXTRA_ID, 0);
        LiveData<Account> accountLD = mAccountViewModel.getAccount(accountId);

        String activityTitle;
        if (accountLD != null) {
            activityTitle = getString(R.string.account_card_modify);
            accountLD.observe(this, this::onAccountChanged);
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

        mAccountViewModel.accountColor.observe(this, this::onAccountColorChanged);
        setLists(intent.getStringExtra(EXTRA_CURRENCY),
                intent.getIntExtra(EXTRA_TYPE, 0));
        setFilters();

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

    public void onAccountChanged(Account account) {
        if (account == null) {
            Log.e("AddEditAccount", "onCreate: Account not found. Closing activity.");
            finish();
            return;
        }
        mAccount = account;
        mAccountViewModel.setAccountColor(account.getBackgroundColor());
        mAccountNameTIET.setText(account.getName());
        mInitialValueTIET.setText(account.getInitialValue().toPlainString());
        Switch includeInSwitch = (Switch) findViewById(R.id.new_account_show_in_home);
        includeInSwitch.setChecked(account.isShowInDashboard());
    }

    public void onAccountColorChanged(@ColorInt int color) {
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
            Log.wtf("AddEditAccount", "setLists: CURRENCIES array triggers error", e);
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
                        mAccountViewModel.setAccountColor(color);
                    }
                }).build().show(getSupportFragmentManager(), TAG_PICKER_DIALOG);
    }

    public void processData(View v) {
        boolean texFieldsAreValid = validateTextFields();

        if (texFieldsAreValid) {
            Switch includeInSwitch = (Switch) findViewById(R.id.new_account_show_in_home);
            MaterialSpinner currencySpinner = (MaterialSpinner) findViewById(R.id.new_account_currency);
            MaterialSpinner typeSpinner = (MaterialSpinner) findViewById(R.id.new_account_type);

            int currencyIndex = currencySpinner.getSelectedIndex();

            int accountType = typeSpinner.getSelectedIndex() + 1;
            String currencyCode = CurrencyUtil.getCurrencyCode(currencyIndex);
            boolean includeInHomepage = includeInSwitch.isChecked();

            saveData(currencyCode, accountType, includeInHomepage);
        } else {
            Toast.makeText(this, getResources().getString(R.string.form_error), Toast.LENGTH_LONG).show();
        }
    }

    private void onAccountNameEditTextChanges(String value) {
        TextInputLayout nameTIL = (TextInputLayout) findViewById(R.id.new_account_name);
        mAccount.setName(value);

        if (value.trim().length() == 0) {
            if (mAccountViewModel.accountNameIsPristine()) return;
            nameTIL.setError(getResources().getString(R.string.account_error_name));
        } else {
            nameTIL.setErrorEnabled(false);
        }

        mAccountViewModel.notifyAccountNameChanged();
    }

    private void onInitialValueEditTextChanges(String value) {
        TextInputLayout valueTIL = (TextInputLayout) findViewById(R.id.new_account_initial_value);

        if (value.length() == 0) {
            if (mAccountViewModel.initialValueIsPristine()) return;
            valueTIL.setError(getResources().getString(R.string.account_error_initial_value));
        } else {
            mAccount.setInitialValue(value);
            valueTIL.setErrorEnabled(false);
        }

        mAccountViewModel.notifyInitialValueChanged();
    }

    @SuppressWarnings("ConstantConditions")
    private boolean validateTextFields() {
        mAccountViewModel.notifyAccountNameChanged();
        mAccountViewModel.notifyInitialValueChanged();
        onAccountNameEditTextChanges(mAccountNameTIET.getText().toString());
        onInitialValueEditTextChanges(mInitialValueTIET.getText().toString());

        return mAccountNameTIET.getParent().toString().length() > 0 &&
                mInitialValueTIET.getText().toString().length() > 0;
    }

    protected void saveData(String currencyCode, int accountType, boolean includeInHomepage) {
        try {
            String toastMessage;

            mAccount.setShowInDashboard(includeInHomepage);
            mAccount.setType(accountType);
            mAccount.setDefaultCurrency(currencyCode);

            mFAB.setEnabled(false);
            if (mAccount.getId() == 0) {
                mAccountViewModel.insert(mAccount);
                toastMessage = getResources().getString(R.string.account_message_saved);
            } else {
                mAccountViewModel.update(mAccount);
                toastMessage = getResources().getString(R.string.account_message_updated);
            }

            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
            finish();
        } catch(NumberFormatException nfe) {
            Log.wtf("AddEditAccount", "saveData: Initial value validation failed", nfe);
        }
    }
}
