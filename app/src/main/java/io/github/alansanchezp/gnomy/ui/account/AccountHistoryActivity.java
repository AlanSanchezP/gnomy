package io.github.alansanchezp.gnomy.ui.account;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;

import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.util.Calendar;

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
        mAccountHistoryViewModel.setMonth(mAccountHistoryViewModel.getMonth().minusMonths(1));
    }

    public void onNextMonthClick(View v) {
        mAccountHistoryViewModel.setMonth(mAccountHistoryViewModel.getMonth().plusMonths(1));
    }

    public void onMonthPickerClick(View v) {
        Calendar calendar = Calendar.getInstance();
        int yearSelected = mAccountHistoryViewModel.getMonth().getYear();
        int monthSelected = mAccountHistoryViewModel.getMonth().getMonthValue();

        calendar.clear();
        calendar.set(YearMonth.now().getYear(), YearMonth.now().getMonthValue()-1, 1);
        long maxDate = calendar.getTimeInMillis();

        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(monthSelected-1, yearSelected, 0, maxDate);

        dialogFragment.setOnDateSetListener(new MonthYearPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear) {
                mAccountHistoryViewModel.setMonth(YearMonth.of(year, monthOfYear+1));
            }
        });

        dialogFragment.show(getSupportFragmentManager(), null);
    }

    public void onReturnToCurrentMonthClick(View v) {
        mAccountHistoryViewModel.setMonth(YearMonth.now());
    }

    private void updateMonth(YearMonth month) {
        // TODO: Modularize this to avoid DRY of month management
        TextView monthTextView = (TextView) findViewById(R.id.month_name_view);
        ImageButton nextMonthBtn = (ImageButton) findViewById(R.id.next_month_btn);
        ImageButton calendarBtn =  (ImageButton) findViewById(R.id.select_total_month);

        TextView accumulatedTitleTV = (TextView) findViewById(R.id.account_history_accumulated_balance_label);
        TextView confirmedTitleTV = (TextView) findViewById(R.id.account_history_confirmed_title);
        TextView pendingTitleTV = (TextView) findViewById(R.id.account_history_pending_title);
        TextView bottomLegendTV = (TextView) findViewById(R.id.account_history_bottom_legend);

        String formatterPattern;
        String monthString;
        String accumulatedTitle;
        String confirmedTitle;
        String pendingTitle;
        String bottomLegend = "* ";

        if (month.getYear() == YearMonth.now().getYear()) {
            formatterPattern = "MMMM";
        } else {
            formatterPattern = "MMMM yyyy";
        }


        monthString = month.format(DateTimeFormatter.ofPattern(formatterPattern));
        monthString = monthString.substring(0, 1).toUpperCase()
                + monthString.substring(1);

        if (month.equals(YearMonth.now())) {
            nextMonthBtn.setVisibility(View.INVISIBLE);
            calendarBtn.setVisibility(View.GONE);

            accumulatedTitle = getString(R.string.account_current_accumulated_balance);
            confirmedTitle = getString(R.string.account_confirmed_balance);
            pendingTitle = getString(R.string.pending_transactions);
        } else {
            nextMonthBtn.setVisibility(View.VISIBLE);
            calendarBtn.setVisibility(View.VISIBLE);

            accumulatedTitle = getString(R.string.account_accumulated_balance);
            confirmedTitle = getString(R.string.account_balance_end_of_month);
            pendingTitle = getString(R.string.unresolved_transactions);
        }

        bottomLegend += pendingTitle + " " + getString(R.string.account_balance_not_included_legend);;

        accumulatedTitleTV.setText(accumulatedTitle);
        confirmedTitleTV.setText(confirmedTitle);
        pendingTitleTV.setText(pendingTitle);
        bottomLegendTV.setText(bottomLegend);

        monthTextView.setText(monthString);
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

        BigDecimal confirmedIncomes = null;
        BigDecimal confirmedExpenses = null;
        BigDecimal confirmedTotal = null;
        BigDecimal pendingIncomes = null;
        BigDecimal pendingExpenses = null;
        BigDecimal pendingTotal = null;

        if (balance != null) {
            confirmedIncomes = balance.getTotalIncomes();
            confirmedExpenses = balance.getTotalExpenses();
            confirmedTotal = confirmedIncomes.subtract(confirmedExpenses);
            pendingIncomes = balance.getProjectedIncomes();
            pendingExpenses = balance.getProjectedExpenses();
            pendingTotal = pendingIncomes.subtract(pendingExpenses);
        }

        try {
            confirmedIncomesTV.setText(CurrencyUtil.format(confirmedIncomes, mAccountCurrency));
            confirmedExpensesTV.setText(CurrencyUtil.format(confirmedExpenses, mAccountCurrency));
            confirmedTotalTV.setText(CurrencyUtil.format(confirmedTotal, mAccountCurrency));

            pendingIncomesTV.setText(CurrencyUtil.format(pendingIncomes, mAccountCurrency));
            pendingExpensesTV.setText(CurrencyUtil.format(pendingExpenses, mAccountCurrency));
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
