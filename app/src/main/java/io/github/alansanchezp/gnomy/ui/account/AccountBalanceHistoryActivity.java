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
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.ui.customView.MonthToolbarView;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.util.android.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.viewmodel.account.AccountBalanceHistoryViewModel;

public class AccountBalanceHistoryActivity
        extends BackButtonActivity {
    public static final String EXTRA_ACCOUNT_ID = "AccountBalanceHistoryActivity.AccountId";
    // Unlike DetailsActivity, account data is passed through Intent
    // in order to avoid an extra query that we have already performed
    // in what is likely to be the only reasonable path to get to this Activity
    public static final String EXTRA_NAME = "AccountBalanceHistoryActivity.AccountName";
    public static final String EXTRA_CURRENCY = "AccountBalanceHistoryActivity.AccountCurrency";
    public static final String EXTRA_ACCOUNT_CREATION_MONTH = "AccountBalanceHistoryActivity.AccountCreationMonth";
    public static final String EXTRA_BG_COLOR = "AccountBalanceHistoryActivity.BgColor";
    private SingleClickViewHolder<Button> mCheckPendingButtonVH;
    private String mAccountCurrency;
    private YearMonth mAccountCreationMonth;
    private boolean mAfterAccountCreation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int accountId = intent.getIntExtra(EXTRA_ACCOUNT_ID, 0);
        if (accountId < 1) throw new RuntimeException("No account id was provided.");

        mCheckPendingButtonVH = new SingleClickViewHolder<>(findViewById(R.id.account_history_check_btn));
        mCheckPendingButtonVH.setOnClickListener(this::onCheckPendingTransactionsClick);

        String accountName = intent.getStringExtra(EXTRA_NAME);
        mAccountCurrency = intent.getStringExtra(EXTRA_CURRENCY);
        // App will crash if you don't provide this as YearMonth.parse() will
        //  throw a RuntimeException
        mAccountCreationMonth = YearMonth.parse(intent.getStringExtra(EXTRA_ACCOUNT_CREATION_MONTH));
        setThemeColor(intent.getIntExtra(EXTRA_BG_COLOR, 0XFF));

        setTitle(accountName + " " + getString(R.string.account_balance_history_legend));

        AccountBalanceHistoryViewModel accountBalanceHistoryViewModel = new ViewModelProvider(
                this,
                new SavedStateViewModelFactory(
                        getApplication(),
                        this
                )).get(AccountBalanceHistoryViewModel.class);
        MonthToolbarView monthBar = findViewById(R.id.monthtoolbar);

        monthBar.setViewModel(accountBalanceHistoryViewModel);
        monthBar.tintElements(mThemeColor, mThemeTextColor);

        accountBalanceHistoryViewModel.activeMonth.observe(this, this::onMonthChanged);

        LiveData<BigDecimal> accumulatedBalance = accountBalanceHistoryViewModel.getAccumulatedFromMonth(accountId);
        LiveData<MonthlyBalance> monthBalance = accountBalanceHistoryViewModel.getBalanceFromMonth(accountId);
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

    public void onMonthChanged(YearMonth month) {
        // TODO: Display some helpful information if month predates account creation
        //  current behavior just shows '---' on everything
        mAfterAccountCreation = !month.isBefore(mAccountCreationMonth);

        TextView accumulatedTitleTV = findViewById(R.id.account_history_accumulated_balance_label);
        TextView confirmedTitleTV = findViewById(R.id.account_history_confirmed_title);
        View confirmedTransactionsCard = findViewById(R.id.account_history_confirmed_card);
        TextView pendingTitleTV = findViewById(R.id.account_history_pending_title);
        TextView bottomLegendTV = findViewById(R.id.account_history_bottom_legend);

        String accumulatedTitle;
        String confirmedTitle = null;
        String pendingTitle;
        String bottomLegend = "* ";

        if (month.isAfter(DateUtil.now())) {
            confirmedTitleTV.setVisibility(View.GONE);
            confirmedTransactionsCard.setVisibility(View.GONE);
            accumulatedTitle = getString(R.string.account_accumulated_balance);
            pendingTitle = getString(R.string.pending_transactions);
        } else if (month.equals(DateUtil.now())) {
            confirmedTitleTV.setVisibility(View.VISIBLE);
            confirmedTransactionsCard.setVisibility(View.VISIBLE);
            accumulatedTitle = getString(R.string.account_current_accumulated_balance);
            confirmedTitle = getString(R.string.account_confirmed_balance);
            pendingTitle = getString(R.string.pending_transactions);
        } else {
            confirmedTitleTV.setVisibility(View.VISIBLE);
            confirmedTransactionsCard.setVisibility(View.VISIBLE);
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
        // Not sure if this code will be reached outside of tests
        //  but who knows
        if (mAfterAccountCreation && accumulated == null) {
            accumulated = new BigDecimal("0");
        }

        try {
            accumulatedTV.setText(CurrencyUtil.format(accumulated, mAccountCurrency));
        } catch(GnomyCurrencyException gce) {
            Log.wtf("AccountHistoryActivity", "updateAccumulated: ", gce);
        }
    }

    private void onBalanceChanged(MonthlyBalance balance) {
        TextView confirmedIncomesTV = findViewById(R.id.account_history_confirmed_incomes);
        TextView confirmedExpensesTV = findViewById(R.id.account_history_confirmed_expenses);
        TextView confirmedTotalTV = findViewById(R.id.account_history_confirmed_total);

        TextView pendingIncomesTV = findViewById(R.id.account_history_pending_incomes);
        TextView pendingExpensesTV = findViewById(R.id.account_history_pending_expenses);
        TextView pendingTotalTV = findViewById(R.id.account_history_pending_total);

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
            mCheckPendingButtonVH.onView(v -> v.setVisibility(View.VISIBLE));
            switch (pendingTotal.compareTo(BigDecimal.ZERO)) {
                case -1:
                    pendingTotalTV.setTextColor(getResources().getColor(R.color.colorExpenses));
                    break;
                case 0:
                    pendingTotalTV.setTextColor(getResources().getColor(R.color.colorText));
                    mCheckPendingButtonVH.onView(v -> v.setVisibility(View.GONE));
                    break;
                case 1:
                    pendingTotalTV.setTextColor(getResources().getColor(R.color.colorIncomes));
                    break;
                default:
                    break;
            }
        } else {
            mCheckPendingButtonVH.onView(v -> v.setVisibility(View.GONE));

            if (mAfterAccountCreation) {
                confirmedIncomes = new BigDecimal("0");
                confirmedExpenses = confirmedIncomes;
                confirmedTotal = confirmedIncomes;
                pendingIncomes = confirmedIncomes;
                pendingExpenses = confirmedIncomes;
                pendingTotal = confirmedIncomes;
            }
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
