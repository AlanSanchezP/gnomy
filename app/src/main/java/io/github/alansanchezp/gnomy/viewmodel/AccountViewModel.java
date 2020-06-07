package io.github.alansanchezp.gnomy.viewmodel;

import android.app.Application;

import org.threeten.bp.YearMonth;

import java.math.BigDecimal;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;

public class AccountViewModel extends AndroidViewModel {
    private AccountRepository mRepository;

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

    public void insert(Account account) {
        mRepository.insert(account);
    }

    public void update(Account account) {
        mRepository.update(account);
    }

    public void archive(Account account) {
        mRepository.archive(account);
    }
}
