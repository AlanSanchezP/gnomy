package io.github.alansanchezp.gnomy.viewmodel.transaction;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import io.github.alansanchezp.gnomy.database.RepositoryBuilder;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.category.CategoryRepository;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;
import io.reactivex.Single;

public class AddEditTransactionViewModel extends AndroidViewModel {
    private static final String TAG_IS_AMOUNT_PRISTINE = "AddEditTransactionVM.IsAmountPristine";
    private static final String TAG_IS_CONCEPT_PRISTINE = "AddEditTransactionVM.IsConceptPristine";
    private static final String TAG_USER_SELECTED_CONFIRMED = "AddEditTransactionVM.SelectedConfirmed";
    private static final String TAG_SHOW_MORE_OPTIONS = "AddEditTransactionVM.ShowMoreOptions";
    private final AccountRepository mAccountRepository;
    private final MoneyTransactionRepository mTransactionRepository;
    private final CategoryRepository mCategoryRepository;
    private final SavedStateHandle mState;
    private boolean mAccountsFirstArrival;

    public AddEditTransactionViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        mAccountRepository = RepositoryBuilder.getRepository(AccountRepository.class, application);
        mTransactionRepository = RepositoryBuilder.getRepository(MoneyTransactionRepository.class, application);
        mCategoryRepository = RepositoryBuilder.getRepository(CategoryRepository.class, application);
        mAccountsFirstArrival = false;
        mState = savedStateHandle;
        if (mState.get(TAG_SHOW_MORE_OPTIONS) == null) {
            mState.set(TAG_SHOW_MORE_OPTIONS, false);
        }
    }

    public LiveData<MoneyTransaction> getTransaction(int id) {
        if (id == 0) return null;
        return mTransactionRepository.find(id);
    }

    public LiveData<List<Category>> getCategories() {
        return mCategoryRepository.getAll();
    }

    public LiveData<List<Account>> getAccounts() {
        return mAccountRepository.getAll();
    }

    public Single<Long> insert(MoneyTransaction transaction) {
        return mTransactionRepository.insert(transaction);
    }

    public Single<Integer> update(MoneyTransaction transaction) {
        return mTransactionRepository.update(transaction);
    }

    public boolean transactionAmountIsPristine() {
        if (mState.get(TAG_IS_AMOUNT_PRISTINE) == null) return true;
        //noinspection ConstantConditions
        return mState.get(TAG_IS_AMOUNT_PRISTINE);
    }

    public void notifyTransactionAmountChanged() {
        mState.set(TAG_IS_AMOUNT_PRISTINE, false);
    }

    public boolean transactionConceptIsPristine() {
        if (mState.get(TAG_IS_CONCEPT_PRISTINE) == null) return true;
        //noinspection ConstantConditions
        return mState.get(TAG_IS_CONCEPT_PRISTINE);
    }

    public void notifyTransactionConceptChanged() {
        mState.set(TAG_IS_CONCEPT_PRISTINE, false);
    }

    public void notifyAccountsListFirstArrival() {
        mAccountsFirstArrival = true;
    }

    public boolean accountsListHasArrivedBefore() {
        return mAccountsFirstArrival;
    }

    public boolean getUserSelectedConfirmedStatus() {
        if (mState.get(TAG_USER_SELECTED_CONFIRMED) == null) return true;
        //noinspection ConstantConditions
        return mState.get(TAG_USER_SELECTED_CONFIRMED);
    }

    public void setUserSelectedConfirmedStatus(boolean status) {
        mState.set(TAG_USER_SELECTED_CONFIRMED, status);
    }

    public boolean showMoreOptions() {
        //noinspection ConstantConditions
        return mState.get(TAG_SHOW_MORE_OPTIONS);
    }

    public void toggleShowMoreOptions() {
        if (showMoreOptions())
            mState.set(TAG_SHOW_MORE_OPTIONS, false);
        else
            mState.set(TAG_SHOW_MORE_OPTIONS, true);
    }
}
