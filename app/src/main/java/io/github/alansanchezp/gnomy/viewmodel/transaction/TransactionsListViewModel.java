package io.github.alansanchezp.gnomy.viewmodel.transaction;

import android.app.Application;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import io.github.alansanchezp.gnomy.database.RepositoryBuilder;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.category.CategoryRepository;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionFilters;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;
import io.github.alansanchezp.gnomy.database.transaction.TransactionDisplayData;
import io.reactivex.Single;

public class TransactionsListViewModel extends AndroidViewModel {
    private static final String TAG_FILTERS = "TransactionsListViewModel.TransactionFilters";
    private static final String TAG_TARGET_TO_DELETE = "TransactionsListVM.TargetToDelete";
    private final SavedStateHandle mSavedState;
    private final MoneyTransactionRepository mTransactionRepository;
    private final AccountRepository mAccountRepository;
    private final CategoryRepository mCategoryRepository;
    private LiveData<YearMonth> mActiveMonth;
    private LiveData<List<TransactionDisplayData>> mTransactions;
    private LiveData<List<Account>> mAccounts;
    private LiveData<List<Category>> mCategories;
    private MutableLiveData<MoneyTransactionFilters> mFilters;

    public TransactionsListViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        mSavedState = savedStateHandle;
        mTransactionRepository = RepositoryBuilder.getRepository(MoneyTransactionRepository.class, application);
        mAccountRepository = RepositoryBuilder.getRepository(AccountRepository.class, application);
        mCategoryRepository = RepositoryBuilder.getRepository(CategoryRepository.class, application);
        if (mSavedState.get(TAG_FILTERS) == null) {
            mSavedState.set(TAG_FILTERS, new MoneyTransactionFilters());
        }
    }

    public void bindMonth(LiveData<YearMonth> month) {
        if (mActiveMonth == null) {
            mActiveMonth = month;
            mFilters = (MutableLiveData<MoneyTransactionFilters>) Transformations.map(month, this::bindMonthToFilters);
            mTransactions = Transformations.switchMap(mFilters, mTransactionRepository::getByFilters);
        }
    }

    private MoneyTransactionFilters bindMonthToFilters(YearMonth month) {
        // A change in month resets all filters but transaction type
        MoneyTransactionFilters filters = mSavedState.get(TAG_FILTERS);
        int type = Objects.requireNonNull(filters).getTransactionType();
        filters = new MoneyTransactionFilters();
        filters.setTransactionType(type);
        filters.setMonth(month);
        mSavedState.set(TAG_FILTERS, filters);
        return filters;
    }

    public LiveData<List<TransactionDisplayData>> getTransactionsList() {
        return mTransactions;
    }

    public int getTargetIdToDelete() {
        if (mSavedState.get(TAG_TARGET_TO_DELETE) == null) {
            setTargetIdToDelete(0);
        }
        //noinspection ConstantConditions
        return mSavedState.get(TAG_TARGET_TO_DELETE);
    }

    public void setTargetIdToDelete(int transactionId) {
        mSavedState.set(TAG_TARGET_TO_DELETE, transactionId);
    }

    public Single<Integer> delete(int transactionId) {
        return mTransactionRepository.delete(transactionId);
    }

    public MoneyTransactionFilters getCurrentFilters() {
        return mFilters.getValue();
    }

    public LiveData<MoneyTransactionFilters> getFilters() {
        return mFilters;
    }

    public void clearFilters() {
        mFilters.postValue(bindMonthToFilters(mActiveMonth.getValue()));
    }

    public void applyFilters (MoneyTransactionFilters filters) {
        mSavedState.set(TAG_FILTERS, filters);
        mFilters.postValue(filters);
    }

    public void setTransactionsType(int type) {
        MoneyTransactionFilters newFilters = mFilters.getValue();
        Objects.requireNonNull(newFilters).setTransactionType(type);
        applyFilters(newFilters);
    }

    public LiveData<List<Account>> getAccounts() {
        if (mAccounts == null) {
            mAccounts = mAccountRepository.getAll();
        }
        return mAccounts;
    }

    public LiveData<List<Category>> getCategories() {
        if (mCategories == null) {
            mCategories = mCategoryRepository.getAll();
        }
        return mCategories;
    }
}
