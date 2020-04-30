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
        mAllAccounts = mRepository.getAll();
    }

    public LiveData<List<Account>> getAll() { return mAllAccounts; }

    public void insert(Account word) { mRepository.insert(word); }
}
