package io.github.alansanchezp.gnomy.ui.account;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.time.YearMonth;

import java.math.BigDecimal;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.androidUtil.GnomyFragmentFactory;
import io.github.alansanchezp.gnomy.data.account.Account;
import io.github.alansanchezp.gnomy.data.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.data.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.data.transaction.TransactionDisplayData;
import io.github.alansanchezp.gnomy.databinding.ActivityAccountHistoryBinding;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.ui.transaction.AddEditTransactionActivity;
import io.github.alansanchezp.gnomy.ui.transaction.UnresolvedTransactionsDialogFragment;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.androidUtil.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.viewmodel.account.AccountBalanceHistoryViewModel;

public class AccountBalanceHistoryActivity
        extends BackButtonActivity<ActivityAccountHistoryBinding>
        implements UnresolvedTransactionsDialogFragment.UnresolvedTransactionsDialogInterface {
    public static final String EXTRA_ACCOUNT_ID = "AccountBalanceHistoryActivity.AccountId";
    public static final String TAG_UNRESOLVED_TRANSACTIONS_DIALOG = "AccountBalanceHistoryActivity.UnresolvedTransactionsDialog";
    private SingleClickViewHolder<Button> mCheckPendingButtonVH;
    private AccountBalanceHistoryViewModel mViewModel;
    private int mAccountId;
    private Account mAccount = null;

    public AccountBalanceHistoryActivity() {
        super(null, false, ActivityAccountHistoryBinding::inflate);
    }

    @Override
    protected GnomyFragmentFactory getFragmentFactory() {
        return super.getFragmentFactory()
                .addMapElement(UnresolvedTransactionsDialogFragment.class, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mAccountId = intent.getIntExtra(EXTRA_ACCOUNT_ID, 0);
        if (mAccountId < 1) throw new RuntimeException("No account id was provided.");

        mCheckPendingButtonVH = new SingleClickViewHolder<>($.accountHistoryCheckBtn);
        mCheckPendingButtonVH.setOnClickListener(this::onCheckPendingTransactionsClick);

        mViewModel = new ViewModelProvider(
                this,
                new SavedStateViewModelFactory(
                        getApplication(),
                        this
                )).get(AccountBalanceHistoryViewModel.class);

        $.monthToolbar.setViewModel(mViewModel);

        mViewModel.getAccumulatedAtMonth(mAccountId)
                .observe(this, this::onAccumulatedBalanceChanged);
    }

    @Override
    protected void tintNavigationBar() {
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    public void onCheckPendingTransactionsClick(View v) {
        UnresolvedTransactionsDialogFragment dialog = new UnresolvedTransactionsDialogFragment(this);
        Bundle args = new Bundle();
        args.putInt(UnresolvedTransactionsDialogFragment.ARG_TARGET_ACCOUNT, mAccountId);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), TAG_UNRESOLVED_TRANSACTIONS_DIALOG);
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
        setOrIgnoreAccount(awa.account);
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

    private void setOrIgnoreAccount(Account account) {
        if (mAccount != null) return;
        mAccount = account;
        setThemeColor(account.getBackgroundColor());
        setTitle(account.getName() + " " + getString(R.string.account_balance_history_legend));
        $.monthToolbar.tintElements(mThemeColor, mThemeTextColor);
    }

    @Override
    public void onUnresolvedTransactionClick(@NonNull MoneyTransaction transaction) {
        int transactionId = transaction.getId();
        int transactionType = transaction.getType();
        Intent updateTransactionIntent = new Intent(this, AddEditTransactionActivity.class);
        updateTransactionIntent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, transactionId);
        updateTransactionIntent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, transactionType);
        startActivity(updateTransactionIntent);
    }

    @Override
    public LiveData<List<TransactionDisplayData>> getUnresolvedTransactionsList(int accountId) {
        return mViewModel.getPendingTransactionsFromMonth(accountId);
    }
}
