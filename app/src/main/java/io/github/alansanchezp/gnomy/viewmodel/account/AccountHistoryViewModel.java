package io.github.alansanchezp.gnomy.viewmodel.account;

import android.app.Application;

import java.time.YearMonth;

import java.math.BigDecimal;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;

public class AccountHistoryViewModel extends AndroidViewModel {
    private AccountRepository mRepository;
    private LiveData<MonthlyBalance> mBalance;
    private LiveData<BigDecimal> mAccumulated;
    public LiveData<YearMonth> selectedMonth;

    public AccountHistoryViewModel (Application application) {
        super(application);
        mRepository = new AccountRepository(application);
    }

    public void setMonthLiveData(LiveData<YearMonth> monthLiveData) {
        selectedMonth = monthLiveData;
    }

    public LiveData<BigDecimal> getAccumulatedFromMonth(final int accountId) {
        if (mAccumulated == null) {
            mAccumulated = Transformations.switchMap(selectedMonth, new Function<YearMonth, LiveData<BigDecimal>>() {
                @Override
                public LiveData<BigDecimal> apply(YearMonth month) {
                    return mRepository.getAccumulatedFromMonth(accountId, month);
                }
            });
        }
        return mAccumulated;
    }

    public LiveData<MonthlyBalance> getBalanceFromMonth(final int accountId) {
        if (mBalance == null) {
            mBalance = Transformations.switchMap(selectedMonth, new Function<YearMonth, LiveData<MonthlyBalance>>() {
                @Override
                public LiveData<MonthlyBalance> apply(YearMonth month) {
                    return mRepository.getBalanceFromMonth(accountId, month);
                }
            });
        }
        return mBalance;
    }
}