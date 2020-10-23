package io.github.alansanchezp.gnomy.viewmodel.account;

import android.app.Application;

import androidx.annotation.ColorInt;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;

public class AddEditAccountViewModel extends AndroidViewModel {
    private static final String COLOR_TAG = "account-color";
    private static final String NAME_PRISTINE_TAG = "name-is-pristine";
    private static final String INITIAL_VALUE_PRISTINE_TAG = "initial-value-is-pristine";
    private final AccountRepository mRepository;
    private final SavedStateHandle mState;
    private final MutableLiveData<Integer> mutableAccountColor;
    public final LiveData<Integer> accountColor;

    public AddEditAccountViewModel(Application application, SavedStateHandle savedStateHandle) {
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

    public boolean accountNameIsPristine() {
        if (mState.get(NAME_PRISTINE_TAG) == null) return true;
        //noinspection ConstantConditions
        return mState.get(NAME_PRISTINE_TAG);
    }

    public void notifyAccountNameChanged() {
        mState.set(NAME_PRISTINE_TAG, false);
    }

    public boolean initialValueIsPristine() {
        if (mState.get(INITIAL_VALUE_PRISTINE_TAG) == null) return true;
        //noinspection ConstantConditions
        return mState.get(INITIAL_VALUE_PRISTINE_TAG);
    }

    public void notifyInitialValueChanged() {
        mState.set(INITIAL_VALUE_PRISTINE_TAG, false);
    }
}
