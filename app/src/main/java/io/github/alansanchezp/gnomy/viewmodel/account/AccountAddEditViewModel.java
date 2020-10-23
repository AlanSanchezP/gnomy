package io.github.alansanchezp.gnomy.viewmodel.account;

import android.app.Application;

import androidx.annotation.ColorInt;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;

public class AccountAddEditViewModel extends AndroidViewModel {
    private static final String COLOR_TAG = "account-color";
    private AccountRepository mRepository;
    private final SavedStateHandle mState;
    private final MutableLiveData<Integer> mutableAccountColor;
    public final LiveData<Integer> accountColor;

    public AccountAddEditViewModel (Application application, SavedStateHandle savedStateHandle) {
        super(application);
        mRepository = new AccountRepository(application);
        mState = savedStateHandle;

        mutableAccountColor = mState.getLiveData(COLOR_TAG, 1);
        accountColor = (LiveData<Integer>) mutableAccountColor;
    }

    public LiveData<Account> getAccount(int id) {
        if (id == 0) return null;
        return mRepository.getAccount(id);
    }

    public void insert(Account account) {
        mRepository.insert(account);
    }

    public void update(Account account) {
        mRepository.update(account);
    }

    public void setAccountColor(@ColorInt int color) {
        mutableAccountColor.setValue(color);
    }
}
