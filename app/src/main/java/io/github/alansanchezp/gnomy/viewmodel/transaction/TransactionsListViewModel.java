package io.github.alansanchezp.gnomy.viewmodel.transaction;

import android.app.Application;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;
import io.github.alansanchezp.gnomy.database.transaction.TransactionDisplayData;
import io.reactivex.Single;

public class TransactionsListViewModel extends AndroidViewModel {
    private static final String TAG_TARGET_TO_DELETE = "TransactionsListVM.TargetToDelete";
    private final SavedStateHandle mSavedState;
    private final MoneyTransactionRepository mTransactionRepository;
    private LiveData<YearMonth> mActiveMonth;
    private LiveData<List<TransactionDisplayData>> mTransactions;
    private LiveData<TreeMap<Integer, List<TransactionDisplayData>>> mTransactionGroups;

    public TransactionsListViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        mSavedState = savedStateHandle;
        mTransactionRepository = new MoneyTransactionRepository(application);
    }

    public void bindMonth(LiveData<YearMonth> month) {
        if (mActiveMonth == null) {
            mActiveMonth = month;
            mTransactions = Transformations.switchMap(month, mTransactionRepository::getAllFromMonth);
        }
    }

    public LiveData<TreeMap<Integer, List<TransactionDisplayData>>> getGroupsByDay() {
        if (mTransactionGroups == null) {
            mTransactionGroups = Transformations.map(mTransactions, a -> {
                TreeMap<Integer, List<TransactionDisplayData>> map = new TreeMap<>();
                if (a == null || a.isEmpty()) return map;
                for (TransactionDisplayData item : a) {
                    int dayOfMonth = item.transaction.getDate().getDayOfMonth();
                    List<TransactionDisplayData> dayList = map.get(dayOfMonth);
                    if (dayList == null) {
                        dayList = new ArrayList<>();
                        map.put(dayOfMonth, dayList);
                    }
                    dayList.add(item);
                }
                return map;
            });
        }
        return mTransactionGroups;
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
}
