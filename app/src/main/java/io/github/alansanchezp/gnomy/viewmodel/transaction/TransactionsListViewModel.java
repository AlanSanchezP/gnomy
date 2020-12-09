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
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;

public class TransactionsListViewModel extends AndroidViewModel {
    private final SavedStateHandle mSavedState;
    private final MoneyTransactionRepository mTransactionRepository;
    private LiveData<YearMonth> mActiveMonth;
    private LiveData<List<MoneyTransaction>> mTransactions;
    private LiveData<TreeMap<Integer, List<MoneyTransaction>>> mTransactionGroups;

    public TransactionsListViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        mSavedState = savedStateHandle;
        mTransactionRepository = new MoneyTransactionRepository(application);
    }

    public void bindMonth(LiveData<YearMonth> month) {
        if (mActiveMonth == null) {
            mActiveMonth = month;
            mTransactions = Transformations.switchMap(month, mTransactionRepository::getAll);
        }
    }

    public LiveData<TreeMap<Integer, List<MoneyTransaction>>> getGroupsByDay() {
        if (mTransactionGroups == null) {
            mTransactionGroups = Transformations.map(mTransactions, a -> {
                TreeMap<Integer, List<MoneyTransaction>> map = new TreeMap<>();
                if (a == null || a.isEmpty()) return map;
                for (MoneyTransaction item : a) {
                    int dayOfMonth = item.getDate().getDayOfMonth();
                    List<MoneyTransaction> dayList = map.get(dayOfMonth);
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
}
