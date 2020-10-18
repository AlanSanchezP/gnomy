package io.github.alansanchezp.gnomy.viewmodel.customView;

import android.app.Application;

import java.time.YearMonth;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.github.alansanchezp.gnomy.util.DateUtil;

public class MonthToolbarViewModel extends AndroidViewModel {
    private MutableLiveData<YearMonth> mutableActiveMonth = new MutableLiveData<>();
    public final LiveData<YearMonth> activeMonth;

    public MonthToolbarViewModel(Application application) {
        super(application);
        if (mutableActiveMonth.getValue() == null) mutableActiveMonth.postValue(DateUtil.now());
        activeMonth = (LiveData<YearMonth>) mutableActiveMonth;
    }

    public void prevMonth() {
        mutableActiveMonth.postValue(mutableActiveMonth.getValue().minusMonths(1));
    }

    public void nextMonth() {
        mutableActiveMonth.postValue(mutableActiveMonth.getValue().plusMonths(1));
    }

    public void today() {
        mutableActiveMonth.postValue(DateUtil.now());
    }

    public void setMonth(YearMonth month) {
        if (month == null) return;
        mutableActiveMonth.postValue(month);
    }
}