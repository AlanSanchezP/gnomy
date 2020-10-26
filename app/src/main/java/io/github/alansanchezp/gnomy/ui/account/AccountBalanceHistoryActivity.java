package io.github.alansanchezp.gnomy.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.time.YearMonth;

import java.math.BigDecimal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.ui.customView.MonthToolbarView;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.viewmodel.account.AccountHistoryViewModel;

public class AccountBalanceHistoryActivity
        extends BackButtonActivity {
    public static final String EXTRA_ID = "account_id";
    // Unlike DetailsActivity, account data is passed through Intent
    // in order to avoid an extra query that we have already performed
    // in what is likely to be the only reasonable path to get to this Activity
    public static final String EXTRA_NAME = "account_name";
    public static final String EXTRA_CURRENCY = "account_currency";
    public static final String EXTRA_BG_COLOR = "bg_color";
    private String mAccountCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int accountId = intent.getIntExtra(EXTRA_ID, 0);
        if (accountId < 1) throw new RuntimeException("No account id was provided.");

        String accountName = intent.getStringExtra(EXTRA_NAME);
        mAccountCurrency = intent.getStringExtra(EXTRA_CURRENCY);
        setThemeColor(intent.getIntExtra(EXTRA_BG_COLOR, 0XFF));

        setTitle(accountName + " " + getString(R.string.account_balance_history_legend));

        AccountHistoryViewModel accountHistoryViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()))
                .get(AccountHistoryViewModel.class);
        MonthToolbarView monthBar = findViewById(R.id.monthtoolbar);

        accountHistoryViewModel.bindMonth(monthBar.getActiveMonth());
        monthBar.tintElements(mThemeColor, mThemeTextColor);

        monthBar.getActiveMonth().observe(this, this::onMonthChanged);

        LiveData<BigDecimal> accumulatedBalance = accountHistoryViewModel.getAccumulatedFromMonth(accountId);
        LiveData<MonthlyBalance> monthBalance = accountHistoryViewModel.getBalanceFromMonth(accountId);
        accumulatedBalance.observe(this, this::onAccumulatedBalanceChanged);
        monthBalance.observe(this, this::onBalanceChanged);
    }

    protected int getLayoutResourceId() {
        return R.layout.activity_account_history;
    }

    protected boolean displayDialogOnBackPress() {
        return false;
    }

    public void onCheckPendingTransactionsClick(View v) {
        // TODO: Implement pending transactions list when Transactions module is ready
        Toast.makeText(this, getString(R.string.wip), Toast.LENGTH_LONG).show();
    }
    // TODO: Insert elements directly to db in tests to avoid exposing these methods as public
    //  This includes all activities

    public void onMonthChanged(YearMonth month) {
        TextView accumulatedTitleTV = findViewById(R.id.account_history_accumulated_balance_label);
        TextView confirmedTitleTV = findViewById(R.id.account_history_confirmed_title);
        TextView pendingTitleTV = findViewById(R.id.account_history_pending_title);
        TextView bottomLegendTV = findViewById(R.id.account_history_bottom_legend);

        String accumulatedTitle;
        String confirmedTitle;
        String pendingTitle;
        String bottomLegend = "* ";

        if (month.equals(DateUtil.now())) {
            accumulatedTitle = getString(R.string.account_current_accumulated_balance);
            confirmedTitle = getString(R.string.account_confirmed_balance);
            pendingTitle = getString(R.string.pending_transactions);
        } else {
            accumulatedTitle = getString(R.string.account_accumulated_balance);
            confirmedTitle = getString(R.string.account_balance_end_of_month);
            pendingTitle = getString(R.string.unresolved_transactions);
        }

        bottomLegend += pendingTitle + " " + getString(R.string.account_balance_not_included_legend);

        accumulatedTitleTV.setText(accumulatedTitle);
        confirmedTitleTV.setText(confirmedTitle);
        pendingTitleTV.setText(pendingTitle);
        bottomLegendTV.setText(bottomLegend);
    }

    private void onAccumulatedBalanceChanged(BigDecimal accumulated) {
        TextView accumulatedTV = findViewById(R.id.account_history_accumulated_balance);

        try {
            accumulatedTV.setText(CurrencyUtil.format(accumulated, mAccountCurrency));
        } catch(GnomyCurrencyException gce) {
            Log.wtf("AccountHistoryActivity", "updateAccumulated: ", gce);
        }
    }

    public void onBalanceChanged(MonthlyBalance balance) {
        TextView confirmedIncomesTV = findViewById(R.id.account_history_confirmed_incomes);
        TextView confirmedExpensesTV = findViewById(R.id.account_history_confirmed_expenses);
        TextView confirmedTotalTV = findViewById(R.id.account_history_confirmed_total);

        TextView pendingIncomesTV = findViewById(R.id.account_history_pending_incomes);
        TextView pendingExpensesTV = findViewById(R.id.account_history_pending_expenses);
        TextView pendingTotalTV = findViewById(R.id.account_history_pending_total);

        Button checkMoreBtn = findViewById(R.id.account_history_check_btn);

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

            switch (confirmedTotal.compareTo(BigDecimal.ZERO)) {
                case -1:
                    confirmedTotalTV.setTextColor(getResources().getColor(R.color.colorExpenses));
                    break;
                case 0:
                    confirmedTotalTV.setTextColor(getResources().getColor(R.color.colorText));
                    break;
                case 1:
                    confirmedTotalTV.setTextColor(getResources().getColor(R.color.colorIncomes));
                    break;
                default:
                    break;
            }
            switch (pendingTotal.compareTo(BigDecimal.ZERO)) {
                case -1:
                    pendingTotalTV.setTextColor(getResources().getColor(R.color.colorExpenses));
                    break;
                case 0:
                    pendingTotalTV.setTextColor(getResources().getColor(R.color.colorText));
                    break;
                case 1:
                    pendingTotalTV.setTextColor(getResources().getColor(R.color.colorIncomes));
                    break;
                default:
                    break;
            }

            checkMoreBtn.setVisibility(View.VISIBLE);
        } else {
            checkMoreBtn.setVisibility(View.GONE);
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
}