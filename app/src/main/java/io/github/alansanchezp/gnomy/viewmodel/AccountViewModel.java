package io.github.alansanchezp.gnomy.viewmodel;

import android.app.Application;

import org.threeten.bp.YearMonth;

import java.util.List;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;

public class AccountViewModel extends AndroidViewModel {
    private AccountRepository mRepository;
    private LiveData<YearMonth> mMonthFilter;
    private LiveData<List<Account>> mAllAccounts;
    private LiveData<List<AccountWithBalance>> mBalancesToDisplay;
    private LiveData<List<Account>> mArchivedAccounts;

    public AccountViewModel (Application application) {
        super(application);
        mRepository = new AccountRepository(application);
    }

    public boolean shouldInitMonthFilter() {
        return mMonthFilter == null;
    }

    public void initMonthFilter(LiveData<YearMonth> monthFilter) {
        if (mMonthFilter == null) mMonthFilter = monthFilter;
    }

    public LiveData<List<Account>> getAll() {
        if (mAllAccounts == null) {
            mAllAccounts = mRepository.getAll();
        }
        return mAllAccounts;
    }

    // TODO: Return individual list for latest accounts

    public LiveData<List<AccountWithBalance>> getBalances() {
        if (mBalancesToDisplay == null) {
            mBalancesToDisplay = Transformations.switchMap(mMonthFilter, new Function<YearMonth, LiveData<List<AccountWithBalance>>> () {
                @Override
                public LiveData<List<AccountWithBalance>> apply(YearMonth month) {
                    return mRepository.getAllFromMonth(month);
                }
            });
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
