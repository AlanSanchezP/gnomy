package io.github.alansanchezp.gnomy.viewmodel.account;

import android.app.Application;

import java.math.BigDecimal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.viewmodel.customView.MonthToolbarViewModel;

public class AccountBalanceHistoryViewModel
        extends MonthToolbarViewModel {
    private AccountRepository mRepository;
    private LiveData<MonthlyBalance> mBalance;
    private LiveData<BigDecimal> mAccumulated;

    public AccountBalanceHistoryViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application, savedStateHandle);
        mRepository = new AccountRepository(application);
    }

    public LiveData<BigDecimal> getAccumulatedFromMonth(final int accountId) {
        if (mAccumulated == null) {
            mAccumulated = Transformations.switchMap(activeMonth,
                    month -> mRepository.getAccumulatedFromMonth(accountId, month));
        }
        return mAccumulated;
    }

    public LiveData<MonthlyBalance> getBalanceFromMonth(final int accountId) {
        if (mBalance == null) {
            mBalance = Transformations.switchMap(activeMonth,
                    month -> mRepository.getBalanceFromMonth(accountId, month));
        }
        return mBalance;
    }
}