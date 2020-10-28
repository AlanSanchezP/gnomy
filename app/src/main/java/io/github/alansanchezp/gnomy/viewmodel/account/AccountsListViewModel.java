package io.github.alansanchezp.gnomy.viewmodel.account;

import android.app.Application;

import java.time.YearMonth;

import java.util.List;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;

public class AccountsListViewModel extends AndroidViewModel {
    private AccountRepository mRepository;
    private LiveData<YearMonth> mActiveMonth;
    private LiveData<List<Account>> mAllAccounts;
    private LiveData<List<AccountWithBalance>> mBalancesToDisplay;
    private LiveData<List<Account>> mArchivedAccounts;

    public AccountsListViewModel (Application application) {
        super(application);
        mRepository = new AccountRepository(application);
    }

    public void bindMonth(LiveData<YearMonth> month) {
        if (mActiveMonth == null) mActiveMonth = month;
    }

    public LiveData<List<Account>> getAll() {
        if (mAllAccounts == null) {
            mAllAccounts = mRepository.getAll();
        }
        return mAllAccounts;
    }

    public LiveData<List<AccountWithBalance>> getBalances() {
        if (mBalancesToDisplay == null) {
            mBalancesToDisplay = Transformations.switchMap(mActiveMonth,
                    month -> mRepository.getAllFromMonth(month));
        }
        return mBalancesToDisplay;
    }

    public  LiveData<List<Account>> getArchivedAccounts() {
        if (mArchivedAccounts == null) {
            mArchivedAccounts = mRepository.getArchivedAccounts();
        }
        return mArchivedAccounts;
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
