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
    private static final String TAG_SELECTED_COLOR = "AddEditAccountVM.SelectedColor";
    private static final String TAG_IS_NAME_PRISTINE = "AddEditAccountVM.IsNamePristine";
    private static final String TAG_IS_INITIAL_VALUE_PRISTINE = "AddEditAccountVM.IsInitialValuePristine";
    private final AccountRepository mRepository;
    private final SavedStateHandle mState;
    private final MutableLiveData<Integer> mutableAccountColor;
    public final LiveData<Integer> accountColor;

    public AddEditAccountViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        mRepository = new AccountRepository(application);
        mState = savedStateHandle;

        mutableAccountColor = mState.getLiveData(TAG_SELECTED_COLOR, 1);
        accountColor = mutableAccountColor;
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
        if (mState.get(TAG_IS_NAME_PRISTINE) == null) return true;
        //noinspection ConstantConditions
        return mState.get(TAG_IS_NAME_PRISTINE);
    }

    public void notifyAccountNameChanged() {
        mState.set(TAG_IS_NAME_PRISTINE, false);
    }

    public boolean initialValueIsPristine() {
        if (mState.get(TAG_IS_INITIAL_VALUE_PRISTINE) == null) return true;
        //noinspection ConstantConditions
        return mState.get(TAG_IS_INITIAL_VALUE_PRISTINE);
    }

    public void notifyInitialValueChanged() {
        mState.set(TAG_IS_INITIAL_VALUE_PRISTINE, false);
    }
}
