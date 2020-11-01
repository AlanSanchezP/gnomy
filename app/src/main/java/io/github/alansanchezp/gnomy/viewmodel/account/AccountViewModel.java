package io.github.alansanchezp.gnomy.viewmodel.account;

import android.app.Application;

import java.time.YearMonth;

import java.math.BigDecimal;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.reactivex.Single;

public class AccountViewModel extends AndroidViewModel {
    private final AccountRepository mRepository;

    public AccountViewModel (Application application) {
        super(application);
        mRepository = new AccountRepository(application);
    }

    public LiveData<Account> getAccount(int id) {
        return mRepository.getAccount(id);
    }

    public LiveData<BigDecimal> getAccumulatedFromMonth(int accountId, YearMonth month) {
        return mRepository.getAccumulatedFromMonth(accountId, month);
    }

    public Single<Integer> archive(int accountId) {
        return mRepository.archive(accountId);
    }
}
