package io.github.alansanchezp.gnomy.ui.account;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.time.YearMonth;

import java.math.BigDecimal;

import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.databinding.ActivityAccountHistoryBinding;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.androidUtil.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.viewmodel.account.AccountBalanceHistoryViewModel;

public class AccountBalanceHistoryActivity
        extends BackButtonActivity<ActivityAccountHistoryBinding> {
    public static final String EXTRA_ACCOUNT_ID = "AccountBalanceHistoryActivity.AccountId";
    // Unlike DetailsActivity, account data is passed through Intent
    // in order to avoid an extra query that we have already performed
    // in what is likely to be the only reasonable path to get to this Activity
    public static final String EXTRA_NAME = "AccountBalanceHistoryActivity.AccountName";
    public static final String EXTRA_CURRENCY = "AccountBalanceHistoryActivity.AccountCurrency";
    public static final String EXTRA_ACCOUNT_CREATION_MONTH = "AccountBalanceHistoryActivity.AccountCreationMonth";
    public static final String EXTRA_BG_COLOR = "AccountBalanceHistoryActivity.BgColor";
    private SingleClickViewHolder<Button> mCheckPendingButtonVH;

    public AccountBalanceHistoryActivity() {
        super(null, false, ActivityAccountHistoryBinding::inflate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int accountId = intent.getIntExtra(EXTRA_ACCOUNT_ID, 0);
        if (accountId < 1) throw new RuntimeException("No account id was provided.");

        mCheckPendingButtonVH = new SingleClickViewHolder<>($.accountHistoryCheckBtn);
        mCheckPendingButtonVH.setOnClickListener(this::onCheckPendingTransactionsClick);

        String accountName = intent.getStringExtra(EXTRA_NAME);
        // App will crash if you don't provide this as YearMonth.parse() will
        //  throw a RuntimeException
        setThemeColor(intent.getIntExtra(EXTRA_BG_COLOR, 0XFF));

        setTitle(accountName + " " + getString(R.string.account_balance_history_legend));

        AccountBalanceHistoryViewModel accountBalanceHistoryViewModel = new ViewModelProvider(
                this,
                new SavedStateViewModelFactory(
                        getApplication(),
                        this
                )).get(AccountBalanceHistoryViewModel.class);

        $.monthtoolbar.setViewModel(accountBalanceHistoryViewModel);
        $.monthtoolbar.tintElements(mThemeColor, mThemeTextColor);

        accountBalanceHistoryViewModel.getAccumulatedAtMonth(accountId)
                .observe(this, this::onAccumulatedBalanceChanged);
    }

    @Override
    protected void tintNavigationBar() {
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    public void onCheckPendingTransactionsClick(View v) {
        // TODO: Implement pending transactions list when Transactions module is ready
        Toast.makeText(this, getString(R.string.wip), Toast.LENGTH_LONG).show();
    }

    private void onMonthChanged(YearMonth month) {
        String accumulatedTitle;
        String confirmedTitle = null;
        String pendingTitle;
        String bottomLegend = "* ";

        if (month.isAfter(DateUtil.now())) {
            $.accountHistoryConfirmedTitle.setVisibility(View.GONE);
            $.accountHistoryConfirmedCard.setVisibility(View.GONE);
            accumulatedTitle = getString(R.string.account_accumulated_balance);
            pendingTitle = getString(R.string.pending_transactions);
        } else if (month.equals(DateUtil.now())) {
            $.accountHistoryConfirmedTitle.setVisibility(View.VISIBLE);
            $.accountHistoryConfirmedCard.setVisibility(View.VISIBLE);
            accumulatedTitle = getString(R.string.account_current_accumulated_balance);
            confirmedTitle = getString(R.string.account_confirmed_balance);
            pendingTitle = getString(R.string.pending_transactions);
        } else {
            $.accountHistoryConfirmedTitle.setVisibility(View.VISIBLE);
            $.accountHistoryConfirmedCard.setVisibility(View.VISIBLE);
            accumulatedTitle = getString(R.string.account_accumulated_balance);
            confirmedTitle = getString(R.string.account_balance_end_of_month);
            pendingTitle = getString(R.string.unresolved_transactions);
        }

        bottomLegend += pendingTitle + " " + getString(R.string.account_balance_not_included_legend);

        $.accountHistoryAccumulatedBalanceLabel.setText(accumulatedTitle);
        $.accountHistoryConfirmedTitle.setText(confirmedTitle);
        $.accountHistoryPendingTitle.setText(pendingTitle);
        $.accountHistoryBottomLegend.setText(bottomLegend);
    }

    private void onAccumulatedBalanceChanged(AccountWithAccumulated awa) {
        // TODO: Display some helpful information if month predates account creation
        onMonthChanged(awa.targetMonth);
        String accountCurrency = awa.account.getDefaultCurrency();
        BigDecimal confirmedIncomes = awa.getConfirmedIncomesAtMonth();
        BigDecimal confirmedExpenses = awa.getConfirmedExpensesAtMonth();
        BigDecimal confirmedTotal = confirmedIncomes.subtract(confirmedExpenses);
        BigDecimal pendingIncomes = awa.getPendingIncomesAtMonth();
        BigDecimal pendingExpenses = awa.getPendingExpensesAtMonth();
        BigDecimal pendingTotal = pendingIncomes.subtract(pendingExpenses);

        switch (confirmedTotal.compareTo(BigDecimal.ZERO)) {
            case -1:
                $.accountHistoryConfirmedTotal.setTextColor(getResources().getColor(R.color.colorExpenses));
                break;
            case 0:
                $.accountHistoryConfirmedTotal.setTextColor(getResources().getColor(R.color.colorText));
                break;
            case 1:
                $.accountHistoryConfirmedTotal.setTextColor(getResources().getColor(R.color.colorIncomes));
                break;
            default:
                break;
        }

        if (pendingIncomes.add(pendingExpenses).compareTo(BigDecimal.ZERO) > 0) {
            mCheckPendingButtonVH.onView(this, v -> v.setVisibility(View.VISIBLE));
        } else {
            mCheckPendingButtonVH.onView(this, v -> v.setVisibility(View.GONE));
        }

        switch (pendingTotal.compareTo(BigDecimal.ZERO)) {
            case -1:
                $.accountHistoryPendingTotal.setTextColor(getResources().getColor(R.color.colorExpenses));
                break;
            case 0:
                $.accountHistoryPendingTotal.setTextColor(getResources().getColor(R.color.colorText));
                break;
            case 1:
                $.accountHistoryPendingTotal.setTextColor(getResources().getColor(R.color.colorIncomes));
                break;
            default:
                break;
        }

        try {
            $.accountHistoryAccumulatedBalance.setText(CurrencyUtil.format(awa.getConfirmedAccumulatedBalanceAtMonth(),
                    awa.account.getDefaultCurrency()));

            $.accountHistoryConfirmedIncomes.setText(CurrencyUtil.format(confirmedIncomes, accountCurrency));
            $.accountHistoryConfirmedExpenses.setText(CurrencyUtil.format(confirmedExpenses, accountCurrency));
            $.accountHistoryConfirmedTotal.setText(CurrencyUtil.format(confirmedTotal, accountCurrency));

            $.accountHistoryPendingIncomes.setText(CurrencyUtil.format(pendingIncomes, accountCurrency));
            $.accountHistoryPendingExpenses.setText(CurrencyUtil.format(pendingExpenses, accountCurrency));
            $.accountHistoryPendingTotal.setText(CurrencyUtil.format(pendingTotal, accountCurrency));
        } catch(GnomyCurrencyException gce) {
            Log.wtf("AccountHistoryActivity", "updateAccumulated: ", gce);
        }
    }
}
