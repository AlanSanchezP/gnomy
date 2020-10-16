package io.github.alansanchezp.gnomy.viewmodel.customView;

import android.app.Application;

import java.time.YearMonth;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.github.alansanchezp.gnomy.util.DateUtil;

public class MonthToolbarViewModel extends AndroidViewModel {
    private MutableLiveData<YearMonth> mMonthFilter = new MutableLiveData<>();
    public LiveData<YearMonth> selectedMonth;

    public MonthToolbarViewModel(Application application) {
        super(application);
        if (mMonthFilter.getValue() == null) mMonthFilter.postValue(DateUtil.now());
        if (selectedMonth == null) selectedMonth = (LiveData<YearMonth>) mMonthFilter;
    }

    public void prevMonth() {
        mMonthFilter.postValue(mMonthFilter.getValue().minusMonths(1));
    }

    public void nextMonth() {
        mMonthFilter.postValue(mMonthFilter.getValue().plusMonths(1));
    }

    public void today() {
        mMonthFilter.postValue(DateUtil.now());
    }

    public void setMonth(YearMonth month) {
        if (month == null) return;
        mMonthFilter.postValue(month);
    }
}