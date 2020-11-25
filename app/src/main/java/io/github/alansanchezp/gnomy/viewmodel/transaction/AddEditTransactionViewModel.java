package io.github.alansanchezp.gnomy.viewmodel.transaction;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.category.CategoryRepository;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;
import io.reactivex.Single;

public class AddEditTransactionViewModel extends AndroidViewModel {
    private final AccountRepository mAccountRepository;
    private final MoneyTransactionRepository mTransactionRepository;
    private final CategoryRepository mCategoryRepository;

    public AddEditTransactionViewModel(Application application) {
        super(application);
        mAccountRepository = new AccountRepository(application);
        mTransactionRepository = new MoneyTransactionRepository(application);
        mCategoryRepository = new CategoryRepository(application);
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
}
