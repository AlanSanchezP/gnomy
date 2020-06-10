package io.github.alansanchezp.gnomy.ui.account;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.threeten.bp.YearMonth;

import java.math.BigDecimal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.viewmodel.AccountHistoryViewModel;

public class AccountHistoryActivity extends AppCompatActivity {
    static final String EXTRA_ID = "account_id";
    static final String EXTRA_NAME = "account_name";
    static final String EXTRA_CURRENCY = "account_currency";
    static final String EXTRA_BG_COLOR = "bg_color";
    private Toolbar mSecondaryBar;
    private Drawable mUpArrow;
    private AccountHistoryViewModel mAccountHistoryViewModel;
    private LiveData<BigDecimal> mAccumulated;
    private LiveData<MonthlyBalance> mBalance;
    private int mToolbarBgColor;
    private int mTitleTextColor;
    private int mAccountId;
    private String mAccountName;
    private String mAccountCurrency;
    private Toolbar mMainBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_history);

        Intent intent = getIntent();
        mAccountId = intent.getIntExtra(EXTRA_ID, 0);
        mAccountName = intent.getStringExtra(EXTRA_NAME);
        mAccountCurrency = intent.getStringExtra(EXTRA_CURRENCY);
        mToolbarBgColor = intent.getIntExtra(EXTRA_BG_COLOR, 0XFF);
        mTitleTextColor = ColorUtil.getTextColor(mToolbarBgColor);

        mMainBar = (Toolbar) findViewById(R.id.toolbar);
        mMainBar.setTitle(mAccountName + " " + getString(R.string.account_balance_history_legend));
        setSupportActionBar(mMainBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUpArrow = getResources().getDrawable(R.drawable.abc_vector_test);

        mSecondaryBar = (Toolbar) findViewById(R.id.toolbar2);

        setColors();

        mAccountHistoryViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(AccountHistoryViewModel.class);
        mAccountHistoryViewModel.getPublicMonthFilter().observe(this, new Observer<YearMonth>() {
            @Override
            public void onChanged(@Nullable final YearMonth month) {
                updateMonth(month);
            }
        });

        mAccumulated = mAccountHistoryViewModel.getAccumulatedFromMonth(mAccountId);
        mBalance = mAccountHistoryViewModel.getBalanceFromMonth(mAccountId);
        mAccumulated.observe(this, new Observer<BigDecimal>() {
            @Override
            public void onChanged(BigDecimal accumulated) {
                updateAccumulated(accumulated);
            }
        });
        mBalance.observe(this, new Observer<MonthlyBalance>() {
            @Override
            public void onChanged(MonthlyBalance balance) {
                updateBalance(balance);
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
        finish();
    }

    public void onPreviousMonthClick(View v) {
    }

    public void onNextMonthClick(View v) {
    }

    public void onMonthPickerClick(View v) {
    }

    public void onReturnToCurrentMonthClick(View v) {
    }

    private void updateMonth(YearMonth month) {
    }

    private void updateAccumulated(BigDecimal accumulated) {
        TextView accumulatedTV = (TextView) findViewById(R.id.account_history_accumulated_balance);

        try {
            accumulatedTV.setText(CurrencyUtil.format(accumulated, mAccountCurrency));
        } catch(GnomyCurrencyException gce) {
            Log.wtf("AccountHistoryActivity", "updateAccumulated: ", gce);
        }
    }

    private void updateBalance(MonthlyBalance balance) {
        TextView confirmedIncomesTV = (TextView) findViewById(R.id.account_history_confirmed_incomes);
        TextView confirmedExpensesTV = (TextView) findViewById(R.id.account_history_confirmed_expenses);
        TextView confirmedTotalTV = (TextView) findViewById(R.id.account_history_confirmed_total);

        TextView pendingIncomesTV = (TextView) findViewById(R.id.account_history_pending_incomes);
        TextView pendingExpensesTV = (TextView) findViewById(R.id.account_history_pending_expenses);
        TextView pendingTotalTV = (TextView) findViewById(R.id.account_history_pending_total);

        BigDecimal confirmedTotal = balance.getTotalIncomes().subtract(balance.getProjectedExpenses());
        BigDecimal pendingTotal = balance.getProjectedIncomes().subtract(balance.getProjectedExpenses());

        try {

            confirmedIncomesTV.setText(CurrencyUtil.format(balance.getTotalIncomes(), mAccountCurrency));
            confirmedExpensesTV.setText(CurrencyUtil.format(balance.getTotalExpenses(), mAccountCurrency));
            confirmedTotalTV.setText(CurrencyUtil.format(confirmedTotal, mAccountCurrency));

            pendingIncomesTV.setText(CurrencyUtil.format(balance.getTotalIncomes(), mAccountCurrency));
            pendingExpensesTV.setText(CurrencyUtil.format(balance.getTotalIncomes(), mAccountCurrency));
            pendingTotalTV.setText(CurrencyUtil.format(pendingTotal, mAccountCurrency));
        } catch(GnomyCurrencyException gce) {
            Log.wtf("AccountHistoryActivity", "updateAccumulated: ", gce);
        }
    }

    private void setColors() {
        mMainBar.setBackgroundColor(mToolbarBgColor);
        mMainBar.setTitleTextColor(mTitleTextColor);
        mUpArrow.setColorFilter(mTitleTextColor, PorterDuff.Mode.SRC_ATOP);

        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);
        getWindow().setStatusBarColor(ColorUtil.getDarkVariant(mToolbarBgColor));

        mSecondaryBar.setBackgroundColor(mToolbarBgColor);
    }
}
