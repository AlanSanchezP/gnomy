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
    private static final String TAG_EXPECTING_NEW_ACCOUNT = "AddEditTransactionVM.ExpectingNewAccount";
    private static final String TAG_EXPECTING_NEW_CATEGORY = "AddEditTransactionVM.ExpectingNewCategory";
    private static final String TAG_USER_SELECTED_ACCOUNT = "AddEditTransactionVM.SelectedAccount";
    private static final String TAG_USER_SELECTED_TRANSFER_ACCOUNT = "AddEditTransactionVM.SelectedTransferAccount";
    private static final String TAG_USER_SELECTED_CATEGORY = "AddEditTransactionVM.SelectedCategory";
    private static final String TAG_IS_CONCEPT_PRISTINE = "AddEditTransactionVM.IsConceptPristine";
    private static final String TAG_USER_SELECTED_CONFIRMED = "AddEditTransactionVM.SelectedConfirmed";
    private static final String TAG_SHOW_MORE_OPTIONS = "AddEditTransactionVM.ShowMoreOptions";
    private final AccountRepository mAccountRepository;
    private final MoneyTransactionRepository mTransactionRepository;
    private final CategoryRepository mCategoryRepository;
    private final SavedStateHandle mState;

    public AddEditTransactionViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        mAccountRepository = RepositoryBuilder.getRepository(AccountRepository.class, application);
        mTransactionRepository = RepositoryBuilder.getRepository(MoneyTransactionRepository.class, application);
        mCategoryRepository = RepositoryBuilder.getRepository(CategoryRepository.class, application);
        mState = savedStateHandle;
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

    public boolean getUserSelectedConfirmedStatus() {
        if (mState.get(TAG_USER_SELECTED_CONFIRMED) == null) return true;
        //noinspection ConstantConditions
        return mState.get(TAG_USER_SELECTED_CONFIRMED);
    }

    public void setUserSelectedConfirmedStatus(boolean status) {
        mState.set(TAG_USER_SELECTED_CONFIRMED, status);
    }

    public boolean showMoreOptions() {
        if (mState.get(TAG_SHOW_MORE_OPTIONS) == null) return false;
        //noinspection ConstantConditions
        return mState.get(TAG_SHOW_MORE_OPTIONS);
    }

    public void toggleShowMoreOptions() {
        if (showMoreOptions())
            mState.set(TAG_SHOW_MORE_OPTIONS, false);
        else
            mState.set(TAG_SHOW_MORE_OPTIONS, true);
    }

    public void notifyExpectingNewAccount(boolean bool) {
        mState.set(TAG_EXPECTING_NEW_ACCOUNT, bool);
    }

    public boolean isExpectingNewAccount() {
        if (mState.get(TAG_EXPECTING_NEW_ACCOUNT) == null) return false;
        //noinspection ConstantConditions
        return mState.get(TAG_EXPECTING_NEW_ACCOUNT);
    }

    public void notifyExpectingNewCategory(boolean bool) {
        mState.set(TAG_EXPECTING_NEW_CATEGORY, bool);
    }

    public boolean isExpectingNewCategory() {
        if (mState.get(TAG_EXPECTING_NEW_CATEGORY) == null) return false;
        //noinspection ConstantConditions
        return mState.get(TAG_EXPECTING_NEW_CATEGORY);
    }

    public int getSelectedAccount() {
        if (mState.get(TAG_USER_SELECTED_ACCOUNT) == null) return 0;
        //noinspection ConstantConditions
        return mState.get(TAG_USER_SELECTED_ACCOUNT);
    }

    public void setSelectedAccount(int accountId) {
        mState.set(TAG_USER_SELECTED_ACCOUNT, accountId);
    }

    public Integer getSelectedTransferAccount() {
        return mState.get(TAG_USER_SELECTED_TRANSFER_ACCOUNT);
    }

    public void setSelectedTransferAccount(Integer accountId) {
        mState.set(TAG_USER_SELECTED_TRANSFER_ACCOUNT, accountId == 0 ? null : accountId);
    }

    public int getSelectedCategory() {
        if (mState.get(TAG_USER_SELECTED_CATEGORY) == null) return 0;
        //noinspection ConstantConditions
        return mState.get(TAG_USER_SELECTED_CATEGORY);
    }

    public void setSelectedCategory(int categoryId) {
        mState.set(TAG_USER_SELECTED_CATEGORY, categoryId);
    }

    public Single<Integer> restoreDestinationAccount(int accountId) {
        return mAccountRepository.restore(accountId);
    }
}
