package io.github.alansanchezp.gnomy.ui.account;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.util.android.InputFilterMinMax;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.viewmodel.AccountViewModel;

import android.content.DialogInterface;
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

public class NewAccountActivity extends AppCompatActivity {
    protected int mBgColor;
    protected int mTextColor;
    protected boolean mNameInputIsPristine = true;
    protected boolean mValueInputIsPristine = true;
    protected Toolbar mToolbar;
    protected Drawable mUpArrow;
    protected String mActivityTitle;
    protected AccountViewModel mAccountViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        mAccountViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(AccountViewModel.class);
        initAccountData();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(mActivityTitle);
        setSupportActionBar(mToolbar);
        mUpArrow = getResources().getDrawable(R.drawable.abc_vector_test);

        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setColors();
        setLists();
        setFilters();

        TextInputLayout nameTIL = (TextInputLayout) findViewById(R.id.new_account_name);
        TextInputLayout valueTIL = (TextInputLayout) findViewById(R.id.new_account_initial_value);

        nameTIL.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isValid = validateName();
                mNameInputIsPristine = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        valueTIL.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isValid = validateValueString();
                mValueInputIsPristine = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("bgcolor", mBgColor);
        savedInstanceState.putBoolean("nameIsPristine", mNameInputIsPristine);
        savedInstanceState.putBoolean("valueIsPristine", mValueInputIsPristine);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean nameIsPristine = savedInstanceState.getBoolean("nameIsPristine");
        boolean valueIsPristine = savedInstanceState.getBoolean("valueIsPristine");
        mBgColor = savedInstanceState.getInt("bgcolor");

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

        setColors();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirmation_dialog_title))
                .setMessage(getString(R.string.confirmation_dialog_description))
                .setPositiveButton(getString(R.string.confirmation_dialog_yes), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.confirmation_dialog_no), null)
                .show();
    }

    protected void initAccountData() {
        mBgColor = ColorUtil.getRandomColor();
        mActivityTitle = getString(R.string.account_new);
    }

    protected void setColors() {
        mTextColor = ColorUtil.getTextColor(mBgColor);
        mToolbar.setBackgroundColor(mBgColor);
        mToolbar.setTitleTextColor(mTextColor);
        mUpArrow.setColorFilter(mTextColor, PorterDuff.Mode.SRC_ATOP);

        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);
        getWindow().setStatusBarColor(ColorUtil.getDarkVariant(mBgColor));
        LinearLayout container = (LinearLayout) findViewById(R.id.new_account_container);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.new_account_ok);
        TextInputLayout nameTIL = (TextInputLayout) findViewById(R.id.new_account_name);
        TextInputEditText nameTIET = (TextInputEditText) findViewById(R.id.new_account_name_input);
        TextInputLayout valueTIL = (TextInputLayout) findViewById(R.id.new_account_initial_value);
        Switch includeInSwitch = (Switch) findViewById(R.id.new_account_show_in_home);
        ImageButton palette = (ImageButton) findViewById(R.id.new_account_color_button);

        // Custom ColorStateLists
        ColorStateList switchCSL = getSwitchColorStateList(mBgColor);
        ColorStateList nameCSL = getStrokeColorStateList(mTextColor);
        ColorStateList textCSL = ColorStateList.valueOf(mTextColor);
        ColorStateList bgCSL = ColorStateList.valueOf(mBgColor);

        container.setBackgroundColor(mBgColor);
        fab.setBackgroundTintList(bgCSL);
        fab.getDrawable().mutate().setTint(mTextColor);
        fab.setRippleColor(mTextColor);

        nameTIL.setBoxStrokeColorStateList(nameCSL);
        nameTIL.setDefaultHintTextColor(textCSL);
        nameTIET.setTextColor(mTextColor);

        valueTIL.setBoxStrokeColor(mBgColor);
        valueTIL.setHintTextColor(bgCSL);

        nameTIL.setErrorTextColor(textCSL);
        nameTIL.setErrorIconTintList(textCSL);
        nameTIL.setBoxStrokeErrorColor(textCSL);

        includeInSwitch.getThumbDrawable().setTintList(switchCSL);
        includeInSwitch.getTrackDrawable().setTintList(switchCSL);

        palette.setBackgroundTintList(bgCSL);
        palette.getDrawable().mutate().setTint(mTextColor);
    }

    protected void setLists() {
        MaterialSpinner currencySpinner = (MaterialSpinner) findViewById(R.id.new_account_currency);
        MaterialSpinner typeSpinner = (MaterialSpinner) findViewById(R.id.new_account_type);

        try {
            String[] currencies = CurrencyUtil.getDisplayArray();
            String[] accountTypes = getResources().getStringArray(R.array.account_types);

            currencySpinner.setItems(currencies);
            typeSpinner.setItems(accountTypes);
        } catch (GnomyCurrencyException e) {
            // This shouldn't happen
            Log.wtf("NewAccountActivity", "setLists: CURRENCIES array triggers error", e);
        }
    }

    protected void setFilters() {
        TextInputEditText valueTIET = (TextInputEditText) findViewById(R.id.new_account_initial_value_input);
        valueTIET.setFilters(new InputFilter[]{new InputFilterMinMax(Account.MIN_INITIAL, Account.MAX_INITIAL)});
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
                .setSelectedColor(mBgColor)
                .setDismissOnColorSelected(true)
                .setOutlineWidth(0)
                .setFixedColumnCount(5)
                .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                    @Override public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                        if (positiveResult) {
                            mBgColor = color;
                            setColors();
                        }
                    }
                }).build().show(getSupportFragmentManager(),"dialog");
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
            Account account = new Account();

            account.setName(name);
            account.setInitialValue(initialValueString);
            account.setShowInDashboard(includeInHomepage);
            account.setType(accountType);
            account.setDefaultCurrency(currencyCode);
            account.setBackgroundColor(mBgColor);
            account.setCreatedAt();

            mAccountViewModel.insert(account);

            Toast.makeText(this, getResources().getString(R.string.account_message_saved), Toast.LENGTH_LONG).show();
            finish();
        } catch(NumberFormatException nfe) {
            Log.wtf("NewAccountActivity", "saveData: Initial value validation failed", nfe);
        }
    }
}
