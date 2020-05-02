package io.github.alansanchezp.gnomy.viewmodel;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;

public class AccountViewModel extends AndroidViewModel {
    private AccountRepository mRepository;

    private LiveData<List<Account>> mAllAccounts;

    public AccountViewModel (Application application) {
        super(application);
        mRepository = new AccountRepository(application);
    }

    public LiveData<List<Account>> getAll() {
        if (mAllAccounts == null) {
            mAllAccounts = mRepository.getAll();
        }
        return mAllAccounts;
    }

    public LiveData<Account> getAccount(int id) {
        return mRepository.find(id);
    }

    public void insert(Account account) {
        mRepository.insert(account);
    }

    public void update(Account oldAccount, Account newAccount) {
        mRepository.update(oldAccount, newAccount);
    }
}
