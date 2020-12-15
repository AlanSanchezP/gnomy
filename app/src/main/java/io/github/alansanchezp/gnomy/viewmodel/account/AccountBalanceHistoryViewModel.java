package io.github.alansanchezp.gnomy.viewmodel.account;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import io.github.alansanchezp.gnomy.database.RepositoryBuilder;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.viewmodel.customView.MonthToolbarViewModel;

public class AccountBalanceHistoryViewModel
        extends MonthToolbarViewModel {
    private final AccountRepository mRepository;
    private LiveData<AccountWithAccumulated> mAccumulated;

    public AccountBalanceHistoryViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application, savedStateHandle);
        mRepository = RepositoryBuilder.getRepository(AccountRepository.class, application);
    }

    public LiveData<AccountWithAccumulated> getAccumulatedAtMonth(final int accountId) {
        if (mAccumulated == null) {
            mAccumulated = Transformations.switchMap(activeMonth,
                    month -> mRepository.getAccumulatedAtMonth(accountId, month));
        }
        return mAccumulated;
    }
}