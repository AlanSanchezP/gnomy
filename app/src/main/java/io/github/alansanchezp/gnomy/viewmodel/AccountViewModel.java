package io.github.alansanchezp.gnomy.viewmodel;

import android.app.Application;
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
        return mRepository.find(id);
    }

    public void insert(Account account) {
        mRepository.insert(account);
    }

    public void update(Account account) {
        mRepository.update(account);
    }
}
