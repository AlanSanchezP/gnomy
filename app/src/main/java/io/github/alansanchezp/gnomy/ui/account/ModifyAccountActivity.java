package io.github.alansanchezp.gnomy.ui.account;

import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;

public class ModifyAccountActivity extends NewAccountActivity {
    protected int mAccountId;
    protected Account mAccount;
    protected boolean mFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initAccountData() {
        mActivityTitle = getString(R.string.account_card_modify);
        mAccountId = getIntent().getIntExtra("accountId", 0);
        LiveData<Account> account = mAccountViewModel.getAccount(mAccountId);
        account.observe(this, new Observer<Account>() {
            @Override
            public void onChanged(@Nullable final Account account) {
                mAccount = account;

                if (mFirstTime) {
                    mBgColor = mAccount.getBackgroundColor();
                    TextInputLayout nameTIL = (TextInputLayout) findViewById(R.id.new_account_name);
                    TextInputLayout valueTIL = (TextInputLayout) findViewById(R.id.new_account_initial_value);
                    Switch includeInSwitch = (Switch) findViewById(R.id.new_account_show_in_home);

                    nameTIL.getEditText().setText(mAccount.getName());
                    valueTIL.getEditText().setText(mAccount.getInitialValue().toString());
                    includeInSwitch.setChecked(mAccount.isShowInDashboard());
                }

                setColors();
                setLists();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mFirstTime = false;
    }

    @Override
    protected void setColors() {
        if (mAccount == null) return;
        super.setColors();
    }

    @Override
    protected void setLists() {
        if (mAccount == null) {
            super.setLists();
            return;
        }

        if (mFirstTime) {
            MaterialSpinner currencySpinner = (MaterialSpinner) findViewById(R.id.new_account_currency);
            MaterialSpinner typeSpinner = (MaterialSpinner) findViewById(R.id.new_account_type);

            List<String> currencies = Arrays.asList(CurrencyUtil.getCurrencies());
            currencySpinner.setSelectedIndex(currencies.indexOf(mAccount.getDefaultCurrency()));
            typeSpinner.setSelectedIndex(mAccount.getType() - 1);
        }
    }

    @Override
    protected void saveData(String name, String initialValueString, String currencyCode, int accountType, boolean includeInHomepage) {
        try {
            Account oldAccount = new Account(mAccount);

            mAccount.setName(name);
            mAccount.setInitialValue(initialValueString);
            mAccount.setShowInDashboard(includeInHomepage);
            mAccount.setType(accountType);
            mAccount.setDefaultCurrency(currencyCode);
            mAccount.setBackgroundColor(mBgColor);

            mAccountViewModel.update(oldAccount, mAccount);

            Toast.makeText(this, getResources().getString(R.string.account_message_saved), Toast.LENGTH_LONG).show();
            finish();
        } catch(NumberFormatException nfe) {
            Log.wtf("NewAccountActivity", "saveData: Initial value validation failed", nfe);
        }
    }
}
