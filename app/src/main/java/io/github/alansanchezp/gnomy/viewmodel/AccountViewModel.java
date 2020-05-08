package io.github.alansanchezp.gnomy.viewmodel;

import android.app.Application;

import org.threeten.bp.YearMonth;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;

public class AccountViewModel extends AndroidViewModel {
    private AccountRepository mRepository;

    private LiveData<List<Account>> mAllAccounts;
    private LiveData<List<AccountWithBalance>> mBalancesToDisplay;
    private LiveData<List<Account>> mArchivedAccounts;

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

    public LiveData<List<AccountWithBalance>> getAllFromMonth(YearMonth month) {
        // TODO: Implement filters
        // https://stackoverflow.com/questions/48769812/best-practice-runtime-filters-with-room-and-livedata
        if (mBalancesToDisplay == null) {
            mBalancesToDisplay = mRepository.getAllFromMonth(month);
        }
        return mBalancesToDisplay;
    }

    public  LiveData<List<Account>> getArchivedAccounts() {
        if (mArchivedAccounts == null) {
            mArchivedAccounts = mRepository.getArchivedAccounts();
        }
        return mArchivedAccounts;
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

    public void delete(Account account) {
        mRepository.delete(account);
    }

    public void archive(Account account) {
        mRepository.archive(account);
    }

    public void restore(Account account) {
        mRepository.restore(account);
    }

    public void restoreAll() {
        mRepository.restoreAll();
    }
}
