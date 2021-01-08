package io.github.alansanchezp.gnomy.viewmodel.account;

import android.app.Application;

import java.time.OffsetDateTime;
import java.time.YearMonth;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import io.github.alansanchezp.gnomy.database.RepositoryBuilder;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.database.category.CategoryRepository;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionFilters;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;
import io.github.alansanchezp.gnomy.database.transaction.TransactionDisplayData;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.reactivex.Single;

public class AccountsListViewModel extends AndroidViewModel {
    private static final String TAG_TARGET_TO_ARCHIVE = "AccountsListVM.IdToArchive";
    private static final String TAG_TARGET_TO_DELETE = "AccountsListVM.IdToDelete";
    private final AccountRepository mAccountRepository;
    private final MoneyTransactionRepository mTransactionRepository;
    private final SavedStateHandle mState;
    private LiveData<YearMonth> mActiveMonth;
    private LiveData<List<AccountWithAccumulated>> mAccumulatesToday;
    private LiveData<List<AccountWithAccumulated>> mAccumulates;
    private LiveData<List<Account>> mArchivedAccounts;

    public AccountsListViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        mAccountRepository = RepositoryBuilder.getRepository(AccountRepository.class, application);
        mTransactionRepository = RepositoryBuilder.getRepository(MoneyTransactionRepository.class, application);
        mState = savedStateHandle;
    }

    /**
     * Binds a {@link LiveData} object that emits {@link YearMonth}
     * values, and that will be used for database queries.
     *
     * !!! IMPORTANT: Use this method BEFORE trying to retrieve
     * any LiveData from this class !!!
     *
     * @param month LiveData object.
     */
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
            mAccumulatesToday = mAccountRepository.getTodayAccumulatesList();
        }
        return mAccumulatesToday;
    }

    public LiveData<List<AccountWithAccumulated>> getAccumulatesListAtMonth() {
        if (mAccumulates == null) {
            mAccumulates = Transformations.switchMap(mActiveMonth, mAccountRepository::getAccumulatesListAtMonth);
        }
        return mAccumulates;
    }

    public  LiveData<List<Account>> getArchivedAccounts() {
        if (mArchivedAccounts == null) {
            mArchivedAccounts = mAccountRepository.getArchivedAccounts();
        }
        return mArchivedAccounts;
    }

    /**
     * Returns a {@link LiveData} that will emit the
     * list of pending transactions for the given account.
     * This list will include any pending transaction that has happened
     * before and during the active month (from {@link #mActiveMonth}).
     * An exception for this occurs if the active month matches
     * the current one (as returned by {@link DateUtil#now()}), in which case
     * only pending transactions BEFORE it will be returned.
     *
     *
     * @param accountId     Account id.
     * @return              LiveData object.
     */
    public LiveData<List<TransactionDisplayData>> getUnresolvedTransactions(int accountId) {
        MoneyTransactionFilters filters = new MoneyTransactionFilters();
        OffsetDateTime[] monthBoundaries = DateUtil.getMonthBoundaries(
                Objects.requireNonNull(mActiveMonth.getValue()));
        if (mActiveMonth.getValue().equals(DateUtil.now()))
            filters.setEndDate(monthBoundaries[0]);
        else
            filters.setEndDate(monthBoundaries[1]);

        filters.setAccountId(accountId);
        filters.setTransactionStatus(MoneyTransactionFilters.UNCONFIRMED_STATUS);
        return mTransactionRepository.getByFilters(filters);
    }

    public Single<Integer> delete(int accountId) {
        return mAccountRepository.delete(accountId);
    }

    public Single<Integer> archive(int accountId) {
        return mAccountRepository.archive(accountId);
    }

    public Single<Integer> restore(int accountId) {
        return mAccountRepository.restore(accountId);
    }

    public Single<Integer> restoreAll() {
        return mAccountRepository.restoreAll();
    }


    /**** Helper methods for dialogs to confirm operations over some account. ****/

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
}
