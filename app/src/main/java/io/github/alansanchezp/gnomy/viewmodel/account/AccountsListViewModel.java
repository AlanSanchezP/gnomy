package io.github.alansanchezp.gnomy.viewmodel.account;

import android.app.Application;

import java.time.YearMonth;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import io.github.alansanchezp.gnomy.database.RepositoryBuilder;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.reactivex.Single;

public class AccountsListViewModel extends AndroidViewModel {
    private static final String TAG_TARGET_TO_ARCHIVE = "account-id-to-archive";
    private static final String TAG_TARGET_TO_DELETE = "account-id-to-delete";
    private final AccountRepository mRepository;
    private final SavedStateHandle mState;
    private LiveData<YearMonth> mActiveMonth;
    private LiveData<List<AccountWithAccumulated>> mAccumulatesToday;
    private LiveData<List<AccountWithAccumulated>> mAccumulates;
    private LiveData<List<Account>> mArchivedAccounts;

    public AccountsListViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        mRepository = RepositoryBuilder.getRepository(AccountRepository.class, application);
        mState = savedStateHandle;
    }

    public void bindMonth(LiveData<YearMonth> month) {
        if (mActiveMonth == null) mActiveMonth = month;
    }

    public Map<Integer,AccountWithAccumulated> getAccumulatesMapFromList(List<AccountWithAccumulated> list) {
        return list.stream().collect(Collectors.toMap(
                balance -> balance.account.getId(),
                balance -> balance));
    }

    public LiveData<List<AccountWithAccumulated>> getTodayAccumulatesList() {
        if (mAccumulatesToday == null) {
            mAccumulatesToday = mRepository.getTodayAccumulatesList();
        }
        return mAccumulatesToday;
    }

    public LiveData<List<AccountWithAccumulated>> getAccumulatesListAtMonth() {
        if (mAccumulates == null) {
            mAccumulates = Transformations.switchMap(mActiveMonth, mRepository::getAccumulatesListAtMonth);
        }
        return mAccumulates;
    }

    public  LiveData<List<Account>> getArchivedAccounts() {
        if (mArchivedAccounts == null) {
            mArchivedAccounts = mRepository.getArchivedAccounts();
        }
        return mArchivedAccounts;
    }

    public int getTargetIdToArchive() {
        if (mState.get(TAG_TARGET_TO_ARCHIVE) == null) {
            setTargetIdToArchive(0);
        }
        //noinspection ConstantConditions
        return mState.get(TAG_TARGET_TO_ARCHIVE);
    }

    public void setTargetIdToArchive(int targetId) {
        mState.set(TAG_TARGET_TO_ARCHIVE, targetId);
    }

    public int getTargetIdToDelete() {
        if (mState.get(TAG_TARGET_TO_DELETE) == null) {
            setTargetIdToDelete(0);
        }
        //noinspection ConstantConditions
        return mState.get(TAG_TARGET_TO_DELETE);
    }

    public void setTargetIdToDelete(int targetId) {
        mState.set(TAG_TARGET_TO_DELETE, targetId);
    }

    public Single<Integer> delete(Account account) {
        return mRepository.delete(account);
    }

    public Single<Integer> archive(int accountId) {
        return mRepository.archive(accountId);
    }

    public Single<Integer> restore(int accountId) {
        return mRepository.restore(accountId);
    }

    public Single<Integer> restoreAll() {
        return mRepository.restoreAll();
    }
}
