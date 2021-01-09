package io.github.alansanchezp.gnomy.viewmodel.account;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import io.github.alansanchezp.gnomy.data.RepositoryBuilder;
import io.github.alansanchezp.gnomy.data.account.AccountRepository;
import io.github.alansanchezp.gnomy.data.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.reactivex.Single;

public class AccountViewModel extends AndroidViewModel {
    private final AccountRepository mRepository;

    public AccountViewModel (Application application) {
        super(application);
        mRepository = RepositoryBuilder.getRepository(AccountRepository.class, application);
    }

    public LiveData<AccountWithAccumulated> getAccountWithAccumulated(int accountId) {
        return mRepository.getAccumulatedAtMonth(accountId, DateUtil.now());
    }

    public Single<Integer> archive(int accountId) {
        return mRepository.archive(accountId);
    }
}
