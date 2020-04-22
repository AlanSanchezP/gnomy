package io.github.alansanchezp.gnomy.ui.account;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.filter.InputFilterMinMax;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.util.GraphicUtil;

import android.content.res.ColorStateList;
import android.graphics.Color;
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

import java.math.BigDecimal;

public class NewAccountActivity extends AppCompatActivity {
    protected int bgColor;
    protected int textColor;
    protected boolean nameInputIsPristine = true;
    protected boolean valueInputIsPristine = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        bgColor = GraphicUtil.getRandomColor();

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
                nameInputIsPristine = false;
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
                valueInputIsPristine = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("bgcolor", bgColor);
        savedInstanceState.putBoolean("nameIsPristine", nameInputIsPristine);
        savedInstanceState.putBoolean("valueIsPristine", valueInputIsPristine);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean nameIsPristine = savedInstanceState.getBoolean("nameIsPristine");
        boolean valueIsPristine = savedInstanceState.getBoolean("valueIsPristine");
        bgColor = savedInstanceState.getInt("bgcolor");

        if (nameIsPristine) {
            nameInputIsPristine = true;
            TextInputLayout nameTIL = (TextInputLayout) findViewById(R.id.new_account_name);
            nameTIL.setErrorEnabled(false);
        }
        if (valueIsPristine) {
            valueInputIsPristine = true;
            TextInputLayout valueTIL = (TextInputLayout) findViewById(R.id.new_account_initial_value);
            valueTIL.setErrorEnabled(false);
        }

        setColors();
    }

    protected void setColors() {
        textColor = GraphicUtil.getTextColor(bgColor);
        LinearLayout container = (LinearLayout) findViewById(R.id.new_account_container);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.new_account_ok);
        TextInputLayout nameTIL = (TextInputLayout) findViewById(R.id.new_account_name);
        TextInputEditText nameTIET = (TextInputEditText) findViewById(R.id.new_account_name_input);
        TextInputLayout valueTIL = (TextInputLayout) findViewById(R.id.new_account_initial_value);
        Switch includeInSwitch = (Switch) findViewById(R.id.new_account_show_in_home);
        ImageButton palette = (ImageButton) findViewById(R.id.new_account_color_button);

        // Custom ColorStateLists
        ColorStateList switchCSL = getSwitchColorStateList(bgColor);
        ColorStateList nameCSL = getStrokeColorStateList(textColor);

        container.setBackgroundColor(bgColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        fab.getDrawable().mutate().setTint(textColor);
        fab.setRippleColor(textColor);

        nameTIL.setBoxStrokeColorStateList(nameCSL);
        nameTIL.setDefaultHintTextColor(ColorStateList.valueOf(textColor));
        nameTIET.setTextColor(textColor);

        valueTIL.setBoxStrokeColor(bgColor);
        valueTIL.setHintTextColor(ColorStateList.valueOf(bgColor));

        if (textColor == 0XFF000000) {
            nameTIL.setErrorTextColor(ColorStateList.valueOf(0XFFFF0000));
            nameTIL.setErrorIconTintList(ColorStateList.valueOf(0XFFFF0000));
            nameTIL.setBoxStrokeErrorColor(ColorStateList.valueOf(0XFFFF0000));
        } else {
            nameTIL.setErrorTextColor(ColorStateList.valueOf(textColor));
            nameTIL.setErrorIconTintList(ColorStateList.valueOf(textColor));
            nameTIL.setBoxStrokeErrorColor(ColorStateList.valueOf(textColor));
        }

        includeInSwitch.getThumbDrawable().setTintList(switchCSL);
        includeInSwitch.getTrackDrawable().setTintList(switchCSL);

        palette.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        palette.getDrawable().mutate().setTint(textColor);
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
            // This shoudln't happen
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
                .setColors(GraphicUtil.getColors())
                .setSelectedColor(bgColor)
                .setDismissOnColorSelected(true)
                .setOutlineWidth(0)
                .setFixedColumnCount(5)
                .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                    @Override public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                        if (positiveResult) {
                            bgColor = color;
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

            try {
                String name = nameTIET.getText().toString();
                BigDecimal initialValue = new BigDecimal(initialValueString)
                        .setScale(4, BigDecimal.ROUND_HALF_EVEN);
                int accountType = typeSpinner.getSelectedIndex() + 1;
                String currencyCode = CurrencyUtil.getCurrencyCode(currencyIndex);
                boolean includeInHomepage = includeInSwitch.isChecked();

                saveData(name, initialValue, currencyCode, accountType, includeInHomepage);
            } catch (NumberFormatException nfe) {
                Log.wtf("NewAccountActivity", "processData: Initial Value validation failed", nfe);
            }
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

    protected void saveData(String name, BigDecimal initialValue, String currencyCode, int accountType, boolean includeInHomepage) {
        AccountRepository repository = new AccountRepository(getApplicationContext());
        Account account = new Account();

        account.setName(name);
        account.setInitialValue(initialValue);
        account.setShowInDashboard(includeInHomepage);
        account.setType(accountType);
        account.setDefaultCurrency(currencyCode);
        account.setBackgroundColor(bgColor);
        account.setCreatedAt();

        repository.insert(account);
        Toast.makeText(this, getResources().getString(R.string.account_message_saved), Toast.LENGTH_LONG).show();
        finish();
    }
}
