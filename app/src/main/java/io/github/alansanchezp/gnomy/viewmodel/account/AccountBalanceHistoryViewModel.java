package io.github.alansanchezp.gnomy.viewmodel.account;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import io.github.alansanchezp.gnomy.database.RepositoryBuilder;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionFilters;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;
import io.github.alansanchezp.gnomy.database.transaction.TransactionDisplayData;
import io.github.alansanchezp.gnomy.viewmodel.customView.MonthToolbarViewModel;

public class AccountBalanceHistoryViewModel
        extends MonthToolbarViewModel {
    private final AccountRepository mRepository;
    private final MoneyTransactionRepository mTransactionsRepository;
    private LiveData<AccountWithAccumulated> mAccumulated;

    public AccountBalanceHistoryViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application, savedStateHandle);
        mRepository = RepositoryBuilder.getRepository(AccountRepository.class, application);
        mTransactionsRepository = RepositoryBuilder.getRepository(MoneyTransactionRepository.class, application);
    }

    public LiveData<AccountWithAccumulated> getAccumulatedAtMonth(final int accountId) {
        if (mAccumulated == null) {
            mAccumulated = Transformations.switchMap(activeMonth,
                    month -> mRepository.getAccumulatedAtMonth(accountId, month));
        }
        return mAccumulated;
    }

    public LiveData<List<TransactionDisplayData>> getPendingTransactionsFromMonth(int accountId) {
        MoneyTransactionFilters filters = new MoneyTransactionFilters();
        filters.setMonth(activeMonth.getValue());
        filters.setAccountId(accountId);
        filters.setTransactionStatus(MoneyTransactionFilters.UNCONFIRMED_STATUS);
        return mTransactionsRepository.getByFilters(filters);
    }
}