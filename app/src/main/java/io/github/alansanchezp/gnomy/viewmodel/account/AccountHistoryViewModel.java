package io.github.alansanchezp.gnomy.viewmodel.account;

import android.app.Application;

import java.time.YearMonth;

import java.math.BigDecimal;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;

public class AccountHistoryViewModel extends AndroidViewModel {
    private AccountRepository mRepository;
    private LiveData<MonthlyBalance> mBalance;
    private LiveData<BigDecimal> mAccumulated;
    private LiveData<YearMonth> mActiveMonth;

    public AccountHistoryViewModel (Application application) {
        super(application);
        mRepository = new AccountRepository(application);
    }

    public void bindMonth(LiveData<YearMonth> month) {
        mActiveMonth = month;
    }

    public LiveData<BigDecimal> getAccumulatedFromMonth(final int accountId) {
        if (mAccumulated == null) {
            mAccumulated = Transformations.switchMap(mActiveMonth,
                    month -> mRepository.getAccumulatedFromMonth(accountId, month));
        }
        return mAccumulated;
    }

    public LiveData<MonthlyBalance> getBalanceFromMonth(final int accountId) {
        if (mBalance == null) {
            mBalance = Transformations.switchMap(mActiveMonth,
                    month -> mRepository.getBalanceFromMonth(accountId, month));
        }
        return mBalance;
    }
}